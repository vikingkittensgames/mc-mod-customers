package com.vikingkittens.mc.customers.client.appearance.monsters;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

final class MonsterCustomersVillagerWitchRenderer<T extends Mob & CustomersVillager>
        extends MobRenderer<T, WitchRenderState, WitchModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/witch.png");

    MonsterCustomersVillagerWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
        addLayer(new WitchItemLayer(this));
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public void extractRenderState(
            T entity,
            WitchRenderState renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);
        HoldingEntityRenderState.extractHoldingEntityRenderState(entity, renderState, itemModelResolver);
        renderState.isHoldingItem = !entity.getMainHandItem().isEmpty();
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState renderState) {
        return TEXTURE;
    }

    @Override
    protected void scale(WitchRenderState renderState, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
