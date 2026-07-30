package com.vikingkittens.mc.customers.client.customer.special;

import com.vikingkittens.mc.customers.customer.special.CustomerSkeletonEntity;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;

public class CustomerSkeletonEntityRenderer extends HumanoidMobRenderer<CustomerSkeletonEntity, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
    private static final Identifier SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public CustomerSkeletonEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                ArmorModelSet.bake(ModelLayers.SKELETON_ARMOR, context.getModelSet(), SkeletonModel::new),
                ArmorModelSet.bake(ModelLayers.SKELETON_ARMOR, context.getModelSet(), SkeletonModel::new),
                context.getEquipmentRenderer()
        ));
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState renderState) {
        return SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public void extractRenderState(CustomerSkeletonEntity entity, SkeletonRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.isAggressive = entity.isAggressive();
        renderState.isPassenger = entity.isPassenger();
    }
}