package com.vikingkittens.mc.customers.client.customer.special;

import com.vikingkittens.mc.customers.customer.special.CustomerZombieEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class CustomerZombieEntityRenderer extends HumanoidMobRenderer<CustomerZombieEntity, CustomerZombieEntityRenderer.Model> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public CustomerZombieEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerZombieEntity entity) {
        return ZOMBIE_LOCATION;
    }

    public static class Model extends HumanoidModel<CustomerZombieEntity> {
        public Model(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(CustomerZombieEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, entity.isAggressive(), this.attackTime, ageInTicks);
        }
    }
}
