package kamorzy.fisheries_wbf.fishery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class FisheryBaitHandler {
    private static final Map<UUID, ActiveBait> ACTIVE_BAIT = new HashMap<>();

    private FisheryBaitHandler() {
    }

    // Purpose is only to decide whether to create or keep an instance of ActiveBait.
    public static void tickItemEntity(ItemEntity itemEntity) {
        if (!(itemEntity.level() instanceof ServerLevel level)) {
            return;
        }

        UUID uuid = itemEntity.getUUID();
        ItemStack stack = itemEntity.getItem();

        if (stack.isEmpty() || itemEntity.isRemoved()) {
            ACTIVE_BAIT.remove(uuid);
            return;
        }

        Optional<FisheryBaitProfile> profile = FisheryBaitProfile.fromItem(stack.getItem());

        if (profile.isEmpty()) {
            ACTIVE_BAIT.remove(uuid);
            return;
        }

        ActiveBait activeBait = ACTIVE_BAIT.compute(uuid, (baitUuid, existing) -> {
            if (existing == null || existing.profile.baitItem() != profile.get().baitItem()) {
                return new ActiveBait(profile.get());
            }

            return existing;
        });

        if (activeBait.tick(itemEntity, level)) {
            ACTIVE_BAIT.remove(uuid);
        }
    }

    private static boolean isFloatingInWater(ItemEntity itemEntity) {
        BlockPos pos = itemEntity.blockPosition();

        return itemEntity.isInWater()
                || itemEntity.level().getFluidState(pos).is(FluidTags.WATER)
                || itemEntity.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    // States determine when to run logic, delay it, or end it in ActiveBait.
    private enum BaitState {
        DRY,
        ACTIVE,
        COMPLETE
    }

    private static final class ActiveBait {
        private final FisheryBaitProfile profile;
        private final List<FisheryBiteRecord> biteRecords = new ArrayList<>();

        private int delayTicks = FisheryConstants.INITIAL_BAIT_DELAY_TICKS;
        private int delayTickTarget = delayTicks;
        private int attractionDelayTicks = 0;
        private int idleFeedbackTicks;

        private BaitState state = BaitState.DRY;

        private ActiveBait(FisheryBaitProfile profile) {
            this.profile = profile;
        }

        private boolean tick(ItemEntity itemEntity, ServerLevel level) {
            if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty() || itemEntity.getItem().getItem() != profile.baitItem()) {
                return true;
            }

            if (state == BaitState.DRY) {
                tickDry(itemEntity);
                return false;
            }

            if (state == BaitState.ACTIVE) {
                tickActive(itemEntity, level);
                return false;
            }

            if (state == BaitState.COMPLETE) {
                return tickComplete(itemEntity, level);
            }

            return false;
        }

        private void tickDry(ItemEntity itemEntity) {
            if (isFloatingInWater(itemEntity)) {
                state = BaitState.ACTIVE;
                attractionDelayTicks = 0;
                idleFeedbackTicks = 0;
                return;
            }
            delayTicks--;
            if (delayTicks <= 0) {
                delayTickTarget = delayTickTarget * 2;
                delayTicks = delayTickTarget;
            }
        }

        private void tickActive(ItemEntity itemEntity, ServerLevel level) {
            if (!isFloatingInWater(itemEntity)) {
                state = BaitState.DRY;
                delayTickTarget = FisheryConstants.INITIAL_BAIT_DELAY_TICKS;
                delayTicks = delayTickTarget;
                return;
            }

            idleFeedbackTicks++;
            if (idleFeedbackTicks >= 20) {
                idleFeedbackTicks = 0;
                FisheryFeedback.baitIdle(level, itemEntity);
            }

            attractionDelayTicks--;
            if (attractionDelayTicks <= 0) {
                attractionDelayTicks = FisheryConstants.ACTIVE_ATTRACTION_INTERVAL_TICKS;

                attractFish(itemEntity, level);
                scanForCloseBites(itemEntity, level);

            }

            if (biteRecords.size() >= profile.requiredBites()) {
                state = BaitState.COMPLETE;
                delayTicks = 0;
            }
        }

        private boolean tickComplete(ItemEntity itemEntity, ServerLevel level) {
            if (delayTicks > 0) {
                delayTicks--;
                return false;
            }

            if (!FisheryOpenWaterCheck.isFlexiblyOpenWater(
                    level,
                    itemEntity.blockPosition(),
                    level.getRandom()
            )) {
                FisheryFeedback.invalidOpenWater(level, itemEntity);
                delayTicks = FisheryConstants.COMPLETE_RETRY_DELAY_TICKS;
                return false;
            }

            complete(itemEntity, level);
            return true;
        }

        private void attractFish(ItemEntity itemEntity, ServerLevel level) {
            List<Mob> candidates = FisheryFishAttractor.findEligibleFish(
                    itemEntity,
                    level,
                    profile,
                    fish -> !hasRecordedBiteFrom(fish)
            );

            if (candidates.isEmpty()) {
                return;
            }

            shuffle(candidates, level.getRandom());

            for (Mob fish : candidates) {
                FisheryFishAttractor.moveFishTowardBait(fish, itemEntity);
                FisheryFeedback.fishAttracted(level, fish);
            }
        }

        private void scanForCloseBites(ItemEntity itemEntity, ServerLevel level) {
            List<Mob> candidates = FisheryFishAttractor.findEligibleFish(
                    itemEntity,
                    level,
                    profile,
                    fish -> !hasRecordedBiteFrom(fish)
            );

            if (candidates.isEmpty()) {
                return;
            }

            shuffle(candidates, level.getRandom());

            for (Mob fish : candidates) {
                if (biteRecords.size() >= profile.requiredBites()) {
                    break;
                }

                if (hasRecordedBiteFrom(fish)) {
                    continue;
                }

                if (!FisheryFishAttractor.isCloseEnoughToBite(fish, itemEntity)) {
                    continue;
                }

                biteRecords.add(FisheryBiteRecord.from(fish));

                FisheryFeedback.bite(
                        level,
                        itemEntity,
                        biteRecords.size(),
                        profile.requiredBites()
                );
            }
        }

        private boolean hasRecordedBiteFrom(Mob fish) {
            UUID fishUuid = fish.getUUID();

            for (FisheryBiteRecord record : biteRecords) {
                if (record.parentUuid().equals(fishUuid)) {
                    return true;
                }
            }

            return false;
        }

        private void complete(ItemEntity itemEntity, ServerLevel level) {
            state = BaitState.COMPLETE;
            Vec3 spawnPos = itemEntity.position();

            for (int i = 0; i < profile.spawnCount(); i++) {
                FisheryBiteRecord selectedParent = biteRecords.get(
                        level.getRandom().nextInt(biteRecords.size())
                );

                FisheryFishSpawner.spawnCopiedFish(level, spawnPos, selectedParent);
                FisheryFishSpawner.applyCooldownToLivingParentIfPresent(level, selectedParent);
            }

            FisheryFeedback.completed(level, spawnPos, profile.spawnCount());

            consumeOneBaitItem(itemEntity);
            biteRecords.clear();
        }

        private static void consumeOneBaitItem(ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            stack.shrink(1);

            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
        }

        private static void shuffle(List<Mob> list, RandomSource random) {
            for (int i = list.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Mob temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
    }
}