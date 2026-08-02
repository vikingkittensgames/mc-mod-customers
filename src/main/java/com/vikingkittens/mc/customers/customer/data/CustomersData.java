package com.vikingkittens.mc.customers.customer.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class CustomersData {
    private CustomersData() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(
                event.includeClient(),
                new CustomerPickupCounterBlockStateProvider(
                        output,
                        event.getExistingFileHelper()
                )
        );
        generator.addProvider(
                event.includeServer(),
                new CustomerPickupCounterRecipeProvider(
                        output,
                        event.getLookupProvider()
                )
        );
        generator.addProvider(
                event.includeServer(),
                new CustomerPickupCounterLootTableProvider(
                        output,
                        event.getLookupProvider()
                )
        );    }
}
