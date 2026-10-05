package com.vikingkittens.mc.customers.compatability;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public final class ComponentCUtils {
    private ComponentCUtils() {}

    public static Codec<Component> codec() {
        return ComponentSerialization.CODEC;
    }

    public static Component withColor(Component component, int color) {
        return component.copy().withStyle(style -> style.withColor(color));
    }
}
