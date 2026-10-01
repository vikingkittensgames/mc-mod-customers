package com.vikingkittens.mc.customers.compatability;

import net.minecraft.resources.ResourceLocation;

public final class ResourceLocationCUtils {
    private ResourceLocationCUtils() {
    }

    public static ResourceLocation create(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation parse(String value) {
        return new ResourceLocation(value);
    }
}
