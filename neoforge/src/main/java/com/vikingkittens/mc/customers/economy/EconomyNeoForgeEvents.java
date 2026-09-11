package com.vikingkittens.mc.customers.economy;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EconomyNeoForgeEvents {
    private EconomyNeoForgeEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EconomyNeoForgeEvents::addReloadListener);
        NeoForge.EVENT_BUS.addListener(EconomyNeoForgeEvents::serverStarted);
        NeoForge.EVENT_BUS.addListener(EconomyNeoForgeEvents::serverTick);
    }

    private static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new EconomyDataReloadListener());
    }

    private static void serverStarted(ServerStartedEvent event) {
        Economy.serverStarted(event.getServer());
    }

    private static void serverTick(ServerTickEvent.Post event) {
        Economy.serverTick(event.getServer());
    }
}
