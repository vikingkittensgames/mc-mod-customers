package com.vikingkittens.mc.customers.client.customer;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomersDisplayCaseGlassRendererTest {
    @Test
    void selectsGlassModelForEveryConnectionState() {
        assertEquals(
                model("single"),
                CustomersDisplayCaseGlassRenderer.getGlassModelId(
                        false,
                        false
                )
        );
        assertEquals(
                model("right"),
                CustomersDisplayCaseGlassRenderer.getGlassModelId(
                        true,
                        false
                )
        );
        assertEquals(
                model("left"),
                CustomersDisplayCaseGlassRenderer.getGlassModelId(
                        false,
                        true
                )
        );
        assertEquals(
                model("middle"),
                CustomersDisplayCaseGlassRenderer.getGlassModelId(
                        true,
                        true
                )
        );
    }

    @Test
    void rotatesGlassModelForEveryFacing() {
        assertEquals(
                0.0F,
                CustomersDisplayCaseGlassRenderer.getRotationDegrees(
                        Direction.NORTH
                )
        );
        assertEquals(
                -90.0F,
                CustomersDisplayCaseGlassRenderer.getRotationDegrees(
                        Direction.EAST
                )
        );
        assertEquals(
                180.0F,
                CustomersDisplayCaseGlassRenderer.getRotationDegrees(
                        Direction.SOUTH
                )
        );
        assertEquals(
                90.0F,
                CustomersDisplayCaseGlassRenderer.getRotationDegrees(
                        Direction.WEST
                )
        );
    }

    @Test
    void registersEveryConnectionModelOnce() {
        assertEquals(4, CustomersDisplayCaseGlassRenderer.modelIds().size());
        assertEquals(
                4,
                new HashSet<>(
                        CustomersDisplayCaseGlassRenderer.modelIds()
                ).size()
        );
    }

    private static ResourceLocation model(String state) {
        return ResourceLocation.fromNamespaceAndPath(
                "customers",
                "block/display_case_glass_" + state
        );
    }
}
