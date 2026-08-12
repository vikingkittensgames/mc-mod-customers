package com.vikingkittens.mc.customers.supplier;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.vikingkittens.mc.customers.Customers;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class SupplierCommandsForgeEvents {
    private SupplierCommandsForgeEvents() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        SupplierCommands.register(event.getDispatcher());
    }
}
