package com.vikingkittens.mc.customers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.NeoForgeRegistrationHelper;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.config.RecipeConditions;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxNeoForgeEvents;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterNeoForgeEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerNeoForgeEvents;
import com.vikingkittens.mc.customers.customer.data.CustomersData;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerNeoForgeEvents;

@Mod(Customers.MODID)
public final class CustomersNeoForge {
    public CustomersNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        CustomerSpawnerNeoForgeEvents.register(modEventBus);
        Customers.initialize();
        CustomerPaymentBoxNeoForgeEvents.register(modEventBus);
        CustomerPickupCounterNeoForgeEvents.register(modEventBus);
        SupplierSpawnerNeoForgeEvents.register(modEventBus);
        RecipeConditions.register(modEventBus);
        ((NeoForgeRegistrationHelper) CustomersServices.registration())
                .bind(modEventBus);
        modEventBus.addListener(CustomersData::gatherClientData);
        modEventBus.addListener(CustomersData::gatherData);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
