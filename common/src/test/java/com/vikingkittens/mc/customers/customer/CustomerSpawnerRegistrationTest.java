package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CustomerSpawnerRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralCustomerSpawnerRegistryHandles() {
        assertInstanceOf(
                CustomersRegistryEntry.class,
                CustomerSpawner.CUSTOMER_SPAWNER_BLOCK
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                CustomerSpawner.CUSTOMER_SPAWNER_ENTITY
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                CustomerSpawner.CUSTOMER_SPAWNER_ITEM
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                CustomerSpawner.CUSTOMER_SPAWNER_MENU
        );
        assertEquals(
                ResourceLocation.parse("customers:customer_spawner_block"),
                CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:customer_spawner_block_entity"),
                CustomerSpawner.CUSTOMER_SPAWNER_ENTITY.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:customer_spawner_block"),
                CustomerSpawner.CUSTOMER_SPAWNER_ITEM.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:customer_spawner"),
                CustomerSpawner.CUSTOMER_SPAWNER_MENU.getId()
        );
    }
}
