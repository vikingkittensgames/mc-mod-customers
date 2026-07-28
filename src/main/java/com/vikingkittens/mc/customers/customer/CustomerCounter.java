package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Finds customer counters and valid positions where customers can stand or sit.
 */
public final class CustomerCounter {
    private CustomerCounter() {
    }

    /**
     * Finds counter blocks configured for a customer spawner.
     */
    public static List<BlockPos> findCounterPositions(Level level, BlockPos spawnerPos, BlockState counterState) {
        return findCounterPositions(level, spawnerPos, counterState, CustomerSpawner.CUSTOMER_SPAWNER_BLOCK::get);
    }

    /**
     * Finds counter blocks using the supplied spawner block.
     */
    static List<BlockPos> findCounterPositions(
            Level level,
            BlockPos spawnerPos,
            BlockState counterState,
            Supplier<Block> spawnerBlock
    ) {
        return SearchUtils.findBlocksInSphere(
                level,
                spawnerPos,
                Config.MAX_COUNTER_DISTANCE.get(),
                (pos, state) -> state.is(counterState.getBlock())
                        && isPossibleCounterPosition(spawnerPos, pos, level, spawnerBlock)
        );
    }

    /**
     * Finds valid positions surrounding the supplied counters.
     */
    public static List<SurroundingPosition> findValidSurroundingPositions(
            Level level,
            List<BlockPos> counterPositions,
            Entity collisionEntity,
            BlockState avoidState
    ) {
        List<SurroundingPosition> result = new ArrayList<>();
        for (BlockPos counterPos : counterPositions) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos pos = findSurroundingPosition(
                        level,
                        counterPos,
                        direction.getStepX(),
                        direction.getStepZ(),
                        collisionEntity,
                        avoidState
                );
                if (pos != null) {
                    result.add(new SurroundingPosition(counterPos, pos));
                }
            }
        }
        return result;
    }

    private static BlockPos findSurroundingPosition(
            Level level,
            BlockPos counterPos,
            int dx,
            int dz,
            Entity collisionEntity,
            BlockState avoidState
    ) {
        int x = counterPos.getX() + dx;
        int z = counterPos.getZ() + dz;
        int checkY = counterPos.getY() - 1;
        if (!isValidSurroundingPosition(
                level, new BlockPos(x, checkY, z), collisionEntity, avoidState
        )) {
            return null;
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            checkY--;
            BlockPos checkedPos = new BlockPos(x, checkY, z);
            if (isValidSurroundingPosition(level, checkedPos, collisionEntity, avoidState)) {
                continue;
            }
            if (!isValidSupportBlock(level.getBlockState(checkedPos), avoidState)) {
                return null;
            }

            int targetY = checkY + 1;
            BlockPos targetPos = new BlockPos(x, targetY, z);
            if (!level.getBlockState(targetPos).isAir()
                    && isValidSurroundingPosition(level, targetPos, collisionEntity, avoidState)) {
                targetY++;
            }
            for (int airOffset = 0; airOffset < 3; airOffset++) {
                if (!level.getBlockState(new BlockPos(x, targetY + airOffset, z)).isAir()) {
                    return null;
                }
            }
            return new BlockPos(x, targetY, z);
        }
        return null;
    }

    static boolean isValidSurroundingPosition(
            Level level,
            BlockPos pos,
            Entity collisionEntity,
            BlockState avoidState
    ) {
        BlockState state = level.getBlockState(pos);
        if (avoidState != null && state.is(avoidState.getBlock())) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.of(collisionEntity));
        return state.isAir()
                || shape.isEmpty()
                || shape.max(Direction.Axis.Y) <= 1.0D / 3.0D
                || CustomerSeatEntity.isSeat(level, pos)
                && CustomerSeatEntity.canSit(level, pos, collisionEntity);
    }
    public static BlockPos getMarkerPosition(
            Level level,
            SurroundingPosition surroundingPosition,
            Entity collisionEntity,
            BlockState avoidState
    ) {
        BlockPos navigationPosition = surroundingPosition.getPosition();
        BlockPos blockBelow = navigationPosition.below();
        BlockState blockBelowState = level.getBlockState(blockBelow);
        return !blockBelowState.isAir()
                && isValidSurroundingPosition(level, blockBelow, collisionEntity, avoidState)
                ? blockBelow
                : navigationPosition;
    }

    private static boolean isValidSupportBlock(BlockState state, BlockState avoidState) {
        return (avoidState == null || !state.is(avoidState.getBlock()))
                && state.getFluidState().isEmpty()
                && !causesDamage(state);
    }

    private static boolean causesDamage(BlockState state) {
        return state.getBlock() instanceof BaseFireBlock
                || state.getBlock() instanceof CactusBlock
                || state.getBlock() instanceof MagmaBlock
                || state.getBlock() instanceof SweetBerryBushBlock
                || state.getBlock() instanceof WitherRoseBlock
                || CampfireBlock.isLitCampfire(state);
    }

    static boolean isPossibleCounterPosition(
            BlockPos spawnerPos,
            BlockPos candidatePos,
            Level level,
            Supplier<Block> spawnerBlock
    ) {
        return !level.getBlockState(candidatePos.below()).is(spawnerBlock.get())
                && (candidatePos.getX() != spawnerPos.getX()
                || candidatePos.getZ() != spawnerPos.getZ());
    }

    /**
     * Associates a customer position with the counter it surrounds.
     */
    public static final class SurroundingPosition {
        private final BlockPos center;
        private final BlockPos position;

        /**
         * Creates a surrounding-position result.
         */
        public SurroundingPosition(BlockPos center, BlockPos position) {
            this.center = center;
            this.position = position;
        }

        /**
         * Returns the counter position.
         */
        public BlockPos getCenter() {
            return center;
        }

        /**
         * Returns the customer position.
         */
        public BlockPos getPosition() {
            return position;
        }

        /**
         * Returns the squared distance from the customer position to its counter.
         */
        public double getDistanceSqr() {
            return position.distToCenterSqr(center.getCenter());
        }
    }
}
