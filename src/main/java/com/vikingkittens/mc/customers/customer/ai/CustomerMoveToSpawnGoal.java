package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import org.slf4j.Logger;

public class CustomerMoveToSpawnGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final CustomerVillagerEntity customer;

    public CustomerMoveToSpawnGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, customer.getSpawnPos(), speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                (
                        customer.getState() == CustomerState.DONE ||
                        (
                                customer.getState() == CustomerState.MOVING_TO_SPAWN &&
                                customer.getNavigation().getPath() == null
                        )
                ) &&
                customer.getSpawnPos() != null;
    }

    @Override
    public void start() {
        customer.stopRiding();
        targetPos = customer.getSpawnPos();
        customer.setState(CustomerState.MOVING_TO_SPAWN);
        customer.setCounterTargetBlockPos(null);
        super.start();
    }

    @Override
    public double acceptedDistance() {
        return 1.5;
    }

    @Override
    protected void onDone() {
        customer.setState(CustomerState.LEAVING);
    }
}
