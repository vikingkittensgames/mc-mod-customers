package com.vikingkittens.mc.customers.customer.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.vikingkittens.mc.customers.Customers;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class CustomersForgeDataEvents {
    private CustomersForgeDataEvents() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(
                event.includeClient(),
                new CustomerPickupCounterBlockStateProvider(output, event.getExistingFileHelper())
        );
        generator.addProvider(
                event.includeClient(),
                new CustomerPaymentBoxBlockStateProvider(output, event.getExistingFileHelper())
        );
        generator.addProvider(
                event.includeClient(),
                new CustomerLeaderboardBlockStateProvider(output, event.getExistingFileHelper())
        );
        generator.addProvider(
                event.includeServer(),
                new CustomerRecipeProvider(output, event.getLookupProvider())
        );
        generator.addProvider(
                event.includeServer(),
                new CustomerLootTableProvider(output, event.getLookupProvider())
        );
    }
}
