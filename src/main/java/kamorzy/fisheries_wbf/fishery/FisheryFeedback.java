package kamorzy.fisheries_wbf.fishery;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

public final class FisheryFeedback {
    private FisheryFeedback() {
    }

    public static void baitIdle(ServerLevel level, ItemEntity baitItem) {
        Vec3 pos = baitItem.position();

        level.sendParticles(
                ParticleTypes.BUBBLE,
                pos.x,
                pos.y + 0.15D,
                pos.z,
                1,
                0.18D,
                0.08D,
                0.18D,
                0.01D
        );
    }

    public static void fishAttracted(ServerLevel level, Mob fish) {
        Vec3 fishPos = fish.position();

        level.sendParticles(
                ParticleTypes.BUBBLE,
                fishPos.x,
                fishPos.y + 0.25D,
                fishPos.z,
                1,
                0.12D,
                0.08D,
                0.12D,
                0.01D
        );
    }

    public static void bite(ServerLevel level, ItemEntity baitItem, int currentBites, int requiredBites) {
        Vec3 pos = baitItem.position();

        double progress = Math.min(1.0D, currentBites / (double) requiredBites);
        int bubbleCount = 6 + (int) (progress * 10.0D);

        level.sendParticles(
                ParticleTypes.BUBBLE,
                pos.x,
                pos.y + 0.2D,
                pos.z,
                bubbleCount,
                0.35D,
                0.18D,
                0.35D,
                0.035D
        );

        level.sendParticles(
                ParticleTypes.FISHING,
                pos.x,
                pos.y + 0.05D,
                pos.z,
                3,
                0.25D,
                0.08D,
                0.25D,
                0.02D
        );

        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.FISHING_BOBBER_SPLASH,
                SoundSource.NEUTRAL,
                0.35F,
                1.35F + level.getRandom().nextFloat() * 0.25F
        );
    }

    public static void completed(ServerLevel level, Vec3 position, int spawnedFishCount) {
        level.sendParticles(
                ParticleTypes.BUBBLE,
                position.x,
                position.y + 0.25D,
                position.z,
                35 + spawnedFishCount * 8,
                0.75D,
                0.35D,
                0.75D,
                0.08D
        );

        level.sendParticles(
                ParticleTypes.SPLASH,
                position.x,
                position.y + 0.1D,
                position.z,
                18 + spawnedFishCount * 4,
                0.65D,
                0.18D,
                0.65D,
                0.12D
        );

        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                position.x,
                position.y + 0.35D,
                position.z,
                5,
                0.45D,
                0.25D,
                0.45D,
                0.02D
        );

        level.playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.FISHING_BOBBER_SPLASH,
                SoundSource.NEUTRAL,
                1.0F,
                0.85F + level.getRandom().nextFloat() * 0.2F
        );

        level.playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.NEUTRAL,
                0.35F,
                1.8F
        );
    }

    public static void spawnedFish(ServerLevel level, Mob fish) {
        Vec3 pos = fish.position();

        level.sendParticles(
                ParticleTypes.BUBBLE,
                pos.x,
                pos.y + 0.25D,
                pos.z,
                10,
                0.25D,
                0.2D,
                0.25D,
                0.04D
        );
    }

    public static void invalidOpenWater(ServerLevel level, ItemEntity baitItem) {
        Vec3 pos = baitItem.position();

        level.sendParticles(
                ParticleTypes.SMOKE,
                pos.x,
                pos.y + 0.2D,
                pos.z,
                3,
                0.2D,
                0.08D,
                0.2D,
                0.005D
        );

        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                SoundSource.NEUTRAL,
                0.2F,
                0.55F
        );
    }
}