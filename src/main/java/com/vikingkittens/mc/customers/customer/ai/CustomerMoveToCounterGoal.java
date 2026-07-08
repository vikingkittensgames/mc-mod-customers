package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
                customer.getState() == CustomerState.INITIALIZING &&
                customer.getCounterBlockState() != null &&
                !customer.getCounterBlockState().isAir();
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
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos airCheckPos = new BlockPos.MutableBlockPos();

        for (BlockPos centerPos : centerPositions) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    checkPos.set(
                            centerPos.getX() + dx,
                            centerPos.getY() - 1,
                            centerPos.getZ() + dz
                    );
                    if (level.getBlockState(checkPos).isAir()) {
                        for (int dy = 0; dy >= -2; dy--) {
                            checkPos.setY(centerPos.getY() + dy);
                            BlockState supportBlockState = level.getBlockState(checkPos);
                            if (!supportBlockState.isAir()) {
                                if (!supportBlockPredicate.test(supportBlockState)) {
                                    break;
                                }
                                checkPos.setY(checkPos.getY() + 1);
                                int numAir = 0;
                                airCheckPos.set(checkPos);
                                for (int ay = 0; ay < 3; ay++) {
                                    airCheckPos.setY(checkPos.getY() + ay);
                                    if (level.getBlockState(airCheckPos).isAir()) {
                                        numAir++;
                                    }
                                }
                                if (numAir >= 3) {
                                    validPositions.add(new SurroundingPosition(centerPos, checkPos.immutable()));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return validPositions;
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

    @Override
    public void start() {
        targetPos = null;
        List<BlockPos> counterPositions = SearchUtils.findBlocksInSphere(
                customer.level(),
                customer.blockPosition(),
                64,
                (blockPos, blockState) -> blockState.is(customer.getCounterBlockState().getBlock()) &&
                        (blockPos.getX() != customer.getSpawnerPos().getX() || blockPos.getZ() != customer.getSpawnerPos().getZ())
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
            validPositions.sort(Comparator.comparingDouble(SurroundingPosition::getDistanceSqr));
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
        // LOGGER.debug("Reached counter or gave up: counter = {}, num-offers = {}", counterPosition, ((Villager)mob).getOffers().size());
        mob.moveTo(targetPos.getBottomCenter(), mob.getYRot(), mob.getXRot());
        if (counterPosition != null) {
            Vec3 lookTargetVec = new Vec3(
                    counterPosition.getX() + 0.5,
                    counterPosition.getY() + 0.5,
                    counterPosition.getZ() + 0.5
            );
            mob.getLookControl().setLookAt(lookTargetVec);
        }
        customer.setState(CustomerState.BUYING);
    }
}

