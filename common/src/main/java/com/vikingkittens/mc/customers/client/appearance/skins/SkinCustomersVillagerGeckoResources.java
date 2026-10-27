package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerGeckoResources {
    private SkinCustomersVillagerGeckoResources() {}

    static Identifier getModelResource(SkinCustomersVillagerDefinition definition) {
        Identifier model = definition.model().resource();
        return normalize(model, "geckolib/models/", ".geo.json", "customers/skins/");
    }

    static Identifier getAnimationResource(SkinCustomersVillagerDefinition definition) {
        Identifier animation = definition.animation().orElseThrow();
        return normalize(animation, "geckolib/animations/", ".animation.json", "customers/skins/");
    }

    private static Identifier normalize(
            Identifier resource,
            String physicalPrefix,
            String physicalSuffix,
            String customersPrefix
    ) {
        String path = resource.getPath();
        if (path.startsWith(physicalPrefix) && path.endsWith(physicalSuffix)) {
            path = path.substring(physicalPrefix.length(), path.length() - physicalSuffix.length());
        } else if (path.endsWith(physicalSuffix)) {
            path = path.substring(0, path.length() - physicalSuffix.length());
        } else {
            path = customersPrefix + path;
        }
        return Identifier.fromNamespaceAndPath(resource.getNamespace(), path);
    }
}
