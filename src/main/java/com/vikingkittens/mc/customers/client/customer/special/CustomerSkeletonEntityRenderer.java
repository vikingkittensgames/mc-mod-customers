package com.vikingkittens.mc.customers.client.customer.special;

import com.vikingkittens.mc.customers.customer.special.CustomerSkeletonEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class CustomerSkeletonEntityRenderer extends HumanoidMobRenderer<CustomerSkeletonEntity, HumanoidModel<CustomerSkeletonEntity>> {
    private static final ResourceLocation SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public CustomerSkeletonEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CustomerSkeletonEntity entity) {
        return SKELETON_LOCATION;
    }
}
