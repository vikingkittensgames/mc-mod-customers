package com.vikingkittens.mc.customers.customer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

public final class CustomerPaymentBoxNeoForgeEvents {
    private CustomerPaymentBoxNeoForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CustomerPaymentBoxNeoForgeEvents::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                CustomerPaymentBox.BLOCK_ENTITY.get(),
                (paymentBox, direction) -> VanillaContainerWrapper.of(paymentBox)
        );
    }
}
