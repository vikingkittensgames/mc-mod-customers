package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import dev.architectury.platform.Platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.RenderCUtils;
import com.vikingkittens.mc.customers.customer.CustomersDisplayCaseBlock;

public final class CustomersDisplayCaseGlassRenderer {
    private static final String FABRIC_MODEL_VARIANT = "fabric_resource";
    private static final String NEOFORGE_MODEL_VARIANT = "standalone";
    private static final List<String> MODEL_STATES = List.of(
            "single",
            "left",
            "middle",
            "right"
    );
    private static final List<ResourceLocation> MODEL_IDS = createModelIds();

    private CustomersDisplayCaseGlassRenderer() {}

    static void render(
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ResourceLocation modelId = getGlassModelId(
                state.getValue(CustomersDisplayCaseBlock.EXTEND_LEFT),
                state.getValue(CustomersDisplayCaseBlock.EXTEND_RIGHT)
        );
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getModelManager().getModel(
                additionalModelLocation(modelId)
        );
        VertexConsumer buffer = bufferSource.getBuffer(
                RenderCUtils.getTranslucentNoDepthRenderType()
        );

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                getRotationDegrees(
                        state.getValue(CustomersDisplayCaseBlock.FACING)
                )
        ));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                state,
                model,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    public static List<ResourceLocation> modelIds() {
        return MODEL_IDS;
    }

    public static ModelResourceLocation additionalModelLocation(
            ResourceLocation modelId
    ) {
        return new ModelResourceLocation(
                modelId,
                Platform.isFabric()
                        ? FABRIC_MODEL_VARIANT
                        : NEOFORGE_MODEL_VARIANT
        );
    }

    static ResourceLocation getGlassModelId(
            boolean extendLeft,
            boolean extendRight
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                Customers.MODID,
                "block/display_case_glass_" +
                        getModelState(extendLeft, extendRight)
        );
    }

    static float getRotationDegrees(Direction facing) {
        return switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static String getModelState(
            boolean extendLeft,
            boolean extendRight
    ) {
        if (extendLeft && extendRight) {
            return "middle";
        }
        if (extendLeft) {
            return "right";
        }
        if (extendRight) {
            return "left";
        }
        return "single";
    }

    private static List<ResourceLocation> createModelIds() {
        return MODEL_STATES.stream()
                .map(state -> ResourceLocation.fromNamespaceAndPath(
                        Customers.MODID,
                        "block/display_case_glass_" + state
                ))
                .toList();
    }
}
