package com.vikingkittens.mc.customers.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.advancements.ftb.CustomersFTB;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

public final class CustomersFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CustomersFabricCompatibility.config();
        CustomersFabricRecipeConditions.initialize();
        Customers.initialize();
        CustomersFTB.initialize();
        ItemStorage.SIDED.registerForBlockEntity(
                (counter, direction) ->
                        new CustomerPickupCounterFabricStorage(
                                counter.getItemInsertionTarget()
                        ),
                CustomerPickupCounter.BLOCK_ENTITY.get()
        );
        CustomersFabricEvents.initialize();
    }
}
