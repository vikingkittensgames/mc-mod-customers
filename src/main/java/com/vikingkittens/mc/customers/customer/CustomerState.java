package com.vikingkittens.mc.customers.customer;

public enum CustomerState {
    INITIALIZING,
    MOVING_TO_COUNTER,
    BUYING,
    THANKING,
    GIVING_UP,
    DONE,
    MOVING_TO_SPAWN,
    LEAVING,
    MOVING_TO_DESPAWN;

    public boolean countsTowardSpawnerLimit() {
        return compareTo(DONE) < 0;
    }

    public boolean canPushCustomer(CustomerState otherState) {
        return compareTo(DONE) < 0
                && otherState != null
                && otherState.compareTo(DONE) < 0;
    }
}
