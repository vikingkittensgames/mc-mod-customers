package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplierSpawnerInventoryDataMigrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void convertsLegacyOffersIntoExplicitItemAndCostPairs() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(Items.BEEF, 32));
        inventory.setStackInSlot(1, new ItemStack(Items.EMERALD, 5));
        inventory.setStackInSlot(2, new ItemStack(Items.CARROT, 16));
        inventory.setStackInSlot(4, new ItemStack(Items.POTATO, 8));
        inventory.setStackInSlot(5, new ItemStack(Items.EMERALD, 2));

        SupplierSpawnerBlockEntity.migrateVersion0Inventory(inventory);

        assertStack(inventory, 0, Items.BEEF, 32);
        assertStack(inventory, 1, Items.EMERALD, 5);
        assertStack(inventory, 2, Items.CARROT, 16);
        assertStack(inventory, 3, Items.EMERALD, 1);
        assertStack(inventory, 4, Items.POTATO, 8);
        assertStack(inventory, 5, Items.EMERALD, 2);
        assertTrue(inventory.getStackInSlot(6).isEmpty());
        assertTrue(inventory.getStackInSlot(7).isEmpty());
    }

    @Test
    void preservesOnlyTheFirstFourLegacyOffersAndClearsNinthColumn() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(Items.BEEF));
        inventory.setStackInSlot(1, new ItemStack(Items.CARROT));
        inventory.setStackInSlot(2, new ItemStack(Items.POTATO));
        inventory.setStackInSlot(3, new ItemStack(Items.BREAD));
        inventory.setStackInSlot(4, new ItemStack(Items.APPLE));
        inventory.setStackInSlot(8, new ItemStack(Items.DIAMOND));

        SupplierSpawnerBlockEntity.migrateVersion0Inventory(inventory);

        assertStack(inventory, 0, Items.BEEF, 1);
        assertStack(inventory, 1, Items.EMERALD, 1);
        assertStack(inventory, 2, Items.CARROT, 1);
        assertStack(inventory, 3, Items.EMERALD, 1);
        assertStack(inventory, 4, Items.POTATO, 1);
        assertStack(inventory, 5, Items.EMERALD, 1);
        assertStack(inventory, 6, Items.BREAD, 1);
        assertStack(inventory, 7, Items.EMERALD, 1);
        assertTrue(inventory.getStackInSlot(8).isEmpty());
    }

    @Test
    void migratesEachInventoryRowIndependently() {
        ItemStackHandler inventory = new ItemStackHandler(18);
        inventory.setStackInSlot(7, new ItemStack(Items.BEEF));
        inventory.setStackInSlot(9, new ItemStack(Items.CARROT));
        inventory.setStackInSlot(10, new ItemStack(Items.EMERALD, 3));

        SupplierSpawnerBlockEntity.migrateVersion0Inventory(inventory);

        assertStack(inventory, 0, Items.BEEF, 1);
        assertStack(inventory, 1, Items.EMERALD, 1);
        assertStack(inventory, 9, Items.CARROT, 1);
        assertStack(inventory, 10, Items.EMERALD, 3);
    }

    private static void assertStack(
            ItemStackHandler inventory,
            int slot,
            net.minecraft.world.item.Item item,
            int count
    ) {
        ItemStack stack = inventory.getStackInSlot(slot);
        assertSame(item, stack.getItem());
        assertEquals(count, stack.getCount());
    }
}
