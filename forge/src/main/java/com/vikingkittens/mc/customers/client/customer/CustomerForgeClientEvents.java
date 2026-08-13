package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerAppearanceEntityRenderer;
import com.vikingkittens.mc.customers.client.supplier.SupplierSpawnerBlockScreen;
import com.vikingkittens.mc.customers.compatability.ForgeNetworkHelper;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class CustomerForgeClientEvents {
    private CustomerForgeClientEvents() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ForgeNetworkHelper.register();
        event.enqueueWork(() -> {
            MenuScreens.register(CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(), CustomerSpawnerBlockScreen::new);
            MenuScreens.register(SupplierSpawner.SUPPLIER_SPAWNER_MENU.get(), SupplierSpawnerBlockScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                CustomerVillagerEntityRenderer.MODEL_LAYER,
                CustomerVillagerEntityRenderer.Model::createBodyLayer
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
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
    }

}
