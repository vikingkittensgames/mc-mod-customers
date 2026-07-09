package com.vikingkittens.mc.customers.client.customer.special;

import com.vikingkittens.mc.customers.customer.special.CustomerDrownedEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class CustomerDrownedEntityRenderer extends HumanoidMobRenderer<CustomerDrownedEntity, CustomerDrownedEntityRenderer.Model> {
    private static final ResourceLocation DROWNED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned.png");
    private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png");


    public CustomerDrownedEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(ModelLayers.DROWNED)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)),
                context.getModelManager()
        ));
        this.addLayer(new CustomerHumanoidOverlayLayer<>(
                this,
                new Model(context.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER)),
                DROWNED_OUTER_LAYER_LOCATION
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerDrownedEntity entity) {
        return DROWNED_LOCATION;
    }

    public static class Model extends HumanoidModel<CustomerDrownedEntity> {
        public Model(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(CustomerDrownedEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, entity.isAggressive(), this.attackTime, ageInTicks);
        }
    }
}

