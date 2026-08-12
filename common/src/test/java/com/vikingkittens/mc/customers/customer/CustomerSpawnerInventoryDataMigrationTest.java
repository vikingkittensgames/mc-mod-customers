package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSpawnerInventoryDataMigrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void migratesLegacySettingsAndPreservesOccupiedCostSlots() {
        Item paymentItem = Items.EMERALD;
        Item maxCustomersItem = Items.REDSTONE;
        Item existingCostItem = Items.DIAMOND;
        PersistedContainer inventory = new PersistedContainer(18, () -> {});
        inventory.setItem(
                2,
                new ItemStack(paymentItem, 3)
        );
        inventory.setItem(
                8,
                new ItemStack(existingCostItem, 4)
        );
        inventory.setItem(
                10,
                new ItemStack(maxCustomersItem, 12)
        );
        inventory.setItem(
                11,
                new ItemStack(maxCustomersItem, 20)
        );

        CustomerSpawnerBlockEntity.InventoryDataMigrationResult result =
                CustomerSpawnerBlockEntity.migrateVersion0Inventory(
                        inventory,
                        paymentItem,
                        maxCustomersItem,
                        5
                );

        assertTrue(result.changed());
        assertEquals(12, result.maxCustomers());
        assertSame(
                paymentItem,
                inventory.getItem(8).getItem()
        );
        assertEquals(3, inventory.getItem(8).getCount());
        assertSame(
                existingCostItem,
                inventory.getItem(2).getItem()
        );
        assertTrue(inventory.getItem(10).isEmpty());
        assertTrue(inventory.getItem(11).isEmpty());
    }

    @Test
    void clampsMigratedMaximumCustomersToUiRange() {
        Item paymentItem = Items.EMERALD;
        Item maxCustomersItem = Items.REDSTONE;
        PersistedContainer inventory = new PersistedContainer(9, () -> {});
        inventory.setItem(
                0,
                new ItemStack(maxCustomersItem, 100)
        );

        CustomerSpawnerBlockEntity.InventoryDataMigrationResult result =
                CustomerSpawnerBlockEntity.migrateVersion0Inventory(
                        inventory,
                        paymentItem,
                        maxCustomersItem,
                        5
                );

        assertEquals(99, result.maxCustomers());
    }

    @Test
    void retainsClampedFallbackWhenNoMigrationDataExists() {
        CustomerSpawnerBlockEntity.InventoryDataMigrationResult result =
                CustomerSpawnerBlockEntity.migrateVersion0Inventory(
                        new PersistedContainer(9, () -> {}),
                        Items.EMERALD,
                        Items.REDSTONE,
                        0
                );

        assertFalse(result.changed());
        assertEquals(1, result.maxCustomers());
    }
}
