package kamorzy.fisheries_wbf.fishery;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FisheryFishAttractor {
    private FisheryFishAttractor() {
    }

    public static List<Mob> findEligibleFish(
            ItemEntity itemEntity,
            ServerLevel level,
            FisheryBaitProfile profile,
            Predicate<Mob> extraFishFilter
    ) {
        AABB searchBox = itemEntity.getBoundingBox().inflate(FisheryConstants.FISH_NOTICE_RANGE);

        return level.getEntities(
                EntityTypeTest.forClass(Mob.class),
                searchBox,
                fish -> profile.isEligible(fish)
                        && !FisheryFishSpawner.isCoolingDown(fish)
                        && isFishInWater(fish)
                        && extraFishFilter.test(fish)
        );
    }

    public static void moveFishTowardBait(Mob fish, ItemEntity itemEntity) {
        Vec3 baitPos = itemEntity.position();

        boolean acceptedPath = fish.getNavigation().moveTo(
                baitPos.x,
                baitPos.y,
                baitPos.z,
                FisheryConstants.ATTRACT_SPEED
        );

        fish.getLookControl().setLookAt(itemEntity, 30.0F, 30.0F);
    }

    public static boolean isCloseEnoughToBite(Mob fish, ItemEntity itemEntity) {
        return fish.distanceToSqr(itemEntity)
                <= FisheryConstants.BITE_DISTANCE * FisheryConstants.BITE_DISTANCE;
    }

    private static boolean isFishInWater(Mob fish) {
        BlockPos pos = fish.blockPosition();

        return fish.level().getFluidState(pos).is(FluidTags.WATER)
                || fish.level().getFluidState(pos.above()).is(FluidTags.WATER)
                || fish.level().getFluidState(pos.below()).is(FluidTags.WATER);
    }
}