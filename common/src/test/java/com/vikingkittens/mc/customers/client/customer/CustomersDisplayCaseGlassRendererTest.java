package com.vikingkittens.mc.customers.client.customer;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

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

    @Test
    void passesTheRawModelIdToTheConfiguredResolver() {
        ResourceLocation modelId = model("single");
        AtomicReference<ResourceLocation> requestedModel =
                new AtomicReference<>();
        BakedModel expected = mock(BakedModel.class);
        CustomersDisplayCaseGlassRenderer.setModelResolver(id -> {
            requestedModel.set(id);
            return expected;
        });

        try {
            assertSame(
                    expected,
                    CustomersDisplayCaseGlassRenderer.resolveModel(modelId)
            );
            assertEquals(modelId, requestedModel.get());
        } finally {
            CustomersDisplayCaseGlassRenderer.resetModelResolver();
        }
    }

    @Test
    void usesTheStandaloneForgeModelVariantForLookup() {
        ResourceLocation modelId = model("single");

        assertEquals(
                new ModelResourceLocation(modelId, "standalone"),
                CustomersDisplayCaseGlassRenderer.additionalModelLocation(
                        modelId
                )
        );
    }

    private static ResourceLocation model(String state) {
        return new ResourceLocation(
                "customers",
                "block/display_case_glass_" + state
        );
    }
}
