package com.vikingkittens.mc.customers.customer.ai;

import com.vikingkittens.mc.customers.common.ai.MobMoveToGoal;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public class CustomerMoveToSpawnGoal extends MobMoveToGoal {
    private CustomerVillagerEntity customer;

    public CustomerMoveToSpawnGoal(CustomerVillagerEntity customer, double speedModifier) {
        super(customer, customer.getSpawnPos(), speedModifier);
        this.customer = customer;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && customer.getState() == CustomerState.BUYING && customer.getOffers().isEmpty();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void onDone() {
        customer.setState(CustomerState.LEAVING);
    }
}
