package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class CustomerSpawnerNeoForgeEvents {
    private CustomerSpawnerNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CustomerSpawnerNeoForgeEvents::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get());
        }
    }
}
