package kamorzy.fisheries_wbf.fishery;

import java.util.Optional;


import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.util.RandomSource;


public final class FisheryOpenWaterCheck {
    private FisheryOpenWaterCheck() {
    }

    public static boolean isFlexiblyOpenWater(Level level, BlockPos centerPos, RandomSource random) {
        if (isOpenWater(level, centerPos)) {
            return true;
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            BlockPos offsetCenter = centerPos.offset(
                    random.nextInt(5) - 2,
                    0,
                    random.nextInt(5) - 2
            );

            if (isOpenWater(level, offsetCenter)) {
                return true;
            }
        }

        return false;
    }
    public static boolean isOpenWater(Level level, BlockPos centerPos) {
        OpenWaterType previousLayer = OpenWaterType.INVALID;

        for (int y = -1; y <= 2; y++) {
            OpenWaterType layer = getOpenWaterTypeForArea(
                    level,
                    centerPos.offset(-2, y, -2),
                    centerPos.offset(2, y, 2)
            );

            if (layer == OpenWaterType.ABOVE_WATER) {
                if (previousLayer == OpenWaterType.INVALID) {
                    return false;
                }
            } else if (layer == OpenWaterType.INSIDE_WATER) {
                if (previousLayer == OpenWaterType.ABOVE_WATER) {
                    return false;
                }
            } else {
                return false;
            }

            previousLayer = layer;
        }

        return true;
    }

    private static OpenWaterType getOpenWaterTypeForArea(Level level, BlockPos from, BlockPos to) {
        Optional<OpenWaterType> result = BlockPos.betweenClosedStream(from, to)
                .map(pos -> getOpenWaterTypeForBlock(level, pos))
                .reduce((a, b) -> a == b ? a : OpenWaterType.INVALID);

        return result.orElse(OpenWaterType.INVALID);
    }

    private static OpenWaterType getOpenWaterTypeForBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir() || state.is(Blocks.LILY_PAD)) {
            return OpenWaterType.ABOVE_WATER;
        }

        FluidState fluidState = state.getFluidState();

        if (fluidState.is(FluidTags.WATER)
                && fluidState.isSource()
                && state.getCollisionShape(level, pos).isEmpty()) {
            return OpenWaterType.INSIDE_WATER;
        }

        return OpenWaterType.INVALID;
    }

    private enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }
}