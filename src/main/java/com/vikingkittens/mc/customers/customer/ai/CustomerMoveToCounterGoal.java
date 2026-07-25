package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.customer.CustomerSeatEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class CustomerMoveToCounterGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomerVillagerEntity customer;
    private BlockPos counterPosition;

    public CustomerMoveToCounterGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, null, speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                (
                        // Happy path for state flow
                        customer.getState() == CustomerState.INITIALIZING ||
                        // Non-happy path where movement starts and the path is lost like with a server restart
                        (
                                customer.getState() == CustomerState.MOVING_TO_COUNTER &&
                                customer.getNavigation().getPath() == null
                        )
                ) &&
                customer.getCounterBlockState() != null &&
                !customer.getCounterBlockState().isAir();
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        // Use this to inject a forced state change check that should trigger ending the goal
        return super.isValidTarget(levelReader, blockPos) && (
                customer.getState() == CustomerState.INITIALIZING ||
                customer.getState() == CustomerState.MOVING_TO_COUNTER
        );
    }

    private static class SurroundingPosition {
        private BlockPos center;
        private BlockPos pos;

        public SurroundingPosition(BlockPos center, BlockPos pos) {
            this.center = center;
            this.pos = pos;
        }

        public BlockPos getCenter() {
            return center;
        }

        public BlockPos getPos() {
            return pos;
        }

        public double getDistanceSqr() {
            return pos.distToCenterSqr(center.getCenter());
        }

        @Override
        public String toString() {
            return center + " -(" + getDistanceSqr() + ")> " + pos;
        }
    }

    public List<SurroundingPosition> findValidSurroundingPositions(
            Level level,
            List<BlockPos> centerPositions,
            Predicate<BlockState> supportBlockPredicate
    ) {
        List<SurroundingPosition> validPositions = new ArrayList<>();

        for (BlockPos centerPos : centerPositions) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    int x = centerPos.getX() + dx;
                    int z = centerPos.getZ() + dz;
                    Integer targetY = findSurroundingTargetY(
                            centerPos.getY() - 1,
                            y -> isValidSurroundingPosition(level, new BlockPos(x, y, z)),
                            y -> level.getBlockState(new BlockPos(x, y, z)).isAir(),
                            y -> supportBlockPredicate.test(
                                    level.getBlockState(new BlockPos(x, y, z))
                            )
                    );
                    if (targetY != null) {
                        validPositions.add(new SurroundingPosition(
                                centerPos,
                                new BlockPos(x, targetY, z)
                        ));
                    }
                }
            }
        }
        return validPositions;
    }

    static Integer findSurroundingTargetY(
            int startingY,
            IntPredicate validPosition,
            IntPredicate airPosition,
            IntPredicate validSupport
    ) {
        if (!validPosition.test(startingY)) {
            return null;
        }

        int checkY = startingY;
        for (int attempt = 0; attempt < 3; attempt++) {
            checkY--;
            if (validPosition.test(checkY)) {
                continue;
            }
            if (!validSupport.test(checkY)) {
                return null;
            }

            int targetY = checkY + 1;
            if (!airPosition.test(targetY) && validPosition.test(targetY)) {
                targetY++;
            }

            for (int airOffset = 0; airOffset < 3; airOffset++) {
                if (!airPosition.test(targetY + airOffset)) {
                    return null;
                }
            }
            return targetY;
        }
        return null;
    }

    private static boolean isValidSupportBlock(CustomerVillagerEntity customer, BlockState blockState) {
        BlockState avoidBlockState = customer.getAvoidBlockState();
        return (avoidBlockState == null || !blockState.is(avoidBlockState.getBlock())) &&
                blockState.getFluidState().isEmpty() &&
                !causesDamage(blockState);
    }

    private static boolean causesDamage(BlockState blockState) {
        return blockState.getBlock() instanceof BaseFireBlock ||
                blockState.getBlock() instanceof CactusBlock ||
                blockState.getBlock() instanceof MagmaBlock ||
                blockState.getBlock() instanceof SweetBerryBushBlock ||
                blockState.getBlock() instanceof WitherRoseBlock ||
                CampfireBlock.isLitCampfire(blockState);
    }

    static boolean isLowCollisionShape(VoxelShape collisionShape) {
        return collisionShape.isEmpty()
                || collisionShape.max(Direction.Axis.Y) <= 2.0D / 3.0D;
    }

    private boolean isValidSurroundingPosition(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        VoxelShape collisionShape = state.getCollisionShape(
                level,
                pos,
                CollisionContext.of(customer)
        );
        return state.isAir()
                || collisionShape.isEmpty()
                || collisionShape.max(Direction.Axis.Y) <= 1.0D / 3.0D
                || CustomerSeatEntity.isSeat(level, pos)
                && CustomerSeatEntity.canSit(level, pos, customer);
    }

    public static List<BlockPos> findCounterPositions(
            Level level,
            BlockPos spawnerPos,
            BlockState counterBlockState
    ) {
        return SearchUtils.findBlocksInSphere(
                level,
                spawnerPos,
                Config.MAX_COUNTER_DISTANCE.get(),
                (blockPos, blockState) -> blockState.is(counterBlockState.getBlock()) &&
                        isPossibleCounterPosition(
                                spawnerPos,
                                blockPos,
                                level.getBlockState(blockPos.below())
                                        .is(CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.get())
                        )
        );
    }

    static boolean isPossibleCounterPosition(
            BlockPos spawnerPos,
            BlockPos candidatePos,
            boolean aboveCustomerSpawner
    ) {
        return !aboveCustomerSpawner &&
                (
                        candidatePos.getX() != spawnerPos.getX() ||
                        candidatePos.getZ() != spawnerPos.getZ()
                );
    }

    @Override
    public void start() {
        targetPos = null;
        List<BlockPos> counterPositions = findCounterPositions(
                customer.level(),
                customer.getSpawnerPos(),
                customer.getCounterBlockState()
        );
        // LOGGER.debug("Counter positions: {}", counterPositions);
        List<SurroundingPosition> validPositions = findValidSurroundingPositions(
                customer.level(),
                counterPositions,
                blockState -> isValidSupportBlock(customer, blockState)
        );

        // LOGGER.debug("Valid positions: {}", validPositions);
        if (!validPositions.isEmpty()) {
            // All valid positions
            RandomSource random = customer.level().getRandom();
            Util.shuffle(validPositions, random);
            // LOGGER.debug("Valid positions shuffled: {}", validPositions);
            validPositions.sort(
                    Comparator
                            .comparing((SurroundingPosition position) ->
                                    !CustomerSeatEntity.canSit(customer.level(), position.getPos().below(), customer))
                            .thenComparingDouble(SurroundingPosition::getDistanceSqr)
            );
            // LOGGER.debug("Valid positions sorted: {}", validPositions);

            // Valid positions not targeted by other customers
            List<SurroundingPosition> untargetedPositions = new ArrayList<>();
            List<SurroundingPosition> untargetedNotTooClosePositions = new ArrayList<>();
            if (customer.getSpawnerPos() != null && customer.level().getBlockEntity(customer.getSpawnerPos()) instanceof CustomerSpawnerBlockEntity spawner) {
                List<BlockPos> otherCustomersTargetPositions = new ArrayList<>();
                for (UUID customerId : spawner.getCustomerIds()) {
                    try {
                        if (((ServerLevel) customer.level()).getEntity(customerId) instanceof CustomerVillagerEntity otherCustomer) {
                            if (
                                    otherCustomer.isAlive() &&
                                            !otherCustomer.isRemoved() &&
                                            otherCustomer.getCounterTargetBlockPos() != null
                            ) {
                                otherCustomersTargetPositions.add(otherCustomer.getCounterTargetBlockPos());
                            }
                        }
                    } catch (Throwable t) {
                        LOGGER.warn("Couldn't get customer targeted position because of error", t);
                    }
                }
                for (SurroundingPosition surroundingPos : validPositions) {
                    if (otherCustomersTargetPositions.stream().noneMatch(pos -> pos.equals(surroundingPos.getPos()))) {
                        untargetedPositions.add(surroundingPos);
                        if (otherCustomersTargetPositions.stream().noneMatch(pos -> pos.distToCenterSqr(surroundingPos.getPos().getBottomCenter()) < 3 * 3)) {
                            untargetedNotTooClosePositions.add(surroundingPos);
                        }
                    }
                }
            }
            // LOGGER.debug("Untargeted positions: {}", untargetedPositions);
            // LOGGER.debug("Untargeted & not close positions: {}", untargetedNotTooClosePositions);

            SurroundingPosition surroundingPos;
            if (!untargetedNotTooClosePositions.isEmpty()) {
                surroundingPos = untargetedNotTooClosePositions.getFirst();
            } else if (!untargetedPositions.isEmpty()) {
                surroundingPos = untargetedPositions.getFirst();
            } else {
                surroundingPos = validPositions.getFirst();
            }
            targetPos = surroundingPos.getPos();
            counterPosition = surroundingPos.getCenter();
        } else {
            targetPos = customer.getSpawnPos();
            counterPosition = targetPos;
        }
        customer.setCounterTargetBlockPos(targetPos);
        // LOGGER.debug("Target positions: {}", targetPos);
        // LOGGER.debug("Counter positions: {}", counterPositions);
        customer.setState(CustomerState.MOVING_TO_COUNTER);
        super.start();
    }

    @Override
    protected void onDone() {
        // Check for a forced state change
        if (customer.getState() == CustomerState.MOVING_TO_COUNTER) {
            mob.moveTo(targetPos.getBottomCenter(), mob.getYRot(), mob.getXRot());
            if (counterPosition != null) {
                customer.lookAt(EntityAnchorArgument.Anchor.EYES, counterPosition.getCenter());
            }
            CustomerSeatEntity.trySit(customer.level(), targetPos.below(), customer);
            customer.setState(CustomerState.BUYING);
        }
    }
}

