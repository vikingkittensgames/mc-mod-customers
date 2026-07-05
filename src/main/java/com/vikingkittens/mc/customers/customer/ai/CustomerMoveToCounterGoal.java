package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            return pos.distToCenterSqr(center.getX(), center.getY(), center.getZ());
        }

        @Override
        public String toString() {
            return center + " -(" + getDistanceSqr() + ")> " + pos;
        }
    }

    public List<SurroundingPosition> findValidSurroundingPositions(Level level, List<BlockPos> centerPositions) {
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
                            if (!level.getBlockState(checkPos).isAir()) {
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
        LOGGER.debug("Counter positions: {}", counterPositions);
        List<SurroundingPosition> validPositions = findValidSurroundingPositions(customer.level(), counterPositions);
        LOGGER.debug("Valid positions: {}", validPositions);
        if (!validPositions.isEmpty()) {
            RandomSource random = customer.level().getRandom();
            Util.shuffle(validPositions, random);
            LOGGER.debug("Valid positions shuffled: {}", validPositions);
            validPositions.sort(Comparator.comparingDouble(SurroundingPosition::getDistanceSqr));
            LOGGER.debug("Valid positions sorted: {}", validPositions);
            List<CustomerVillagerEntity> otherCustomers = SearchUtils.findEntitiesInSphere(
                    customer.level(),
                    CustomerVillagerEntity.class,
                    customer.blockPosition(),
                    64,
                    (blockpos, customer) -> customer.getState() == CustomerState.BUYING
            );
            List<SurroundingPosition> unusedPositions = new ArrayList<>();
            for (SurroundingPosition surroundingPos : validPositions) {
                BlockPos pos = surroundingPos.getPos();
                boolean isTooClose = false;
                for (CustomerVillagerEntity customer : otherCustomers) {
                    // distanceToSqr measures from the entity's exact position to the center of the BlockPos
                    // 3 blocks distance squared = 9.0
                    if (customer.distanceToSqr(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5
                    ) < 9.0) {
                        isTooClose = true;
                        break;
                    }
                }
                // If no customer was within 3 blocks, it's safe to use!
                if (!isTooClose) {
                    unusedPositions.add(surroundingPos);
                }
            }
            LOGGER.debug("Unused positions: {}", unusedPositions);
            SurroundingPosition surroundingPos;
            if (!unusedPositions.isEmpty()) {
                surroundingPos = unusedPositions.getFirst();
            } else {
                surroundingPos = validPositions.getFirst();
            }
            targetPos = surroundingPos.getPos();
            counterPosition = surroundingPos.getCenter();
        } else {
            targetPos = customer.getSpawnPos();
            counterPosition = targetPos;
        }
        LOGGER.debug("Target positions: {}", targetPos);
        LOGGER.debug("Counter positions: {}", counterPositions);
        customer.setState(CustomerState.MOVING_TO_COUNTER);
        super.start();
    }

    @Override
    protected void onDone() {
        LOGGER.debug("Reached counter or gave up: counter = {}, num-offers = {}", counterPosition, ((Villager)mob).getOffers().size());
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
