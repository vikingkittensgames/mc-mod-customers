package com.vikingkittens.mc.customers.client;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.CustomersClientRegistrations;
import com.vikingkittens.mc.customers.client.advancements.ftb.CustomersFTBClient;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerAppearanceEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomerPickupCounterBlockEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomerSpawnerBlockScreen;
import com.vikingkittens.mc.customers.client.customer.CustomerVillagerEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomersClientNetworking;
import com.vikingkittens.mc.customers.client.supplier.SupplierSpawnerBlockScreen;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Customers.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomersClient {
    public CustomersClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CustomersClientNetworking.initialize();
            CustomersClientRegistrations.initializeRuntime();
            CustomersFTBClient.initialize();
        });
    }

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                CustomerVillagerEntityRenderer.MODEL_LAYER,
                CustomerVillagerEntityRenderer.Model::createBodyLayer
        );
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                CustomerPickupCounter.BLOCK_ENTITY.get(),
                CustomerPickupCounterBlockEntityRenderer::new
        );
        event.registerEntityRenderer(Customer.CUSTOMER_SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(
                Customer.CUSTOMER_VILLAGER.get(),
                context -> new CustomersVillagerAppearanceEntityRenderer<>(
                        context,
                        new CustomerVillagerEntityRenderer(context)
                )
        );
        event.registerEntityRenderer(
                Supplier.SUPPLIER_VILLAGER.get(),
                context -> new CustomersVillagerAppearanceEntityRenderer<>(
                        context,
                        new VillagerRenderer(context)
                )
        );
    }

    @SubscribeEvent
    static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(), CustomerSpawnerBlockScreen::new);
        event.register(SupplierSpawner.SUPPLIER_SPAWNER_MENU.get(), SupplierSpawnerBlockScreen::new);
    }
}
