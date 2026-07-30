package com.vikingkittens.mc.customers.client.customer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vikingkittens.mc.customers.customer.special.CustomerHuskEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class CustomerHuskEntityRenderer extends HumanoidMobRenderer<
        CustomerHuskEntity,
        ZombieRenderState,
        ZombieModel<ZombieRenderState>
> {
    private static final Identifier HUSK_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/husk.png");

    public CustomerHuskEntityRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.HUSK_BABY)),
                0.5F
        );
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                ArmorModelSet.bake(
                        ModelLayers.HUSK_ARMOR,
                        context.getModelSet(),
                        ZombieModel::new
                ),
                ArmorModelSet.bake(
                        ModelLayers.HUSK_BABY_ARMOR,
                        context.getModelSet(),
                        ZombieModel::new
                ),
                context.getEquipmentRenderer()
        ));
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState renderState) {
        return HUSK_LOCATION;
    }

    @Override
    protected void scale(ZombieRenderState renderState, PoseStack poseStack) {
        poseStack.scale(1.0625F, 1.0625F, 1.0625F);
        super.scale(renderState, poseStack);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(
            CustomerHuskEntity entity,
            ZombieRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.isAggressive = entity.isAggressive();
        renderState.isPassenger = entity.isPassenger();
    }
}