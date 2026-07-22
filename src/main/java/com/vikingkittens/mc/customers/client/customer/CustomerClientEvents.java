package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.special.CustomerZombieEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerWitchEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerStrayEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerSkeletonEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerHuskEntityRenderer;
import com.vikingkittens.mc.customers.client.customer.special.CustomerDrownedEntityRenderer;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import java.util.List;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    private static final float NAME_TAG_TEXT_SCALE = 0.025F;
    private static final float NAME_TAG_ITEM_GAP = 0.12F;

    public static void showCustomerShiftFinishedScreen(CustomerShiftFinishedPayload payload) {
        Minecraft.getInstance().setScreen(new CustomerShiftFinishedScreen(payload));
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Customer.CUSTOMER_VILLAGER.get(), VillagerRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_ZOMBIE.get(), CustomerZombieEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_SKELETON.get(), CustomerSkeletonEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_WITCH.get(), CustomerWitchEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_HUSK.get(), CustomerHuskEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_DROWNED.get(), CustomerDrownedEntityRenderer::new);
        event.registerEntityRenderer(Customer.CUSTOMER_STRAY.get(), CustomerStrayEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        // 1. Check if the entity is a villager (custom or vanilla)
        if (event.getEntity() instanceof CustomerVillagerEntity customer) {
            List<ItemStack> offerDisplayItems = customer.getState() == CustomerState.BUYING ? customer.getOfferDisplayItems() : List.of();
            if (!offerDisplayItems.isEmpty()) {
                Minecraft minecraft = Minecraft.getInstance();
                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource buffer = event.getMultiBufferSource();
                float nameTagOffset = isNameTagRendered(event, customer, minecraft)
                        ? minecraft.font.lineHeight * NAME_TAG_TEXT_SCALE + NAME_TAG_ITEM_GAP
                        : 0.0F;

                poseStack.pushPose();
                // Translate above the head
                poseStack.translate(0, customer.getBbHeight() + 0.25F, 0);
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

        return event.canRender().isDefault()
                && (customer.shouldShowName() || customer.hasCustomName() && customer == minecraft.getEntityRenderDispatcher().crosshairPickEntity);
    }
}


