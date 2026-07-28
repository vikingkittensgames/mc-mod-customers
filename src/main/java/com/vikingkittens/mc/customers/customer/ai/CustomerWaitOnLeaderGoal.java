package com.vikingkittens.mc.customers.customer.ai;

import com.vikingkittens.mc.customers.common.ai.MobTimedGoal;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public class CustomerWaitOnLeaderGoal extends MobTimedGoal {
    private final CustomerVillagerEntity customer;
    private BlockPos followingCustomerStartPosition;

    public CustomerWaitOnLeaderGoal(CustomerVillagerEntity customer) {
        super(customer);
        this.customer = customer;
    }

    @Override
    protected long maxTicks() {
        return 20 * 5;
    }

    @Override
    public boolean canUse() {
        return super.canUse()
                && customer.getState() == CustomerState.WAITING_ON_LEADER;
    }

    @Override
    public void start() {
        super.start();
        CustomerVillagerEntity followingCustomer = getFollowingCustomer();
        followingCustomerStartPosition = followingCustomer == null
                ? null
                : followingCustomer.blockPosition();
    }
    @Override
    public boolean canContinueToUse() {
        if (!super.canContinueToUse()) {
            return false;
        }
        CustomerVillagerEntity followingCustomer = getFollowingCustomer();
        BlockPos followingCustomerPosition = followingCustomer == null
                ? null
                : followingCustomer.blockPosition();
        if (
                followingCustomerPosition != null
                        && followingCustomerStartPosition != null
                        && Math.abs(
                                followingCustomerPosition.getX()
                                        - followingCustomerStartPosition.getX()
                        ) < 1
                        && Math.abs(
                                followingCustomerPosition.getZ()
                                        - followingCustomerStartPosition.getZ()
                        ) < 1
        ) {
            return true;
        }
        customer.setState(CustomerState.INITIALIZING);
        return false;
    }

    private CustomerVillagerEntity getFollowingCustomer() {
        CustomerSpawnerBlockEntity spawner = customer.getSpawner();
        if (spawner == null || customer.getCounterTargetBlockPos() == null) {
            return null;
        }
        UUID followingCustomerId =
                spawner.getReservedTargetCounterPositionFollowingCustomerId(
                        customer.getCounterTargetBlockPos(),
                        customer.getUUID()
                );
        return followingCustomerId == null
                ? null
                : CustomerVillagerEntity.getActiveCustomer(
                        customer.level(),
                        followingCustomerId
                );
    }

    @Override
    protected void onDone() {
        customer.setState(CustomerState.INITIALIZING);
    }
}
