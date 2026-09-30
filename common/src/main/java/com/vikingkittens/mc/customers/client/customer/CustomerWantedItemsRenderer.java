package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
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
    private static final float BASE_VERTICAL_OFFSET = 0.25F;

    private CustomerWantedItemsRenderer() {}

    public static void render(
            Entity renderedEntity,
            boolean nameTagRendered,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
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
        poseStack.pushPose();
        float appearanceNameTagOffset = CustomersVillagerClientAppearances.getNameTagOffset(customer);
        float verticalOffset = getVerticalOffset(
                nameTagRendered,
                appearanceNameTagOffset,
                minecraft.font.lineHeight
        );
        poseStack.translate(
                0,
                customer.getBbHeight() + verticalOffset,
                0
        );
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());

        float iconSpacing = 0.5F;
        float startX = -((offerDisplayItems.size() - 1) * iconSpacing) / 2.0F;
        for (int index = 0; index < offerDisplayItems.size(); index++) {
            poseStack.pushPose();
            poseStack.translate(startX + index * iconSpacing, 0, 0);
            ItemStackRenderState itemRenderState = new ItemStackRenderState();
            ItemModelResolver itemModelResolver = minecraft.getItemModelResolver();
            itemModelResolver.updateForTopItem(
                    itemRenderState,
                    offerDisplayItems.get(index),
                    ItemDisplayContext.GROUND,
                    customer.level(),
                    null,
                    customer.getId()
            );
            if (!itemRenderState.isEmpty()) {
                itemRenderState.submit(
                        poseStack,
                        nodeCollector,
                        packedLight,
                        0,
                        0
                );
            }
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

    static float getVerticalOffset(
            boolean nameTagRendered,
            float appearanceNameTagOffset,
            int fontLineHeight
    ) {
        float nameTagOffset = nameTagRendered
                ? fontLineHeight * NAME_TAG_TEXT_SCALE + NAME_TAG_ITEM_GAP
                : 0.0F;
        return BASE_VERTICAL_OFFSET
                + appearanceNameTagOffset
                + nameTagOffset;
    }
}
