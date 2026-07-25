package com.vikingkittens.mc.customers.customer.ai;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerMoveToCounterGoalTest {
    @Test
    void rejectsConfigurationBlockAboveTheCurrentSpawner() {
        BlockPos spawnerPos = new BlockPos(100, 60, 150);

        assertFalse(CustomerMoveToCounterGoal.isPossibleCounterPosition(
                spawnerPos,
                spawnerPos.above(),
                true
        ));
    }

    @Test
    void rejectsConfigurationBlockAboveAnotherSpawner() {
        assertFalse(CustomerMoveToCounterGoal.isPossibleCounterPosition(
                new BlockPos(100, 60, 150),
                new BlockPos(120, 65, 170),
                true
        ));
    }

    @Test
    void acceptsMatchingCounterAwayFromSpawner() {
        assertTrue(CustomerMoveToCounterGoal.isPossibleCounterPosition(
                new BlockPos(100, 60, 150),
                new BlockPos(110, 65, 160),
                false
        ));
    }
}
