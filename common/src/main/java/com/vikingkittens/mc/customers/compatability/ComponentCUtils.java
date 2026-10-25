package com.vikingkittens.mc.customers.compatability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public final class ComponentCUtils {
    private ComponentCUtils() {}

    public static Codec<Component> codec() {
        return ExtraCodecs.JSON.comapFlatMap(
                json -> {
                    Component component = Component.Serializer.fromJson(json);
                    return component == null
                            ? DataResult.error(() -> "Invalid text component")
                            : DataResult.success(component);
                },
                Component.Serializer::toJsonTree
        );
    }

    public static Component withColor(Component component, int color) {
        return component.copy().withStyle(style -> style.withColor(color));
    }
}
