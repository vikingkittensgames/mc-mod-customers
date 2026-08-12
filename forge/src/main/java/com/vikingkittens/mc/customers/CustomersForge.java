package com.vikingkittens.mc.customers;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.ForgeConfigHelper;
import com.vikingkittens.mc.customers.compatability.ForgeNetworkHelper;
import com.vikingkittens.mc.customers.compatability.ForgeRegistrationHelper;
import com.vikingkittens.mc.customers.config.RecipeConditions;
import com.vikingkittens.mc.customers.customer.CustomerForgeEvents;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxForgeEvents;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterForgeEvents;
import com.vikingkittens.mc.customers.supplier.SupplierForgeEvents;

@Mod(Customers.MODID)
public final class CustomersForge {
    public CustomersForge(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        Customers.initialize();
        ForgeNetworkHelper.register();
        RecipeConditions.register(modEventBus);
        ((ForgeRegistrationHelper) CustomersServices.registration()).bind(modEventBus);
        CustomerPaymentBoxForgeEvents.register(modEventBus);
        CustomerForgeEvents.register(modEventBus);
        CustomerPickupCounterForgeEvents.register();
        SupplierForgeEvents.register(modEventBus);
        context.registerConfig(ModConfig.Type.COMMON, ForgeConfigHelper.SPEC);
    }
}
