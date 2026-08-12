package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SupplierSpawnerRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralSupplierSpawnerRegistryHandles() {
        assertInstanceOf(
                CustomersRegistryEntry.class,
                SupplierSpawner.SUPPLIER_SPAWNER_BLOCK
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                SupplierSpawner.SUPPLIER_SPAWNER_ENTITY
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                SupplierSpawner.SUPPLIER_SPAWNER_ITEM
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                SupplierSpawner.SUPPLIER_SPAWNER_MENU
        );
        assertEquals(
                ResourceLocation.parse("customers:supplier_spawner_block"),
                SupplierSpawner.SUPPLIER_SPAWNER_BLOCK.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:supplier_spawner_block_entity"),
                SupplierSpawner.SUPPLIER_SPAWNER_ENTITY.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:supplier_spawner_block"),
                SupplierSpawner.SUPPLIER_SPAWNER_ITEM.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:supplier_spawner"),
                SupplierSpawner.SUPPLIER_SPAWNER_MENU.getId()
        );
    }
}
