package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerAppearanceEntityRenderer;
import com.vikingkittens.mc.customers.client.supplier.SupplierSpawnerBlockScreen;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(),
                CustomerSpawnerBlockScreen::new
        );
        event.register(
                SupplierSpawner.SUPPLIER_SPAWNER_MENU.get(),
                SupplierSpawnerBlockScreen::new
        );
    }

    /**
     * Renders customer request groups for customer spawner boss bars.
     *
     * @param event boss bar rendering event
     */
    @SubscribeEvent
    public static void onBossEventProgress(
            CustomizeGuiOverlayEvent.BossEventProgress event
    ) {
        CustomerSpawnerSnapshotManager.findByBossEvent(
                event.getBossEvent().getId()
        ).ifPresent(snapshot -> {
            event.setCanceled(true);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null
                    || !CustomerBossBarRenderer.isInRange(
                            minecraft.player,
                            snapshot.spawnerPos()
                    )) {
                return;
            }
            event.setIncrement(CustomerBossBarRenderer.render(
                    event.getGuiGraphics(),
                    event.getBossEvent(),
                    snapshot,
                    event.getX(),
                    event.getY(),
                    event.getIncrement()
            ));
        });
    }

    /**
     * Clears synchronized customer spawner data when leaving a world.
     *
     * @param event client logout event
     */
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CustomerSpawnerSnapshotManager.clear();
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {
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
                context ->
                        new CustomersVillagerAppearanceEntityRenderer<>(
                                context,
                                new CustomerVillagerEntityRenderer(context)
                        )
        );
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        CustomerVillagerEntity customer = CustomerWantedItemsRenderer.getRenderedCustomer(event.getEntity());
        CustomerWantedItemsRenderer.render(
                event.getEntity(),
                customer != null && isNameTagRendered(event, customer, Minecraft.getInstance()),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }

    private static boolean isNameTagRendered(RenderNameTagEvent event, CustomerVillagerEntity customer, Minecraft minecraft) {
        if (event.getContent() == null || event.getContent().getString().isBlank()) {
            return false;
        }

        if (!ClientHooks.isNameplateInRenderDistance(customer, minecraft.getEntityRenderDispatcher().distanceToSqr(customer))) {
            return false;
        }

        if (event.canRender().isTrue()) {
            return true;
        }

        if (!event.canRender().isDefault()) {
            return false;
        }

        boolean sourceVisibility = customer.shouldShowName()
                || customer.hasCustomName()
                        && customer
                                == minecraft.getEntityRenderDispatcher()
                                        .crosshairPickEntity;
        return CustomerWantedItemsRenderer.getDefaultNameTagVisibility(
                event.getEntity(),
                sourceVisibility
        );
    }
}
