package com.vikingkittens.mc.customers.customer.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class CustomersData {
    private CustomersData() {
    }

    public static void gatherClientData(GatherDataEvent.Client event) {
        event.addProvider(new CustomerOverlayBlockModelProvider(event.getGenerator().getPackOutput()));
    }

    public static void gatherData(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        event.addProvider(new CustomerRecipeProvider(output, event.getLookupProvider()));
        event.addProvider(new CustomerLootTableProvider(output, event.getLookupProvider()));
    }
}
