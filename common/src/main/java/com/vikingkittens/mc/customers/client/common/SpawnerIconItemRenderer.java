package com.vikingkittens.mc.customers.client.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlock;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerItem;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerItem;

public final class SpawnerIconItemRenderer {
    private SpawnerIconItemRenderer() {}

    public static void render(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation icon;
        BlockState state;
        if (stack.is(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get())) {
            icon = CustomerSpawnerItem.getIcon(stack).orElse(null);
            CustomerSpawnerMode mode = CustomerSpawnerItem.getMode(stack).orElse(CustomerSpawnerMode.CONTINUOUS);
            state = CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.get().defaultBlockState()
                    .setValue(CustomerSpawnerBlock.STATE_SPAWN_MODE, mode);
        } else {
            icon = SupplierSpawnerItem.getIcon(stack).orElse(null);
            state = SupplierSpawner.SUPPLIER_SPAWNER_BLOCK.get().defaultBlockState();
        }
        poseStack.pushPose();
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffers, light, overlay);
        if (icon != null) SpawnerIconRenderer.render(icon, poseStack, buffers, light, overlay);
        poseStack.popPose();
    }
}
