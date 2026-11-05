package com.vikingkittens.mc.customers.common;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockUtilsTest {
    @Test
    void usesHorizontalClickedFaceForWallPlacement() {
        assertEquals(
                Direction.EAST,
                BlockUtils.resolveHorizontalPlacementFacing(
                        Direction.EAST,
                        Direction.NORTH
                )
        );
    }

    @Test
    void facesThePlayerForFloorAndCeilingPlacement() {
        assertEquals(
                Direction.SOUTH,
                BlockUtils.resolveHorizontalPlacementFacing(
                        Direction.UP,
                        Direction.NORTH
                )
        );
        assertEquals(
                Direction.NORTH,
                BlockUtils.resolveHorizontalPlacementFacing(
                        Direction.DOWN,
                        Direction.SOUTH
                )
        );
    }
}
