package com.vikingkittens.mc.customers.customer.ai;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerMoveToCounterGoalTest {
    @Test
    void findsAirImmediatelyAboveSolidSupport() {
        assertEquals(10, findTargetY(10, Set.of(10), Set.of(10, 11, 12), Set.of(9)));
    }

    @Test
    void movesAboveASeatBeforeCheckingHeadroom() {
        assertEquals(
                10,
                findTargetY(10, Set.of(10, 9), Set.of(10, 11, 12), Set.of(8))
        );
    }

    @Test
    void movesAboveALowDecorativeBlockBeforeCheckingHeadroom() {
        assertEquals(
                10,
                findTargetY(10, Set.of(10, 9), Set.of(10, 11, 12), Set.of(8))
        );
    }

    @Test
    void descendsThroughMultipleValidPositions() {
        assertEquals(
                8,
                findTargetY(10, Set.of(10, 9, 8), Set.of(8, 9, 10), Set.of(7))
        );
    }

    @Test
    void rejectsInvalidStartingPosition() {
        assertNull(findTargetY(10, Set.of(), Set.of(), Set.of(9)));
    }

    @Test
    void rejectsUnsafeSupportBlock() {
        assertNull(findTargetY(10, Set.of(10), Set.of(10, 11, 12), Set.of()));
    }

    @Test
    void rejectsPositionWithOnlyTwoAirBlocksOfHeadroom() {
        assertNull(findTargetY(10, Set.of(10), Set.of(10, 11), Set.of(9)));
    }

    @Test
    void givesUpWhenNoSupportIsFoundWithinThreeBlocks() {
        assertNull(
                findTargetY(10, Set.of(10, 9, 8, 7), Set.of(7, 8, 9, 10), Set.of())
        );
    }

    private static Integer findTargetY(
            int startingY,
            Set<Integer> validPositions,
            Set<Integer> airPositions,
            Set<Integer> validSupportPositions
    ) {
        return CustomerMoveToCounterGoal.findSurroundingTargetY(
                startingY,
                validPositions::contains,
                airPositions::contains,
                validSupportPositions::contains
        );
    }
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
