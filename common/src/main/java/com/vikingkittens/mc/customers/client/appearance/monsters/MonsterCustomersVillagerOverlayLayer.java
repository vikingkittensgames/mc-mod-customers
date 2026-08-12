package com.vikingkittens.mc.customers.client.appearance.monsters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

final class MonsterCustomersVillagerOverlayLayer
        extends RenderLayer<
                CustomerVillagerEntity,
                MonsterCustomersVillagerHumanoidRenderer.Model> {
    private final HumanoidModel<CustomerVillagerEntity> overlayModel;
    private final ResourceLocation texture;

    MonsterCustomersVillagerOverlayLayer(
            RenderLayerParent<
                            CustomerVillagerEntity,
                            MonsterCustomersVillagerHumanoidRenderer.Model>
                    renderer,
            HumanoidModel<CustomerVillagerEntity> overlayModel,
            ResourceLocation texture
    ) {
        super(renderer);
        this.overlayModel = overlayModel;
        this.texture = texture;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CustomerVillagerEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        coloredCutoutModelCopyLayerRender(
                getParentModel(),
                overlayModel,
                texture,
                poseStack,
                buffer,
                packedLight,
                entity,
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
