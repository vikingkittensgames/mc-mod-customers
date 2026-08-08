package com.vikingkittens.mc.customers.client.appearance.monsters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

final class MonsterCustomersVillagerWitchRenderer
        extends MobRenderer<
                CustomerVillagerEntity,
                WitchModel<CustomerVillagerEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace(
                    "textures/entity/witch.png"
            );

    MonsterCustomersVillagerWitchRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new WitchModel<>(context.bakeLayer(ModelLayers.WITCH)),
                0.5F
        );
        addLayer(new WitchItemLayer<>(
                this,
                context.getItemInHandRenderer()
        ));
    }

    @Override
    public void render(
            CustomerVillagerEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        model.setHoldingItem(!entity.getMainHandItem().isEmpty());
        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                buffer,
                packedLight
        );
    }

    @Override
    public ResourceLocation getTextureLocation(
            CustomerVillagerEntity entity
    ) {
        return TEXTURE;
    }

    @Override
    protected void scale(
            CustomerVillagerEntity entity,
            PoseStack poseStack,
            float partialTick
    ) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
