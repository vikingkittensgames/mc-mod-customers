package com.vikingkittens.mc.customers.common;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;

import net.minecraft.world.entity.npc.villager.Villager;

import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.supplier.Supplier;

public final class CustomersEntityAttributes {
    private CustomersEntityAttributes() {}

    public static void initialize() {
        EntityAttributeRegistry.register(Customer.CUSTOMER_VILLAGER, Villager::createAttributes);
        EntityAttributeRegistry.register(Supplier.SUPPLIER_VILLAGER, Villager::createAttributes);
    }
}
