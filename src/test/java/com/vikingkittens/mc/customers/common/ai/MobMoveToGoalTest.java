package com.vikingkittens.mc.customers.common.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobMoveToGoalTest {
    @Test
    void scalesTimeoutWithDistance() {
        assertEquals(160, MobMoveToGoal.calculateMaxTicks(10, 0.25));
        assertEquals(1_024, MobMoveToGoal.calculateMaxTicks(64, 0.25));
    }

    @Test
    void accountsForMovementSpeed() {
        assertEquals(1_024, MobMoveToGoal.calculateMaxTicks(64, 0.25));
        assertEquals(256, MobMoveToGoal.calculateMaxTicks(64, 0.5));
    }
}
