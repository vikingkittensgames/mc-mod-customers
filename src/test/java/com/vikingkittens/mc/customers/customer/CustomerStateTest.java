package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerStateTest {
    @Test
    void statesBeforeDoneCountTowardSpawnerLimit() {
        assertTrue(CustomerState.INITIALIZING.countsTowardSpawnerLimit());
        assertTrue(CustomerState.MOVING_TO_COUNTER.countsTowardSpawnerLimit());
        assertTrue(CustomerState.BUYING.countsTowardSpawnerLimit());
        assertTrue(CustomerState.THANKING.countsTowardSpawnerLimit());
        assertTrue(CustomerState.GIVING_UP.countsTowardSpawnerLimit());
    }

    @Test
    void doneAndLaterStatesDoNotCountTowardSpawnerLimit() {
        assertFalse(CustomerState.DONE.countsTowardSpawnerLimit());
        assertFalse(CustomerState.MOVING_TO_SPAWN.countsTowardSpawnerLimit());
        assertFalse(CustomerState.LEAVING.countsTowardSpawnerLimit());
        assertFalse(CustomerState.MOVING_TO_DESPAWN.countsTowardSpawnerLimit());
    }

    @Test
    void onlyStatesBeforeDoneCanPushOtherStatesBeforeDone() {
        for (CustomerState state : CustomerState.values()) {
            for (CustomerState otherState : CustomerState.values()) {
                boolean expected =
                        state.compareTo(CustomerState.DONE) < 0
                                && otherState.compareTo(CustomerState.DONE) < 0;

                assertEquals(expected, state.canPushCustomer(otherState));
            }
        }
    }

    @Test
    void customerWithoutAStateCannotBePushed() {
        assertFalse(CustomerState.BUYING.canPushCustomer(null));
    }
}
