package com.vikingkittens.mc.customers.customer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.vikingkittens.mc.customers.compatability.NeoForgeItemInsertionTarget;

public final class CustomerPickupCounterNeoForgeEvents {
    private CustomerPickupCounterNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CustomerPickupCounterNeoForgeEvents::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CustomerPickupCounter.BLOCK_ENTITY.get(),
                (counter, direction) -> new NeoForgeItemInsertionTarget(counter.getItemInsertionTarget())
        );
    }
}
