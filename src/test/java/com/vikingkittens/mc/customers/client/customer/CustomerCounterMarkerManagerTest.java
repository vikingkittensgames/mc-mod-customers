package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerCounterMarkerManagerTest {
    @AfterEach
    void clearMarkers() {
        CustomerCounterMarkerManager.clear();
    }

    @Test
    void markersRemainVisibleUntilNinetySecondsExpire() {
        CustomerCounterMarker marker = new CustomerCounterMarker(
                BlockPos.ZERO,
                CustomerSpawnerMode.DAY
        );
        BlockPos surroundingPosition = new BlockPos(1, 2, 3);

        CustomerCounterMarkerManager.show(
                List.of(marker),
                List.of(surroundingPosition),
                1_000L
        );

        assertEquals(
                List.of(marker),
                CustomerCounterMarkerManager.get(90_999L)
        );
        assertEquals(
                List.of(surroundingPosition),
                CustomerCounterMarkerManager.getSurroundingPositions(90_999L)
        );
        assertEquals(
                List.of(),
                CustomerCounterMarkerManager.get(91_000L)
        );
        assertEquals(
                List.of(),
                CustomerCounterMarkerManager.getSurroundingPositions(91_000L)
        );
    }

    @Test
    void showingMarkersReplacesThePreviousSetAndRestartsExpiration() {
        CustomerCounterMarker first = new CustomerCounterMarker(
                BlockPos.ZERO,
                CustomerSpawnerMode.DAY
        );
        CustomerCounterMarker second = new CustomerCounterMarker(
                new BlockPos(10, 20, 30),
                CustomerSpawnerMode.NIGHT
        );

        CustomerCounterMarkerManager.show(List.of(first), List.of(), 1_000L);
        CustomerCounterMarkerManager.show(List.of(second), List.of(), 50_000L);

        assertEquals(
                List.of(second),
                CustomerCounterMarkerManager.get(139_999L)
        );
    }

    @Test
    void rotationAdvancesSmoothlyBetweenGameTicks() {
        assertEquals(
                80.0F,
                CustomerCounterMarkerManager.getRotationDegrees(1_000L),
                0.0001F
        );
        assertEquals(
                80.8F,
                CustomerCounterMarkerManager.getRotationDegrees(1_010L),
                0.0001F
        );
    }

    @Test
    void rotationWrapsAfterOneCompleteTurn() {
        assertEquals(
                359.92F,
                CustomerCounterMarkerManager.getRotationDegrees(4_499L),
                0.0001F
        );
        assertEquals(
                0.0F,
                CustomerCounterMarkerManager.getRotationDegrees(4_500L),
                0.0001F
        );
    }

    @Test
    void bobbingUsesElapsedClientTime() {
        assertEquals(
                0.0F,
                CustomerCounterMarkerManager.getBobOffset(0L),
                0.0001F
        );
        assertEquals(
                0.1F,
                CustomerCounterMarkerManager.getBobOffset(785L),
                0.001F
        );
    }
}
