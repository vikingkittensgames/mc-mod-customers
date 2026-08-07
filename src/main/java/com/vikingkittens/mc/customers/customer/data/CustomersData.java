package com.vikingkittens.mc.customers.customer.data;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class CustomersData {
    private CustomersData() {
    }

    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(CustomerPickupCounterBlockStateProvider::new);
    }

    public static void gatherServerData(GatherDataEvent.Server event) {
        event.createProvider(CustomerPickupCounterRecipeProvider::new);
        event.createProvider(CustomerPickupCounterLootTableProvider::new);
    }
}
