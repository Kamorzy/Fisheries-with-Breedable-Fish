package kamorzy.fisheries_wbf.fishery;
import kamorzy.fisheries_wbf.FisheriesWbfModEntry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;

public final class FisheryFishSpawner {
    private FisheryFishSpawner() {
    }

    public static boolean isCoolingDown(Mob fish) {
        long cooldownUntil = ((FishCooldownAccess) fish).fisheries_wbf$getBaitCooldownUntilGameTime();
        return cooldownUntil > fish.level().getGameTime();
    }

    public static void applyCooldown(Mob fish) {
        long cooldownUntil = fish.level().getGameTime() + FisheryConstants.FISH_COOLDOWN_TICKS;
        ((FishCooldownAccess) fish).fisheries_wbf$setBaitCooldownUntilGameTime(cooldownUntil);
    }

    public static void applyCooldownToLivingParentIfPresent(ServerLevel level, FisheryBiteRecord parentRecord) {
        Entity entity = level.getEntity(parentRecord.parentUuid());

        if (!(entity instanceof Mob fish)) {
            return;
        }

        if (!fish.isAlive()) {
            return;
        }

        applyCooldown(fish);
    }

    public static void spawnCopiedFish(ServerLevel level, Vec3 position, FisheryBiteRecord parentRecord) {
        EntityType<?> type = parentRecord.fishType();
        Entity entity = type.create(level, EntitySpawnReason.BREEDING);

        if (!(entity instanceof Mob fish)) {
            return;
        }

        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), parentRecord.copiedFishData().copy());
        fish.load(input);
        fish.snapTo(
                position.x + randomOffset(level),
                position.y,
                position.z + randomOffset(level),
                level.getRandom().nextFloat() * 360.0F,
                0.0F
        );

        if (parentRecord.persistent()) {
            fish.setPersistenceRequired();
        }

        applyCooldown(fish);
        level.addFreshEntity(fish);
        FisheryFeedback.spawnedFish(level, fish);
    }

    private static double randomOffset(ServerLevel level) {
        return (level.getRandom().nextDouble() - 0.5D) * 0.75D;
    }

    public interface FishCooldownAccess {
        long fisheries_wbf$getBaitCooldownUntilGameTime();
        void fisheries_wbf$setBaitCooldownUntilGameTime(long gameTime);
    }
}
