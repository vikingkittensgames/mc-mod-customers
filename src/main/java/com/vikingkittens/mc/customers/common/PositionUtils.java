package com.vikingkittens.mc.customers.common;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

public class PositionUtils {
    public static Direction getClosestHorizontalDirection(BlockPos start, BlockPos end) {
        int deltaX = end.getX() - start.getX();
        int deltaZ = end.getZ() - start.getZ();

        if (Math.abs(deltaX) > Math.abs(deltaZ)) {
            return deltaX > 0 ? Direction.EAST : Direction.WEST;
        }

        return deltaZ > 0 ? Direction.SOUTH : Direction.NORTH;
    }
    public static BlockPos findGroundedTargetPosition(
            LevelReader level,
            BlockPos initialPosition
    ) {
        int minimumY = LevelCUtils.getMinBuildHeight(level);
        int maximumY = LevelCUtils.getMaxBuildHeight(level);
        BlockPos.MutableBlockPos position = initialPosition.mutable();

        while (
                position.getY() < maximumY - 1
                        && !level.getBlockState(position).isAir()
        ) {
            position.move(Direction.UP);
        }

        if (!level.getBlockState(position).isAir()) {
            return null;
        }

        while (
                position.getY() > minimumY
                        && level.getBlockState(position.below()).isAir()
        ) {
            position.move(Direction.DOWN);
        }

        if (position.getY() <= minimumY
                || position.getY() + 1 >= maximumY) {
            return null;
        }

        BlockPos targetPosition = position.immutable();
        return level.getBlockState(targetPosition).isAir()
                && level.getBlockState(targetPosition.above()).isAir()
                ? targetPosition
                : null;
    }
}
