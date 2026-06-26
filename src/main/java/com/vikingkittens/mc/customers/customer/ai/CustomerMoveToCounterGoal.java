package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CustomerMoveToCounterGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomerVillagerEntity customer;
    private BlockState counterBlockState;

    public CustomerMoveToCounterGoal(CustomerVillagerEntity customer, BlockState counterBlockState, double speedModifier) {
        super(customer, null, speedModifier);
        this.customer = customer;
        this.counterBlockState = counterBlockState;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && customer.getState() == CustomerState.INITIALIZING;
    }

    public List<BlockPos> findValidSurroundingPositions(Level level, List<BlockPos> centerPositions) {
        List<BlockPos> validPositions = new ArrayList<>();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos airCheckPos = new BlockPos.MutableBlockPos();

        for (BlockPos origin : centerPositions) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    checkPos.set(
                            origin.getX() + dx,
                            origin.getY(),
                            origin.getZ() + dz
                    );
                    if (level.getBlockState(checkPos).isAir()) {
                        for (int dy = -1; dy > -4; dy--) {
                            checkPos.setY(origin.getY() + dy);
                            if (!level.getBlockState(checkPos).isAir()) {
                                int numAir = 0;
                                airCheckPos.set(checkPos);
                                for (int ay = 1; ay < 4; ay++) {
                                    airCheckPos.setY(checkPos.getY() + ay);
                                    if (level.getBlockState(airCheckPos).isAir()) {
                                        numAir++;
                                    }
                                }
                                if (numAir >= 3) {
                                    validPositions.add(checkPos.immutable());
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
                blockState -> blockState.is(counterBlockState.getBlock())
        );
        List<BlockPos> validPositions = findValidSurroundingPositions(customer.level(), counterPositions);
        if (!validPositions.isEmpty()) {
            RandomSource random = customer.level().getRandom();
            Util.shuffle(validPositions, random);
            List<CustomerVillagerEntity> otherCustomers = SearchUtils.findEntitiesInSphere(
                    customer.level(),
                    CustomerVillagerEntity.class,
                    customer.blockPosition(),
                    64,
                    customer -> customer.getState() == CustomerState.BUYING
            );
            List<BlockPos> unusedPositions = new ArrayList<>();
            for (BlockPos pos : validPositions) {
                boolean isTooClose = false;
                for (CustomerVillagerEntity customer : otherCustomers) {
                    // distanceToSqr measures from the entity's exact position to the center of the BlockPos
                    // 3 blocks distance squared = 9.0
                    if (customer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 9.0) {
                        isTooClose = true;
                        break;
                    }
                }
                // If no customer was within 3 blocks, it's safe to use!
                if (!isTooClose) {
                    unusedPositions.add(pos);
                }
            }
            if (!unusedPositions.isEmpty()) {
                targetPos = unusedPositions.getFirst();
            } else {
                targetPos = validPositions.getFirst();
            }
        } else {
            targetPos = customer.getSpawnPos();
        }
        customer.setState(CustomerState.MOVING_TO_COUNTER);
        super.start();
    }

    @Override
    protected void onDone() {
        customer.setState(CustomerState.BUYING);
    }
}
