package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CustomerRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralCustomerRegistryHandles() {
        assertInstanceOf(
                CustomersRegistryEntry.class,
                Customer.CUSTOMER_VILLAGER
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                Customer.CUSTOMER_SEAT
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                Customer.CUSTOMER_PROFESSION
        );
        assertEquals(
                ResourceLocation.parse("customers:customer"),
                Customer.CUSTOMER_PROFESSION.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:customer_impatient"),
                Customer.CUSTOMER_IMPATIENT_PROFESSION.getId()
        );
        assertEquals(
                ResourceLocation.parse("customers:customer_casual"),
                Customer.CUSTOMER_CASUAL_PROFESSION.getId()
        );
    }
}
