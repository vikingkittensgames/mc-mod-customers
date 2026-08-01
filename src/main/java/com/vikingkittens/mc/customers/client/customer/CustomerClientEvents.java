package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.special.CustomerDrownedEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerHuskEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerSkeletonEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerStrayEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerWitchEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerZombieEntityRenderer;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.customer.special.CustomerWitchEntity;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    private static final float NAME_TAG_TEXT_SCALE = 0.025F;
    private static final float NAME_TAG_ITEM_GAP = 0.12F;
    static final ContextKey<CustomerVillagerEntity> CUSTOMER_RENDER_DATA =
            new ContextKey<>(
                    Identifier.fromNamespaceAndPath(
                            Customers.MODID,
                            "customer_name_tag"
                    )
            );
    public static final ContextKey<Boolean> CUSTOMER_SITTING_RENDER_DATA =
            new ContextKey<>(
                    Identifier.fromNamespaceAndPath(
                            Customers.MODID,
                            "customer_sitting"
                    )
            );

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
        event.registerEntityRenderer(Customer.CUSTOMER_SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(
                Customer.CUSTOMER_VILLAGER.get(),
                CustomerVillagerEntityRenderer::new
        );
        event.registerEntityRenderer(Customer.CUSTOMER_ZOMBIE.get(), CustomerZombieEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_SKELETON.get(), CustomerSkeletonEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_WITCH.get(), CustomerWitchEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_HUSK.get(), CustomerHuskEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_DROWNED.get(), CustomerDrownedEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_STRAY.get(), CustomerStrayEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerRenderStateModifiers(
            RegisterRenderStateModifiersEvent event
    ) {
        event.registerEntityModifier(
                CustomerVillagerEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
        event.registerEntityModifier(
                CustomerZombieEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
        event.registerEntityModifier(
                CustomerSkeletonEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
        event.registerEntityModifier(
                CustomerWitchEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
        event.registerEntityModifier(
                CustomerHuskEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
        event.registerEntityModifier(
                CustomerDrownedEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
        event.registerEntityModifier(
                CustomerStrayEntityRenderer.class,
                CustomerClientEvents::extractCustomerRenderData
        );
    }

    /**
     * Stores customer data after NeoForge resets the reusable render state.
     */
    static void extractCustomerRenderData(
            CustomerVillagerEntity customer,
            EntityRenderState renderState
    ) {
        renderState.setRenderData(CUSTOMER_RENDER_DATA, customer);
        renderState.setRenderData(
                CUSTOMER_SITTING_RENDER_DATA,
                customer.isCustomerSitting()
        );
    }
    @SubscribeEvent
    public static void onCanRenderNameTag(RenderNameTagEvent.CanRender event) {
        if (event.getEntity() instanceof CustomerVillagerEntity customer) {
            List<ItemStack> offerDisplayItems = customer.getState() == CustomerState.BUYING ? customer.getOfferDisplayItems() : List.of();
            if (!offerDisplayItems.isEmpty()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (!isNameTagRendered(event, customer, minecraft)) {
                    event.setContent(Component.empty());
                }
                event.setCanRender(TriState.TRUE);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent.DoRender event) {
        CustomerVillagerEntity customer =
                event.getEntityRenderState().getRenderData(CUSTOMER_RENDER_DATA);
        if (customer == null) {
            return;
        }

        List<ItemStack> offerDisplayItems =
                customer.getState() == CustomerState.BUYING
                        ? customer.getOfferDisplayItems()
                        : List.of();
        if (offerDisplayItems.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector nodeCollector = event.getSubmitNodeCollector();
        boolean nameTagRendered = !event.getContent().getString().isBlank();
        float nameTagOffset = nameTagRendered
                ? minecraft.font.lineHeight * NAME_TAG_TEXT_SCALE + NAME_TAG_ITEM_GAP
                : 0.0F;

        poseStack.pushPose();
        float offset = 0.25F;
        if (customer instanceof CustomerWitchEntity) {
            offset += 0.40F - nameTagOffset;
        }
        poseStack.translate(0, customer.getBbHeight() + offset, 0);
        poseStack.mulPose(event.getCameraRenderState().orientation);
        poseStack.translate(0.0F, nameTagOffset, 0.0F);

        float iconSpacing = 0.5F;
        float startX = -((offerDisplayItems.size() - 1) * iconSpacing) / 2.0F;
        int index = 0;
        for (ItemStack costA : offerDisplayItems) {
            poseStack.pushPose();
            poseStack.translate(startX + index * iconSpacing, 0, 0);
            ItemStackRenderState itemRenderState = new ItemStackRenderState();
            minecraft.getItemModelResolver().updateForLiving(
                    itemRenderState,
                    costA,
                    ItemDisplayContext.GROUND,
                    customer
            );
            itemRenderState.submit(
                    poseStack,
                    nodeCollector,
                    event.getEntityRenderState().lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    event.getEntityRenderState().outlineColor
            );
            poseStack.popPose();
            index++;
        }
        poseStack.popPose();

        if (!nameTagRendered) {
            event.setCanceled(true);
        }
    }

    private static boolean isNameTagRendered(
            RenderNameTagEvent.CanRender event,
            CustomerVillagerEntity customer,
            Minecraft minecraft
    ) {
        if (event.getContent() == null || event.getContent().getString().isBlank()) {
            return false;
        }

        if (!ClientHooks.isNameplateInRenderDistance(customer, minecraft.getEntityRenderDispatcher().distanceToSqr(customer))) {
            return false;
        }

        if (event.canRender().isTrue()) {
            return true;
        }

        return event.canRender().isDefault()
                && (customer.shouldShowName() || customer.hasCustomName() && customer == minecraft.getEntityRenderDispatcher().crosshairPickEntity);
    }
}
