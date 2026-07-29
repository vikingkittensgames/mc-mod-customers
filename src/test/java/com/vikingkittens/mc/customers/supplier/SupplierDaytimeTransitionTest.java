package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies supplier spawning at the transition from night to day.
 */
class SupplierDaytimeTransitionTest {
    @Test
    void detectsNightToDayTransition() {
        assertTrue(
                SupplierSpawnerBlockEntity.shouldSpawnForDaytimeTransition(
                        true,
                        false,
                        true
                )
        );
    }

    @Test
    void doesNotSpawnWhileInitializingDuringDaytime() {
        assertFalse(
                SupplierSpawnerBlockEntity.shouldSpawnForDaytimeTransition(
                        false,
                        false,
                        true
                )
        );
    }

    @Test
    void ignoresTransitionsThatAreNotNightToDay() {
        assertFalse(
                SupplierSpawnerBlockEntity.shouldSpawnForDaytimeTransition(
                        true,
                        true,
                        true
                )
        );
        assertFalse(
                SupplierSpawnerBlockEntity.shouldSpawnForDaytimeTransition(
                        true,
                        true,
                        false
                )
        );
        assertFalse(
                SupplierSpawnerBlockEntity.shouldSpawnForDaytimeTransition(
                        true,
                        false,
                        false
                )
        );
    }
}
