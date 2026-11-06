package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.vikingkittens.mc.customers.client.common.SpawnerIconRenderer;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;

public final class CustomerSpawnerIconBlockEntityRenderer implements BlockEntityRenderer<CustomerSpawnerBlockEntity> {
    public CustomerSpawnerIconBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CustomerSpawnerBlockEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        if (entity.getIcon() != null) SpawnerIconRenderer.render(entity.getIcon(), poseStack, buffers,
                LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above()), overlay);
    }
}
