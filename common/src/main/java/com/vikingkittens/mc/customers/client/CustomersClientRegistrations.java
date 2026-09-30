package com.vikingkittens.mc.customers.client;

import java.util.Collection;
import java.util.stream.Stream;

import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerAppearanceEntityRenderer;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;
import com.vikingkittens.mc.customers.client.appearance.monsters.MonsterCustomersVillagerClientAppearance;
import com.vikingkittens.mc.customers.client.appearance.skins.SkinCustomersVillagerClientAppearanceProvider;
import com.vikingkittens.mc.customers.client.customer.CustomerPickupCounterBlockEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.CustomerSpawnerBlockScreen;
import com.vikingkittens.mc.customers.client.customer.CustomerSpawnerSnapshotManager;
import com.vikingkittens.mc.customers.client.customer.CustomerVillagerEntityRenderer;
import com.vikingkittens.mc.customers.client.supplier.SupplierSpawnerBlockScreen;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersClientRegistrations {
    private CustomersClientRegistrations() {}

    public static void initialize() {
        initializeRuntime();
        registerScreens();
        registerRenderers();
    }

    public static void initializeRuntime() {
        registerAppearances();
        registerRenderTypes(CustomerPaymentBox.BLOCKS.values(), CustomerPickupCounter.BLOCKS.values());
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> CustomerSpawnerSnapshotManager.clear());
    }

    static void registerRenderTypes(
            Collection<? extends java.util.function.Supplier<? extends Block>> paymentBoxes,
            Collection<? extends java.util.function.Supplier<? extends Block>> pickupCounters
    ) {
        Block[] blocks = Stream.concat(paymentBoxes.stream(), pickupCounters.stream())
                .map(java.util.function.Supplier::get)
                .toArray(Block[]::new);
        RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT, blocks);
    }

    private static void registerAppearances() {
        CustomersVillagerClientAppearances.register(
                MonsterCustomersVillagerAppearance.ID,
                MonsterCustomersVillagerClientAppearance::new
        );
        CustomersVillagerClientAppearances.registerProvider(
                SkinCustomersVillagerClientAppearanceProvider.INSTANCE
        );
    }

    private static void registerScreens() {
        MenuScreenRegistry.registerScreenFactory(
                CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(),
                CustomerSpawnerBlockScreen::new
        );
        MenuScreenRegistry.registerScreenFactory(
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
