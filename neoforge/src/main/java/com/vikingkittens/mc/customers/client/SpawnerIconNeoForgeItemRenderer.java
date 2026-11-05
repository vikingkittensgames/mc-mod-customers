package com.vikingkittens.mc.customers.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.client.common.SpawnerIconItemRenderer;

public final class SpawnerIconNeoForgeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public SpawnerIconNeoForgeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        SpawnerIconItemRenderer.render(stack, pose, buffers, light, overlay);
    }
}
