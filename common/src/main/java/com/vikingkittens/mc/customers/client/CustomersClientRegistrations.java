package com.vikingkittens.mc.customers.client;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;

import com.vikingkittens.mc.customers.appearance.mca.McaCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.mca.McaCustomersVillagerMod;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerAppearanceEntityRenderer;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;
import com.vikingkittens.mc.customers.client.appearance.mca.McaCustomersVillagerClientAppearance;
import com.vikingkittens.mc.customers.client.appearance.monsters.MonsterCustomersVillagerClientAppearance;
import com.vikingkittens.mc.customers.client.appearance.skins.SkinCustomersVillagerClientAppearanceProvider;
import com.vikingkittens.mc.customers.client.customer.CustomerPickupCounterBlockEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomerSpawnerBlockScreen;
import com.vikingkittens.mc.customers.client.customer.CustomerSpawnerSnapshotManager;
import com.vikingkittens.mc.customers.client.customer.CustomerVillagerEntityRenderer;
import com.vikingkittens.mc.customers.client.supplier.SupplierSpawnerBlockScreen;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersClientRegistrations {
    private CustomersClientRegistrations() {}

    public static void initialize() {
        registerAppearances();
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> CustomerSpawnerSnapshotManager.clear());
        registerScreens();
        registerRenderers();
    }

    private static void registerAppearances() {
        if (McaCustomersVillagerMod.isSupported()) {
            CustomersVillagerClientAppearances.register(
                    McaCustomersVillagerAppearance.ID,
                    McaCustomersVillagerClientAppearance::new
            );
        }
        CustomersVillagerClientAppearances.register(
                MonsterCustomersVillagerAppearance.ID,
                MonsterCustomersVillagerClientAppearance::new
        );
        CustomersVillagerClientAppearances.registerProvider(
                SkinCustomersVillagerClientAppearanceProvider.INSTANCE
        );
    }

    private static void registerScreens() {
        MenuRegistry.registerScreenFactory(
                CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(),
                CustomerSpawnerBlockScreen::new
        );
        MenuRegistry.registerScreenFactory(
                SupplierSpawner.SUPPLIER_SPAWNER_MENU.get(),
                SupplierSpawnerBlockScreen::new
        );
    }

    private static void registerRenderers() {
        EntityModelLayerRegistry.register(
                CustomerVillagerEntityRenderer.MODEL_LAYER,
                CustomerVillagerEntityRenderer.Model::createBodyLayer
        );
        BlockEntityRendererRegistry.register(
                CustomerPickupCounter.BLOCK_ENTITY.get(),
                CustomerPickupCounterBlockEntityRenderer::new
        );
        EntityRendererRegistry.register(Customer.CUSTOMER_SEAT, NoopRenderer::new);
        EntityRendererRegistry.register(
                Customer.CUSTOMER_VILLAGER,
                context -> new CustomersVillagerAppearanceEntityRenderer<>(
                        context,
                        new CustomerVillagerEntityRenderer(context)
                )
        );
        EntityRendererRegistry.register(
                Supplier.SUPPLIER_VILLAGER,
                context -> new CustomersVillagerAppearanceEntityRenderer<>(
                        context,
                        new VillagerRenderer(context)
                )
        );
    }
}
