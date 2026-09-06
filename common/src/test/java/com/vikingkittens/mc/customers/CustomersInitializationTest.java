package com.vikingkittens.mc.customers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSeat;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomersInitializationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void initializesLoaderNeutralRegistrations() {
        Customers.initialize();

        assertEquals(
                Identifier.parse("customers:default"),
                CustomersVillagerAppearances.DEFAULT_APPEARANCE.getId()
        );
        assertEquals(
                Identifier.parse("customers:monsters"),
                MonsterCustomersVillagerAppearanceEvents.APPEARANCE.getId()
        );
        assertEquals(
                Identifier.parse("customers:customer_payment_box_block_entity"),
                CustomerPaymentBox.BLOCK_ENTITY.getId()
        );
        assertEquals(Identifier.parse("customers:customer_seat"), CustomerSeat.ENTITY_TYPE.getId());
        assertEquals(Identifier.parse("customers:customer_villager"), Customer.CUSTOMER_VILLAGER.getId());
        assertEquals(
                Identifier.parse("customers:customer_spawner_block"),
                CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.getId()
        );
        assertEquals(
                Identifier.parse("customers:customer_pickup_counter"),
                CustomerPickupCounter.BLOCK_ENTITY.getId()
        );
        assertEquals(Identifier.parse("customers:supplier_villager"), Supplier.SUPPLIER_VILLAGER.getId());
        assertEquals(
                Identifier.parse("customers:supplier_spawner_block"),
                SupplierSpawner.SUPPLIER_SPAWNER_BLOCK.getId()
        );
    }
}
