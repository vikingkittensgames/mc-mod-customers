package com.vikingkittens.mc.customers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import com.vikingkittens.mc.customers.advancements.ftb.CustomersFTB;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.NeoForgeRegistrationHelper;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.config.RecipeConditions;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxNeoForgeEvents;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterNeoForgeEvents;

@Mod(Customers.MODID)
public final class CustomersNeoForge {
    public CustomersNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Customers.initialize();
        CustomersFTB.initialize();
        CustomerPaymentBoxNeoForgeEvents.register(modEventBus);
        CustomerPickupCounterNeoForgeEvents.register(modEventBus);
        RecipeConditions.register(modEventBus);
        ((NeoForgeRegistrationHelper) CustomersServices.registration())
                .bind(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
