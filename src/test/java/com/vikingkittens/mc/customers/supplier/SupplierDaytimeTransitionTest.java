package com.vikingkittens.mc.customers.supplier;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupplierDaytimeTransitionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void roundTripsDaytimePersistenceState() {
        SupplierSpawnerBlockEntity spawner = mock(
                SupplierSpawnerBlockEntity.class,
                CALLS_REAL_METHODS
        );
        DataReader input = mock(DataReader.class);
        when(input.getBoolean("daytimeStateInitialized")).thenReturn(true);
        when(input.getBoolean("lastTickWasDaytime")).thenReturn(true);
        spawner.readSpawnerData(input);

        DataWriter output = mock(DataWriter.class);
        spawner.writeSpawnerData(output);

        verify(output).putBoolean("daytimeStateInitialized", true);
        verify(output).putBoolean("lastTickWasDaytime", true);
    }
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
