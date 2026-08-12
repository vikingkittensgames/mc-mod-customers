package com.vikingkittens.mc.customers.customer;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.vikingkittens.mc.customers.Customers;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class CustomerCommandsForgeEvents {
    private CustomerCommandsForgeEvents() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CustomerCommands.register(event.getDispatcher());
    }
}
