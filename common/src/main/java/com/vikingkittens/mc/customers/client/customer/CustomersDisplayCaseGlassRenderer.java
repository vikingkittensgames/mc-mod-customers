package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.RenderCUtils;
import com.vikingkittens.mc.customers.customer.CustomersDisplayCaseBlock;

public final class CustomersDisplayCaseGlassRenderer {
    private static final List<String> MODEL_STATES = List.of(
            "single",
            "left",
            "middle",
            "right"
    );
    private static final List<Identifier> MODEL_IDS = createModelIds();

    private static Function<Identifier, BlockStateModel> modelProvider;

    private CustomersDisplayCaseGlassRenderer() {}

    static void render(
            BlockState state,
            PoseStack poseStack,
            OrderedSubmitNodeCollector nodeCollector,
            int packedLight,
            int packedOverlay
    ) {
        Identifier modelId = getGlassModelId(
                state.getValue(CustomersDisplayCaseBlock.EXTEND_LEFT),
                state.getValue(CustomersDisplayCaseBlock.EXTEND_RIGHT)
        );
        BlockStateModel model = modelProvider.apply(modelId);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                getRotationDegrees(
                        state.getValue(CustomersDisplayCaseBlock.FACING)
                )
        ));
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        nodeCollector.submitBlockModel(
                poseStack,
                RenderCUtils.getTranslucentNoDepthRenderType(),
                model,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay,
                0
        );
        poseStack.popPose();
    }

    public static void setModelProvider(
            Function<Identifier, BlockStateModel> provider
    ) {
        modelProvider = Objects.requireNonNull(provider);
    }

    public static List<Identifier> modelIds() {
        return MODEL_IDS;
    }

    static Identifier getGlassModelId(
            boolean extendLeft,
            boolean extendRight
    ) {
        return Identifier.fromNamespaceAndPath(
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

    private static List<Identifier> createModelIds() {
        return MODEL_STATES.stream()
                .map(state -> Identifier.fromNamespaceAndPath(
                        Customers.MODID,
                        "block/display_case_glass_" + state
                ))
                .toList();
    }
}
