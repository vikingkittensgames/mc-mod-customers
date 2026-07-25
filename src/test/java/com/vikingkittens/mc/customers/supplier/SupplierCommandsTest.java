package com.vikingkittens.mc.customers.supplier;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupplierCommandsTest {
    @Test
    void formatsEnabledSpawner() {
        assertEquals(
                "  (100, 60, 150): Enabled",
                SupplierCommands.formatSpawner(new BlockPos(100, 60, 150), true).getString()
        );
    }

    @Test
    void formatsDisabledSpawner() {
        assertEquals(
                "  (300, 80, -200): Disabled",
                SupplierCommands.formatSpawner(new BlockPos(300, 80, -200), false).getString()
        );
    }
}