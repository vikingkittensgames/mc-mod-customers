package com.vikingkittens.mc.customers.supplier;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID)
public final class SupplierCommandsNeoForgeEvents {
    private SupplierCommandsNeoForgeEvents() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        SupplierCommands.register(event.getDispatcher());
    }
}
