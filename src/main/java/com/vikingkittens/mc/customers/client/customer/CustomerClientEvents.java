package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;
import com.vikingkittens.mc.customers.client.supplier.SupplierSpawnerBlockScreen;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    private static final int MAX_OVERHEAD_ITEMS = 3;
    private static final float NAME_TAG_TEXT_SCALE = 0.025F;
    private static final float NAME_TAG_ITEM_GAP = 0.12F;

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

    public static void showCustomerShiftFinishedScreen(CustomerShiftFinishedPayload payload) {
        Minecraft.getInstance().setScreen(new CustomerShiftFinishedScreen(payload));
    }

    public static void showCounterMarkers(CustomerCounterMarkersPayload payload) {
        CustomerCounterMarkerManager.show(
                payload.markers(),
                payload.surroundingPositions(),
                Util.getMillis()
        );
    }

    public static void updateCustomerSpawnerSnapshot(
            CustomerSpawnerSnapshotPayload payload
    ) {
        payload.snapshot().ifPresentOrElse(
                CustomerSpawnerSnapshotManager::replace,
                () -> CustomerSpawnerSnapshotManager.remove(
                        payload.spawnerPos()
                )
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
        CustomerVillagerEntity customer =
                getRenderedCustomer(event.getEntity());
        if (customer != null) {
            List<ItemStack> offerDisplayItems =
                    customer.getState() == CustomerState.BUYING
                            ? CustomerSpawnerSnapshotManager.findOfferCostItems(
                                    customer.getUUID(),
                                    MAX_OVERHEAD_ITEMS
                            )
                            : List.of();
            if (!offerDisplayItems.isEmpty()) {
                Minecraft minecraft = Minecraft.getInstance();
                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource buffer = event.getMultiBufferSource();
                float nameTagOffset = isNameTagRendered(event, customer, minecraft)
                        ? minecraft.font.lineHeight * NAME_TAG_TEXT_SCALE + NAME_TAG_ITEM_GAP
                        : 0.0F;

                poseStack.pushPose();
                // Translate above the head
                float offset = 0.25F;
                float appearanceNameTagOffset =
                        CustomersVillagerClientAppearances
                                .getNameTagOffset(customer);
                if (appearanceNameTagOffset != 0.0F) {
                    offset += appearanceNameTagOffset - nameTagOffset;
                }
                poseStack.translate(0, customer.getBbHeight() + offset, 0);
                poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
                poseStack.translate(0.0F, nameTagOffset, 0.0F);

                float iconSpacing = 0.5F;
                float startX = -((offerDisplayItems.size() - 1) * iconSpacing) / 2.0F;
                int index = 0;
                for (ItemStack costA : offerDisplayItems) {
                    poseStack.pushPose();
                    poseStack.translate(startX + index * iconSpacing, 0, 0);
                    minecraft.getItemRenderer().renderStatic(
                            costA,
                            ItemDisplayContext.GROUND,
                            event.getPackedLight(),
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            buffer,
                            customer.level(),
                            0
                    );
                    poseStack.popPose();

                    index++;
                }
                poseStack.popPose();
            }
        }
    }

    static @Nullable CustomerVillagerEntity getRenderedCustomer(
            Entity renderedEntity
    ) {
        if (renderedEntity instanceof CustomerVillagerEntity customer) {
            return customer;
        }
        if (
                renderedEntity instanceof CustomersVillagerRenderProxy proxy
                        && proxy.getCustomersVillagerSource()
                                instanceof CustomerVillagerEntity customer
        ) {
            return customer;
        }
        return null;
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
        return getDefaultNameTagVisibility(
                event.getEntity(),
                sourceVisibility
        );
    }

    static boolean getDefaultNameTagVisibility(
            Entity renderedEntity,
            boolean sourceVisibility
    ) {
        return renderedEntity instanceof CustomersVillagerRenderProxy proxy
                ? proxy.shouldRenderNameTag()
                : sourceVisibility;
    }
}
