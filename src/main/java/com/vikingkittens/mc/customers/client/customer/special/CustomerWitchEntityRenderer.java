package com.vikingkittens.mc.customers.client.customer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vikingkittens.mc.customers.customer.special.CustomerWitchEntity;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;

public class CustomerWitchEntityRenderer extends MobRenderer<CustomerWitchEntity, WitchModel<CustomerWitchEntity>> {
    private static final ResourceLocation WITCH_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/witch.png");

    public CustomerWitchEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel<>(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
        this.addLayer(new WitchItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(CustomerWitchEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.setHoldingItem(!entity.getMainHandItem().isEmpty());
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerWitchEntity entity) {
        return WITCH_LOCATION;
    }

    @Override
    protected void scale(CustomerWitchEntity livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
