package com.vikingkittens.mc.customers;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.config.RecipeConditions;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.customer.data.CustomersData;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

@Mod(Customers.MODID)
public class Customers {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "customers";

    public Customers(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        CustomerSpawner.register(modEventBus);
        CustomerPickupCounter.register(modEventBus);
        Customer.register(modEventBus);
        SupplierSpawner.register(modEventBus);
        Supplier.register(modEventBus);
        RecipeConditions.register(modEventBus);
        modEventBus.addListener(CustomersData::gatherClientData);
        modEventBus.addListener(CustomersData::gatherServerData);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
