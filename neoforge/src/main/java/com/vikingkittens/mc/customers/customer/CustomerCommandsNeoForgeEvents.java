package com.vikingkittens.mc.customers.customer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID)
public final class CustomerCommandsNeoForgeEvents {
    private CustomerCommandsNeoForgeEvents() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CustomerCommands.register(event.getDispatcher());
    }
}
