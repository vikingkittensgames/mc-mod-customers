package com.vikingkittens.mc.customers.client.compatability;

import net.minecraft.world.phys.AABB;

import java.util.Objects;

/**
 * Describes version-independent debug box geometry.
 *
 * @param bounds world-space box bounds
 * @param color packed ARGB color
 */
public record DebugBoxC(
        AABB bounds,
        int color
) {
    public DebugBoxC {
        Objects.requireNonNull(bounds, "bounds");
    }
}
