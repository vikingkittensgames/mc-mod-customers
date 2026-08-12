package com.vikingkittens.mc.customers.client.appearance.skins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerRenderer<T extends Mob & CustomersVillager> extends HumanoidMobRenderer<T, PlayerModel<T>> {
    SkinCustomersVillagerRenderer(EntityRendererProvider.Context context, boolean slim) {
        super(
                context,
                new PlayerModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim),
                SkinCustomersVillagerDefinition.DEFAULT_SHADOW_RADIUS
        );
        addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        SkinCustomersVillagerDefinition skin = SkinCustomersVillagerClientAppearance.getSkin(entity);
        return SkinCustomersVillagerTextureManager.getTexture(skin);
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float scale = SkinCustomersVillagerClientAppearance.getSkin(entity).scale();
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected float getShadowRadius(T entity) {
        return SkinCustomersVillagerClientAppearance.getSkin(entity).shadowRadius();
    }
}
