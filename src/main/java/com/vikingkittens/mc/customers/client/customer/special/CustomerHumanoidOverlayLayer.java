package com.vikingkittens.mc.customers.client.customer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class CustomerHumanoidOverlayLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final HumanoidModel<T> layerModel;
    private final ResourceLocation textureLocation;

    public CustomerHumanoidOverlayLayer(RenderLayerParent<T, M> renderer, HumanoidModel<T> layerModel, ResourceLocation textureLocation) {
        super(renderer);
        this.layerModel = layerModel;
        this.textureLocation = textureLocation;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        coloredCutoutModelCopyLayerRender(
                getParentModel(),
                layerModel,
                textureLocation,
                poseStack,
                bufferSource,
                packedLight,
                livingEntity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch,
                partialTick,
                -1
        );
    }
}
