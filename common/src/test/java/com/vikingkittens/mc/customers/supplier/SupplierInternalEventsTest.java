package com.vikingkittens.mc.customers.supplier;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SupplierInternalEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void supplierSpawnerConfigurationIsCountedCopiedAndComparedByValue() {
        ItemStack sellItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        SupplierInternalEvents.SupplierSpawnerConfigChanged event = event(sellItem, costItem, true);
        SupplierInternalEvents.SupplierSpawnerConfigChanged equivalent = event(
                sellItem.copy(),
                costItem.copy(),
                true
        );

        sellItem.setCount(1);
        costItem.setCount(1);

        assertEquals(1, event.numSellItems());
        assertEquals(1, event.numCostItems());
        assertEquals(2, event.numAppearances());
        assertEquals(3, event.offers().getFirst().item().getCount());
        assertEquals(2, event.offers().getFirst().cost().getCount());
        assertTrue(event.hasSameConfiguration(equivalent));
    }

    @Test
    void supplierSpawnerConfigurationDetectsAChangedValue() {
        assertFalse(event(new ItemStack(Items.APPLE), new ItemStack(Items.EMERALD), true)
                .hasSameConfiguration(event(new ItemStack(Items.APPLE), new ItemStack(Items.EMERALD), false)));
    }

    private static SupplierInternalEvents.SupplierSpawnerConfigChanged event(
            ItemStack sellItem,
            ItemStack costItem,
            boolean autoCost
    ) {
        return new SupplierInternalEvents.SupplierSpawnerConfigChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                UUID.randomUUID(),
                List.of(new SupplierInternalEvents.Offer(sellItem, costItem)),
                autoCost,
                List.of("default", "customers:first", "customers:second")
        );
    }
}
