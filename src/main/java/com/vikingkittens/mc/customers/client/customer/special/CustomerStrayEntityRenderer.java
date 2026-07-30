package com.vikingkittens.mc.customers.client.customer.special;

import com.vikingkittens.mc.customers.customer.special.CustomerStrayEntity;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;

public class CustomerStrayEntityRenderer extends HumanoidMobRenderer<CustomerStrayEntity, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
    private static final Identifier STRAY_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/stray.png");
    private static final Identifier STRAY_OVERLAY_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png");

    public CustomerStrayEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.STRAY)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                ArmorModelSet.bake(ModelLayers.STRAY_ARMOR, context.getModelSet(), SkeletonModel::new),
                ArmorModelSet.bake(ModelLayers.STRAY_ARMOR, context.getModelSet(), SkeletonModel::new),
                context.getEquipmentRenderer()
        ));
        this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), ModelLayers.STRAY_OUTER_LAYER, STRAY_OVERLAY_LOCATION));
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState renderState) {
        return STRAY_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public void extractRenderState(CustomerStrayEntity entity, SkeletonRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.isAggressive = entity.isAggressive();
        renderState.isPassenger = entity.isPassenger();
    }
}