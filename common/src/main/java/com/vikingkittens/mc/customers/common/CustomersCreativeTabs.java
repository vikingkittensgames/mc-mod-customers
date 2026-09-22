package com.vikingkittens.mc.customers.common;

import dev.architectury.registry.CreativeTabRegistry;

import net.minecraft.world.item.CreativeModeTabs;

import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersCreativeTabs {
    private CustomersCreativeTabs() {}

    public static void initialize() {
        CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, CustomerSpawner.CUSTOMER_SPAWNER_ITEM);
        CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, SupplierSpawner.SUPPLIER_SPAWNER_ITEM);
        CustomerPaymentBox.ITEMS.values().forEach(item ->
                CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, item));
        CustomerPickupCounter.ITEMS.values().forEach(item ->
                CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, item));
        CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, CustomerLeaderboard.ITEM);
    }
}
