package com.vikingkittens.mc.customers.client.customer.special;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.customer.special.CustomerDrownedEntity;

public class CustomerDrownedEntityRenderer extends HumanoidMobRenderer<
        CustomerDrownedEntity,
        ZombieRenderState,
        DrownedModel
> {
    private static final Identifier DROWNED_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/drowned.png");

    public CustomerDrownedEntityRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)),
                new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY)),
                0.5F
        );
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                ArmorModelSet.bake(
                        ModelLayers.DROWNED_ARMOR,
                        context.getModelSet(),
                        DrownedModel::new
                ),
                ArmorModelSet.bake(
                        ModelLayers.DROWNED_BABY_ARMOR,
                        context.getModelSet(),
                        DrownedModel::new
                ),
                context.getEquipmentRenderer()
        ));
        this.addLayer(new DrownedOuterLayer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState renderState) {
        return DROWNED_LOCATION;
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(
            CustomerDrownedEntity entity,
            ZombieRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.isAggressive = entity.isAggressive();
        renderState.isPassenger = entity.isPassenger();
    }
}
