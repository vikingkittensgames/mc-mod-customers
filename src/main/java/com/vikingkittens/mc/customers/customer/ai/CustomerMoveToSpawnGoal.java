package com.vikingkittens.mc.customers.customer.ai;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import org.slf4j.Logger;

public class CustomerMoveToSpawnGoal extends MobMoveToGoal {
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomerVillagerEntity customer;

    public CustomerMoveToSpawnGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, customer.getSpawnPos(), speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() &&
                (
                        // Happy path for state flow
                        customer.getState() == CustomerState.DONE ||
                        // Non-happy path where movement starts and the path is lost like with a server restart
                        (
                            customer.getState() == CustomerState.MOVING_TO_SPAWN &&
                            customer.getNavigation().getPath() == null
                        )
                ) &&
                customer.getSpawnPos() != null;
    }

    @Override
    public void start() {
        targetPos = customer.getSpawnPos();
        // LOGGER.debug("Target positions: {}", targetPos);
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
        // LOGGER.debug("Reached spawn");
        customer.setState(CustomerState.LEAVING);
    }
}
