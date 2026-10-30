package com.vikingkittens.mc.customers.client.appearance.skins;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerGeckoModel extends GeoModel<SkinCustomersVillagerGeckoProxy> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return SkinCustomersVillagerGeckoResources.getModelResource(definition(renderState));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return definition(renderState).getTextureLocation();
    }

    @Override
    public Identifier getAnimationResource(SkinCustomersVillagerGeckoProxy animatable) {
        return SkinCustomersVillagerGeckoResources.getAnimationResource(animatable.getDefinition());
    }

    private static SkinCustomersVillagerDefinition definition(GeoRenderState renderState) {
        return renderState.getGeckolibData(SkinCustomersVillagerGeckoData.DEFINITION);
    }
}
