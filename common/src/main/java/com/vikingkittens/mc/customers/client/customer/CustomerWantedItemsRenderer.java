package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerRenderProxy;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public final class CustomerWantedItemsRenderer {
    private static final int MAX_OVERHEAD_ITEMS = 3;
    private static final float NAME_TAG_TEXT_SCALE = 0.025F;
    private static final float NAME_TAG_ITEM_GAP = 0.12F;

    private CustomerWantedItemsRenderer() {}

    public static void render(
            Entity renderedEntity,
            boolean nameTagRendered,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        CustomerVillagerEntity customer = getRenderedCustomer(renderedEntity);
        if (customer == null) {
            return;
        }
        List<ItemStack> offerDisplayItems = customer.getState() == CustomerState.BUYING
                ? CustomerSpawnerSnapshotManager.findOfferCostItems(customer.getUUID(), MAX_OVERHEAD_ITEMS)
                : List.of();
        if (offerDisplayItems.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float nameTagOffset = nameTagRendered
                ? minecraft.font.lineHeight * NAME_TAG_TEXT_SCALE + NAME_TAG_ITEM_GAP
                : 0.0F;
        poseStack.pushPose();
        float offset = 0.25F;
        float appearanceNameTagOffset = CustomersVillagerClientAppearances.getNameTagOffset(customer);
        if (appearanceNameTagOffset != 0.0F) {
            offset += appearanceNameTagOffset - nameTagOffset;
        }
        poseStack.translate(0, customer.getBbHeight() + offset, 0);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.translate(0.0F, nameTagOffset, 0.0F);

        float iconSpacing = 0.5F;
        float startX = -((offerDisplayItems.size() - 1) * iconSpacing) / 2.0F;
        for (int index = 0; index < offerDisplayItems.size(); index++) {
            poseStack.pushPose();
            poseStack.translate(startX + index * iconSpacing, 0, 0);
            minecraft.getItemRenderer().renderStatic(
                    offerDisplayItems.get(index),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    customer.level(),
                    0
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    static @Nullable CustomerVillagerEntity getRenderedCustomer(Entity renderedEntity) {
        if (renderedEntity instanceof CustomerVillagerEntity customer) {
            return customer;
        }
        if (renderedEntity instanceof CustomersVillagerRenderProxy proxy &&
                proxy.getCustomersVillagerSource() instanceof CustomerVillagerEntity customer) {
            return customer;
        }
        return null;
    }

    public static boolean getDefaultNameTagVisibility(Entity renderedEntity, boolean sourceVisibility) {
        return renderedEntity instanceof CustomersVillagerRenderProxy proxy
                ? proxy.shouldRenderNameTag()
                : sourceVisibility;
    }
}
