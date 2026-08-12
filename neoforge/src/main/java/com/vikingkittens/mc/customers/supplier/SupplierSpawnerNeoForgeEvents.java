package com.vikingkittens.mc.customers.supplier;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class SupplierSpawnerNeoForgeEvents {
    private SupplierSpawnerNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(SupplierSpawnerNeoForgeEvents::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get());
        }
    }
}
