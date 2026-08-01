package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.InteractionResult;

/**
 * Provides version-compatible interaction results.
 */
public final class InteractionCUtils {
    private InteractionCUtils() {
    }
    public static InteractionResult sidedSuccess(boolean clientSide) {
        return clientSide
                ? InteractionResult.SUCCESS
                : InteractionResult.SUCCESS_SERVER;
    }
}
