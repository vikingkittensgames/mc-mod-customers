package com.vikingkittens.mc.customers.compatability;

import net.minecraft.network.chat.Component;

public final class ComponentCUtils {
    private ComponentCUtils() {}

    public static Component withColor(Component component, int color) {
        return component.copy().withStyle(style -> style.withColor(color));
    }
}
