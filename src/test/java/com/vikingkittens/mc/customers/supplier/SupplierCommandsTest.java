package com.vikingkittens.mc.customers.supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupplierCommandsTest {
    @Test
    void formatsHeadingInGreen() {
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.GREEN),
                SupplierCommands.formatHeading().getStyle().getColor()
        );
    }

    @Test
    void formatsEnabledSpawner() {
        var result = SupplierCommands.formatSpawner(
                new BlockPos(100, 60, 150),
                true
        );

        assertEquals(
                "  (100, 60, 150): Enabled",
                result.getString()
        );
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
                result.getStyle().getColor()
        );
    }

    @Test
    void formatsDisabledSpawner() {
        var result = SupplierCommands.formatSpawner(
                new BlockPos(300, 80, -200),
                false
        );

        assertEquals(
                "  (300, 80, -200): Disabled",
                result.getString()
        );
        assertEquals(
                TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
                result.getStyle().getColor()
        );
    }
}
