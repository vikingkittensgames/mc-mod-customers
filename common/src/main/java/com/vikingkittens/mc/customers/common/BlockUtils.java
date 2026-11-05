package com.vikingkittens.mc.customers.common;

import net.minecraft.core.Direction;

public final class BlockUtils {
    private BlockUtils() {}

    public static Direction resolveHorizontalPlacementFacing(
            Direction clickedFace,
            Direction playerHorizontalDirection
    ) {
        if (clickedFace.getAxis().isHorizontal()) {
            return clickedFace;
        }
        return playerHorizontalDirection.getOpposite();
    }
}
