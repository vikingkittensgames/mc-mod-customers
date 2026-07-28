package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.PositionUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import org.slf4j.Logger;

import java.util.UUID;

public class CustomerLineUpGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CustomerVillagerEntity customer;
    private UUID followingCustomerId;

    public CustomerLineUpGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, customer.getSpawnPos(), speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                customer.getCounterTargetBlockPos() != null &&
                (
                        // Happy path for state flow
                        customer.getState() == CustomerState.LINING_UP ||
                        // Non-happy path where movement starts and the path is lost like with a server restart
                        (
                                customer.getState() == CustomerState.IN_LINE &&
                                customer.getNavigation().getPath() == null
                        )
                );
    }

    @Override
    protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
        // Use this to inject a forced state change check that should trigger ending the goal
        return super.isValidTarget(levelReader, blockPos) && (
                customer.getState() == CustomerState.LINING_UP ||
                customer.getState() == CustomerState.IN_LINE
        );
    }

    @Override
    public void start() {
        // Reset state
        CustomerSpawnerBlockEntity spawner = customer.getSpawner();
        if (spawner != null) {
            followingCustomerId = spawner.getReservedTargetCounterPositionFollowingCustomerId(
                    customer.getCounterTargetBlockPos(),
                    customer.getUUID()
            );
            if (followingCustomerId != null) {
                CustomerVillagerEntity followingCustomer = CustomerVillagerEntity.getActiveCustomer(customer.level(), followingCustomerId);
                if (followingCustomer != null) {
                    Direction direction = PositionUtils.getClosestHorizontalDirection(spawner.getBlockPos(), followingCustomer.blockPosition()).getOpposite();
                    targetPos = PositionUtils.findGroundedTargetPosition(
                            customer.level(),
                            followingCustomer.blockPosition().relative(direction)
                    );
                    if (targetPos != null) {
                        customer.setState(CustomerState.IN_LINE);
                        super.start();
                    } else {
                        customer.setState(CustomerState.WAITING_ON_LEADER);
                    }
                } else {
                    customer.setState(CustomerState.INITIALIZING);
                }
            } else {
                customer.setState(CustomerState.INITIALIZING);
            }
        }
    }

    @Override
    public double acceptedDistance() {
        return 0.5;
    }

    @Override
    protected void onDone() {
        // Check for a forced state change
        if (customer.getState() == CustomerState.IN_LINE) {
            mob.moveTo(targetPos.getBottomCenter(), mob.getYRot(), mob.getXRot());
            CustomerVillagerEntity followingCustomer = CustomerVillagerEntity.getActiveCustomer(
                    customer.level(),
                    followingCustomerId
            );
            if (followingCustomer != null) {
                customer.lookAt(
                        EntityAnchorArgument.Anchor.EYES,
                        followingCustomer.getEyePosition()
                );
            } else {
                customer.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos.getCenter());
            }
            customer.setState(CustomerState.WAITING_ON_LEADER);
        }
    }
}
