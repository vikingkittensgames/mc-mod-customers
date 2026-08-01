package com.vikingkittens.mc.customers.client.customer.special;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.customer.special.CustomerZombieEntity;

public class CustomerZombieEntityRenderer extends HumanoidMobRenderer<
        CustomerZombieEntity,
        ZombieRenderState,
        ZombieModel<ZombieRenderState>
> {
    private static final Identifier ZOMBIE_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public CustomerZombieEntityRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                0.5F
        );
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                ArmorModelSet.bake(
                        ModelLayers.ZOMBIE_ARMOR,
                        context.getModelSet(),
                        ZombieModel::new
                ),
                ArmorModelSet.bake(
                        ModelLayers.ZOMBIE_BABY_ARMOR,
                        context.getModelSet(),
                        ZombieModel::new
                ),
                context.getEquipmentRenderer()
        ));
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState renderState) {
        return ZOMBIE_LOCATION;
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(
            CustomerZombieEntity entity,
            ZombieRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.isAggressive = entity.isAggressive();
        renderState.isPassenger = entity.isPassenger();
    }
}
