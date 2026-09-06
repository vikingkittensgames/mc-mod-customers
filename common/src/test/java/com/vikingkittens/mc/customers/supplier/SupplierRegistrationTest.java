package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SupplierRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralSupplierRegistryHandles() {
        assertInstanceOf(
                CustomersRegistryEntry.class,
                Supplier.SUPPLIER_VILLAGER
        );
        assertInstanceOf(
                CustomersRegistryEntry.class,
                Supplier.SUPPLIER_PROFESSION
        );
        assertEquals(
                Identifier.parse("customers:supplier"),
                Supplier.SUPPLIER_PROFESSION.getId()
        );
    }
}
