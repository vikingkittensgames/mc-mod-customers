package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerLeaderboardBlockTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void facesAwayFromAHorizontalClickedBlockFace() {
        assertEquals(
                Direction.EAST,
                CustomerLeaderboardBlock.resolvePlacementFacing(
                        Direction.EAST,
                        Direction.NORTH
                )
        );
    }

    @Test
    void facesThePlayerWhenPlacedOnFloorOrCeiling() {
        assertEquals(
                Direction.SOUTH,
                CustomerLeaderboardBlock.resolvePlacementFacing(
                        Direction.UP,
                        Direction.NORTH
                )
        );
        assertEquals(
                Direction.NORTH,
                CustomerLeaderboardBlock.resolvePlacementFacing(
                        Direction.DOWN,
                        Direction.SOUTH
                )
        );
    }

    @Test
    void usesATwoByTwelveBySixteenSouthFacingShape() {
        AABB bounds = CustomerLeaderboardBlock.getShapeForFacing(Direction.NORTH).bounds();

        assertEquals(2.0D / 16.0D, bounds.minX);
        assertEquals(14.0D / 16.0D, bounds.maxX);
        assertEquals(0.0D, bounds.minY);
        assertEquals(1.0D, bounds.maxY);
        assertEquals(14.0D / 16.0D, bounds.minZ);
        assertEquals(1.0D, bounds.maxZ);
    }
}
