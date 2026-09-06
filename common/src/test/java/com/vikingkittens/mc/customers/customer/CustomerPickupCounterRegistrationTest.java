package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CustomerPickupCounterRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralPickupCounterRegistryHandles() {
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPickupCounter.getBlockName(variant);
            Identifier id = Identifier.fromNamespaceAndPath("customers", name);

            assertInstanceOf(CustomersRegistryEntry.class, CustomerPickupCounter.BLOCKS.get(variant));
            assertEquals(id, CustomerPickupCounter.BLOCKS.get(variant).getId());
            assertInstanceOf(CustomersRegistryEntry.class, CustomerPickupCounter.ITEMS.get(variant));
            assertEquals(id, CustomerPickupCounter.ITEMS.get(variant).getId());
        }

        assertInstanceOf(CustomersRegistryEntry.class, CustomerPickupCounter.BLOCK_ENTITY);
        assertEquals(
                Identifier.parse("customers:customer_pickup_counter"),
                CustomerPickupCounter.BLOCK_ENTITY.getId()
        );
    }
}
