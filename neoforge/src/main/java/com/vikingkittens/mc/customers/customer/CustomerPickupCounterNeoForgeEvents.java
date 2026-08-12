package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import com.vikingkittens.mc.customers.compatability.NeoForgeItemInsertionTarget;

public final class CustomerPickupCounterNeoForgeEvents {
    private CustomerPickupCounterNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CustomerPickupCounterNeoForgeEvents::addCreative);
        modEventBus.addListener(CustomerPickupCounterNeoForgeEvents::registerCapabilities);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            CustomerPickupCounter.ITEMS.values().forEach(item -> event.accept(item.get()));
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CustomerPickupCounter.BLOCK_ENTITY.get(),
                (counter, direction) -> new NeoForgeItemInsertionTarget(counter.getItemInsertionTarget())
        );
    }
}
