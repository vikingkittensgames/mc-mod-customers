package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomersDisplayCaseBlockEntityRendererTest {
    @Test
    void placesTopShelfItemsInBackAndFrontRows() {
        assertEquals(
                pixels(4.0D, 10.0D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(0)
        );
        assertEquals(
                pixels(8.0D, 10.01D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(1)
        );
        assertEquals(
                pixels(12.0D, 10.0D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(2)
        );
        assertEquals(
                pixels(4.0D, 9.0D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(3)
        );
        assertEquals(
                pixels(8.0D, 9.01D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(4)
        );
        assertEquals(
                pixels(12.0D, 9.0D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(5)
        );
    }

    @Test
    void placesBottomShelfItemsInBackAndFrontRows() {
        assertEquals(
                pixels(4.0D, 4.0D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(6)
        );
        assertEquals(
                pixels(8.0D, 4.01D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(7)
        );
        assertEquals(
                pixels(12.0D, 4.0D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(8)
        );
        assertEquals(
                pixels(4.0D, 3.0D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(9)
        );
        assertEquals(
                pixels(8.0D, 3.01D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(10)
        );
        assertEquals(
                pixels(12.0D, 3.0D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(11)
        );
    }

    @Test
    void transformsBackShelfPositionsForEveryFacing() {
        assertEquals(
                pixels(4.0D, 10.0D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(0, Direction.NORTH)
        );
        assertEquals(
                pixels(5.0D, 10.0D, 4.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(0, Direction.EAST)
        );
        assertEquals(
                pixels(12.0D, 10.0D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(0, Direction.SOUTH)
        );
        assertEquals(
                pixels(11.0D, 10.0D, 12.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(0, Direction.WEST)
        );
    }

    @Test
    void transformsFrontShelfPositionsForEveryFacing() {
        assertEquals(
                pixels(4.0D, 9.0D, 5.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(3, Direction.NORTH)
        );
        assertEquals(
                pixels(11.0D, 9.0D, 4.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(3, Direction.EAST)
        );
        assertEquals(
                pixels(12.0D, 9.0D, 11.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(3, Direction.SOUTH)
        );
        assertEquals(
                pixels(5.0D, 9.0D, 12.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemSurfacePosition(3, Direction.WEST)
        );
    }

    @Test
    void rotatesItemsToMatchEveryFacing() {
        assertEquals(
                0.0F,
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemRotation(Direction.NORTH)
        );
        assertEquals(
                -90.0F,
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemRotation(Direction.EAST)
        );
        assertEquals(
                180.0F,
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemRotation(Direction.SOUTH)
        );
        assertEquals(
                90.0F,
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemRotation(Direction.WEST)
        );
    }

    @Test
    void pitchesItemsForwardForEveryFacing() {
        assertEquals(
                new Vec3(-15.0D, 0.0D, 0.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemPitch(Direction.NORTH)
        );
        assertEquals(
                new Vec3(0.0D, 0.0D, -15.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemPitch(Direction.EAST)
        );
        assertEquals(
                new Vec3(15.0D, 0.0D, 0.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemPitch(Direction.SOUTH)
        );
        assertEquals(
                new Vec3(0.0D, 0.0D, 15.0D),
                CustomersDisplayCaseBlockEntityRenderer
                        .getItemPitch(Direction.WEST)
        );
    }

    @Test
    void raisesPitchedItemsByHalfAPixel() {
        Vec3 surface = CustomersDisplayCaseBlockEntityRenderer
                .getItemSurfacePosition(0, Direction.NORTH);
        Vec3 render = CustomersDisplayCaseBlockEntityRenderer
                .getItemRenderPosition(0, Direction.NORTH);

        assertEquals(surface.x, render.x);
        assertEquals(surface.y + 0.5D / 16.0D + 0.0175D, render.y);
        assertEquals(surface.z, render.z);
    }

    private static Vec3 pixels(double x, double y, double z) {
        return new Vec3(x / 16.0D, y / 16.0D, z / 16.0D);
    }
}
