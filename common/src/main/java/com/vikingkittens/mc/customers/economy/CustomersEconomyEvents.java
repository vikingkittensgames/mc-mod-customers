package com.vikingkittens.mc.customers.economy;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.ReloadListenerRegistry;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import com.vikingkittens.mc.customers.Customers;

public final class CustomersEconomyEvents {
    private CustomersEconomyEvents() {}

    public static void initialize() {
        ReloadListenerRegistry.register(
                PackType.SERVER_DATA,
                new EconomyDataReloadListener(),
                Identifier.fromNamespaceAndPath(Customers.MODID, "economy")
        );
        LifecycleEvent.SERVER_STARTED.register(Economy::serverStarted);
        TickEvent.SERVER_POST.register(Economy::serverTick);
    }
}
