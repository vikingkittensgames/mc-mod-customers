package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;

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
        PersistedContainer inventory = new PersistedContainer(9, () -> {});
        inventory.setItem(0, new ItemStack(Items.BEEF, 32));
        inventory.setItem(1, new ItemStack(Items.EMERALD, 5));
        inventory.setItem(2, new ItemStack(Items.CARROT, 16));
        inventory.setItem(4, new ItemStack(Items.POTATO, 8));
        inventory.setItem(5, new ItemStack(Items.EMERALD, 2));

        SupplierSpawnerBlockEntity.migrateVersion0Inventory(inventory);

        assertStack(inventory, 0, Items.BEEF, 32);
        assertStack(inventory, 1, Items.EMERALD, 5);
        assertStack(inventory, 2, Items.CARROT, 16);
        assertStack(inventory, 3, Items.EMERALD, 1);
        assertStack(inventory, 4, Items.POTATO, 8);
        assertStack(inventory, 5, Items.EMERALD, 2);
        assertTrue(inventory.getItem(6).isEmpty());
        assertTrue(inventory.getItem(7).isEmpty());
    }

    @Test
    void preservesOnlyTheFirstFourLegacyOffersAndClearsNinthColumn() {
        PersistedContainer inventory = new PersistedContainer(9, () -> {});
        inventory.setItem(0, new ItemStack(Items.BEEF));
        inventory.setItem(1, new ItemStack(Items.CARROT));
        inventory.setItem(2, new ItemStack(Items.POTATO));
        inventory.setItem(3, new ItemStack(Items.BREAD));
        inventory.setItem(4, new ItemStack(Items.APPLE));
        inventory.setItem(8, new ItemStack(Items.DIAMOND));

        SupplierSpawnerBlockEntity.migrateVersion0Inventory(inventory);

        assertStack(inventory, 0, Items.BEEF, 1);
        assertStack(inventory, 1, Items.EMERALD, 1);
        assertStack(inventory, 2, Items.CARROT, 1);
        assertStack(inventory, 3, Items.EMERALD, 1);
        assertStack(inventory, 4, Items.POTATO, 1);
        assertStack(inventory, 5, Items.EMERALD, 1);
        assertStack(inventory, 6, Items.BREAD, 1);
        assertStack(inventory, 7, Items.EMERALD, 1);
        assertTrue(inventory.getItem(8).isEmpty());
    }

    @Test
    void migratesEachInventoryRowIndependently() {
        PersistedContainer inventory = new PersistedContainer(18, () -> {});
        inventory.setItem(7, new ItemStack(Items.BEEF));
        inventory.setItem(9, new ItemStack(Items.CARROT));
        inventory.setItem(10, new ItemStack(Items.EMERALD, 3));

        SupplierSpawnerBlockEntity.migrateVersion0Inventory(inventory);

        assertStack(inventory, 0, Items.BEEF, 1);
        assertStack(inventory, 1, Items.EMERALD, 1);
        assertStack(inventory, 9, Items.CARROT, 1);
        assertStack(inventory, 10, Items.EMERALD, 3);
    }

    private static void assertStack(
            PersistedContainer inventory,
            int slot,
            net.minecraft.world.item.Item item,
            int count
    ) {
        ItemStack stack = inventory.getItem(slot);
        assertSame(item, stack.getItem());
        assertEquals(count, stack.getCount());
    }
}
