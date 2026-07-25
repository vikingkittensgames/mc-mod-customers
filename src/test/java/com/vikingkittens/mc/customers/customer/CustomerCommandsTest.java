package com.vikingkittens.mc.customers.customer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerCommandsTest {
    @Test
    void formatsHeadingsInGreen() {
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.GREEN),
                CustomerCommands.formatHeading(false).getStyle().getColor()
        );
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.GREEN),
                CustomerCommands.formatHeading(true).getStyle().getColor()
        );
    }

    @Test
    void formatsSpawnerStatusAndMode() {
        Component result = CustomerCommands.formatSpawner(
                new BlockPos(100, 60, 150),
                true,
                CustomerSpawnerMode.BREAKFAST
        );

        assertEquals(
                "  (100, 60, 150): Enabled, titles.customers.spawn_mode.breakfast",
                result.getString()
        );
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
                result.getStyle().getColor()
        );
    }

    @Test
    void formatsIndentedCounter() {
        Component result = CustomerCommands.formatCounter(
                new BlockPos(110, 65, 160),
                Component.literal("Green Candles")
        );

        assertEquals(
                "      (110, 65, 160): Green Candles",
                result.getString()
        );
        assertNull(result.getStyle().getColor());
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
