package com.vikingkittens.mc.customers.client.supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import com.vikingkittens.mc.customers.client.common.SpawnerIconRenderer;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerBlockEntity;

public final class SupplierSpawnerIconBlockEntityRenderer implements BlockEntityRenderer<SupplierSpawnerBlockEntity> {
    public SupplierSpawnerIconBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SupplierSpawnerBlockEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        if (entity.getIcon() != null) SpawnerIconRenderer.render(entity.getIcon(), poseStack, buffers, light, overlay);
    }
}
