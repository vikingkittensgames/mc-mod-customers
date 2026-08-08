package com.vikingkittens.mc.customers.appearance;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class CustomersVillagerAppearanceEvents {
    private CustomersVillagerAppearanceEvents() {}

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(CustomersVillagerAppearance.APPEARANCE_REGISTRY);
    }
}
