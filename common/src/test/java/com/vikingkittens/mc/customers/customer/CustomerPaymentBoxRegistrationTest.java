package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CustomerPaymentBoxRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralPaymentBoxRegistryHandles() {
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPaymentBox.getBlockName(variant);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("customers", name);

            assertInstanceOf(
                    CustomersRegistryEntry.class,
                    CustomerPaymentBox.BLOCKS.get(variant)
            );
            assertEquals(id, CustomerPaymentBox.BLOCKS.get(variant).getId());
            assertInstanceOf(CustomersRegistryEntry.class, CustomerPaymentBox.ITEMS.get(variant));
            assertEquals(id, CustomerPaymentBox.ITEMS.get(variant).getId());
        }

        assertInstanceOf(CustomersRegistryEntry.class, CustomerPaymentBox.BLOCK_ENTITY);
        assertEquals(
                ResourceLocation.parse("customers:customer_payment_box_block_entity"),
                CustomerPaymentBox.BLOCK_ENTITY.getId()
        );
    }
}
