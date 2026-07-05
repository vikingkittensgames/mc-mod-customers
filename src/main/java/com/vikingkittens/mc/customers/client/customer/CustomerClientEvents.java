package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.Customer;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

import java.util.List;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Customer.CUSTOMER_VILLAGER.get(), VillagerRenderer::new);
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        // 1. Check if the entity is a villager (custom or vanilla)
        if (event.getEntity() instanceof CustomerVillagerEntity customer) {
            List<ItemStack> offerDisplayItems = customer.getState() == CustomerState.BUYING ? customer.getOfferDisplayItems() : List.of();
            if (!offerDisplayItems.isEmpty()) {
                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource buffer = event.getMultiBufferSource();

                poseStack.pushPose();
                // Translate above the head
                poseStack.translate(0, customer.getBbHeight() + 0.5F, 0);
                poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

                float iconSpacing = 0.5F;
                float startX = -((offerDisplayItems.size() - 1) * iconSpacing) / 2.0F;
                int index = 0;
                for (ItemStack costA : offerDisplayItems) {
                    poseStack.pushPose();
                    poseStack.translate(startX + index * iconSpacing, 0, 0);
                    Minecraft.getInstance().getItemRenderer().renderStatic(
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
}
