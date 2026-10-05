package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerGeckoResources {
    private SkinCustomersVillagerGeckoResources() {}

    static ResourceLocation getModelResource(SkinCustomersVillagerDefinition definition) {
        ResourceLocation model = definition.model().resource();
        if (model.getPath().endsWith(".geo.json")) {
            return model;
        }
        return ResourceLocation.fromNamespaceAndPath(
                model.getNamespace(),
                "geo/customers/skins/" + model.getPath() + ".geo.json"
        );
    }

    static ResourceLocation getAnimationResource(SkinCustomersVillagerDefinition definition) {
        ResourceLocation animation = definition.animation().orElseThrow();
        if (animation.getPath().endsWith(".animation.json")) {
            return animation;
        }
        return ResourceLocation.fromNamespaceAndPath(
                animation.getNamespace(),
                "animations/customers/skins/" + animation.getPath() + ".animation.json"
        );
    }
}
