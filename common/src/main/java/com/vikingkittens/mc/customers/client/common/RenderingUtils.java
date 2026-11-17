package com.vikingkittens.mc.customers.client.common;

import net.minecraft.util.Mth;

public final class RenderingUtils {
    private RenderingUtils() {}

    public static int getModelCount(int itemCount, int maxStackSize) {
        if (itemCount <= 1) {
            return 1;
        }
        return 1 + Mth.ceil(
                (float) itemCount / maxStackSize * 4.0F
        );
    }
}
