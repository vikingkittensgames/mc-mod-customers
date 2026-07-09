package com.vikingkittens.mc.customers.client.customer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vikingkittens.mc.customers.customer.special.CustomerHuskEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class CustomerHuskEntityRenderer extends HumanoidMobRenderer<CustomerHuskEntity, CustomerHuskEntityRenderer.Model> {
    private static final ResourceLocation HUSK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/husk.png");

    public CustomerHuskEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.HUSK)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.HUSK_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.HUSK_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerHuskEntity entity) {
        return HUSK_LOCATION;
    }

    @Override
    protected void scale(CustomerHuskEntity livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.0625F, 1.0625F, 1.0625F);
        super.scale(livingEntity, poseStack, partialTickTime);
    }

    public static class Model extends HumanoidModel<CustomerHuskEntity> {
        public Model(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(CustomerHuskEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, entity.isAggressive(), this.attackTime, ageInTicks);
        }
    }
}
