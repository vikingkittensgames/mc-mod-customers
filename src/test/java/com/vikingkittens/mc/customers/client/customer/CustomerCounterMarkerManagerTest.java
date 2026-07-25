package com.vikingkittens.mc.customers.client.customer;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        CustomerCounterMarkerManager.show(List.of(marker), 1_000L);

        assertEquals(
                List.of(marker),
                CustomerCounterMarkerManager.get(90_999L)
        );
        assertEquals(
                List.of(),
                CustomerCounterMarkerManager.get(91_000L)
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

        CustomerCounterMarkerManager.show(List.of(first), 1_000L);
        CustomerCounterMarkerManager.show(List.of(second), 50_000L);

        assertEquals(
                List.of(second),
                CustomerCounterMarkerManager.get(139_999L)
        );
    }
}