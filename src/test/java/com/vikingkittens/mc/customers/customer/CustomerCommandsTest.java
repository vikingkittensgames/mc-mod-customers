package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerCommandsTest {
    @Test
    void formatsSpawnerStatusAndMode() {
        assertEquals(
                "  (100, 60, 150): Enabled, titles.customers.spawn_mode.breakfast",
                CustomerCommands.formatSpawner(
                        new BlockPos(100, 60, 150),
                        true,
                        CustomerSpawnerMode.BREAKFAST
                ).getString()
        );
    }

    @Test
    void formatsIndentedCounter() {
        assertEquals(
                "      (110, 65, 160): Green Candles",
                CustomerCommands.formatCounter(
                        new BlockPos(110, 65, 160),
                        Component.literal("Green Candles")
                ).getString()
        );
    }

    @Test
    void keepsTheFirstSpawnerModeWhenSpawnersShareACounter() {
        Map<BlockPos, CustomerCounterMarker> markers = new LinkedHashMap<>();
        BlockPos position = new BlockPos(10, 20, 30);

        CustomerCommands.addCounterMarker(
                markers,
                position,
                CustomerSpawnerMode.BREAKFAST
        );
        CustomerCommands.addCounterMarker(
                markers,
                position,
                CustomerSpawnerMode.NIGHT
        );

        assertEquals(1, markers.size());
        assertEquals(
                CustomerSpawnerMode.BREAKFAST,
                markers.get(position).spawnerMode()
        );
    }
}