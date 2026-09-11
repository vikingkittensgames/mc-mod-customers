package com.vikingkittens.mc.customers.economy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

public final class EconomyForgeEvents {
    private EconomyForgeEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(EconomyForgeEvents::addReloadListener);
        MinecraftForge.EVENT_BUS.addListener(EconomyForgeEvents::serverStarted);
        MinecraftForge.EVENT_BUS.addListener(EconomyForgeEvents::serverTick);
    }

    private static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new EconomyDataReloadListener());
    }

    private static void serverStarted(ServerStartedEvent event) {
        Economy.serverStarted(event.getServer());
    }

    private static void serverTick(TickEvent.ServerTickEvent.Post event) {
        Economy.serverTick(event.getServer());
    }
}
