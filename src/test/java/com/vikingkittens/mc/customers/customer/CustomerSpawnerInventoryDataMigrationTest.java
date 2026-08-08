package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

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
        ItemStackHandler inventory = new ItemStackHandler(18);
        inventory.setStackInSlot(
                2,
                new ItemStack(paymentItem, 3)
        );
        inventory.setStackInSlot(
                8,
                new ItemStack(existingCostItem, 4)
        );
        inventory.setStackInSlot(
                10,
                new ItemStack(maxCustomersItem, 12)
        );
        inventory.setStackInSlot(
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
                inventory.getStackInSlot(8).getItem()
        );
        assertEquals(3, inventory.getStackInSlot(8).getCount());
        assertSame(
                existingCostItem,
                inventory.getStackInSlot(2).getItem()
        );
        assertTrue(inventory.getStackInSlot(10).isEmpty());
        assertTrue(inventory.getStackInSlot(11).isEmpty());
    }

    @Test
    void clampsMigratedMaximumCustomersToUiRange() {
        Item paymentItem = Items.EMERALD;
        Item maxCustomersItem = Items.REDSTONE;
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(
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
                        new ItemStackHandler(9),
                        Items.EMERALD,
                        Items.REDSTONE,
                        0
                );

        assertFalse(result.changed());
        assertEquals(1, result.maxCustomers());
    }
}
