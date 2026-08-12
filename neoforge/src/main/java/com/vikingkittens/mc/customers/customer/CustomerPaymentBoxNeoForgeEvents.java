package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class CustomerPaymentBoxNeoForgeEvents {
    private CustomerPaymentBoxNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CustomerPaymentBoxNeoForgeEvents::addCreative);
        modEventBus.addListener(CustomerPaymentBoxNeoForgeEvents::registerCapabilities);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            CustomerPaymentBox.ITEMS.values().forEach(item -> event.accept(item.get()));
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                CustomerPaymentBox.BLOCK_ENTITY.get(),
                (paymentBox, direction) -> new InvWrapper(paymentBox)
        );
    }
}
