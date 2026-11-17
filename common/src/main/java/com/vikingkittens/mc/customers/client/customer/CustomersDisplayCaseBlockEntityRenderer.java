package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.client.common.RenderingUtils;
import com.vikingkittens.mc.customers.customer.CustomersDisplayCaseBlock;
import com.vikingkittens.mc.customers.customer.CustomersDisplayCaseBlockEntity;

public class CustomersDisplayCaseBlockEntityRenderer
        implements BlockEntityRenderer<CustomersDisplayCaseBlockEntity, CustomersDisplayCaseBlockEntityRenderer.RenderState> {
    static final boolean PITCH_ITEMS = true;
    private static final float ITEM_SCALE = 0.35F;
    private static final float STACK_HEIGHT = 0.03F;
    private static final double PITCHED_ITEM_HEIGHT_OFFSET = 0.5D / 16.0D;
    private static final double ITEM_PITCH_DEGREES = 15.0D;
    private static final double SURFACE_OFFSET = 0.0175D;
    private static final double MIDDLE_ITEM_HEIGHT_OFFSET = 0.01D / 16.0D;
    private static final List<Vec3> ITEM_SURFACE_POSITIONS = List.of(
            pixels(4.0D, 10.0D, 11.0D),
            pixels(8.0D, 10.0D, 11.0D),
            pixels(12.0D, 10.0D, 11.0D),
            pixels(4.0D, 9.0D, 5.0D),
            pixels(8.0D, 9.0D, 5.0D),
            pixels(12.0D, 9.0D, 5.0D),
            pixels(4.0D, 4.0D, 11.0D),
            pixels(8.0D, 4.0D, 11.0D),
            pixels(12.0D, 4.0D, 11.0D),
            pixels(4.0D, 3.0D, 5.0D),
            pixels(8.0D, 3.0D, 5.0D),
            pixels(12.0D, 3.0D, 5.0D)
    );
    private final ItemModelResolver itemModelResolver;

    public CustomersDisplayCaseBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            CustomersDisplayCaseBlockEntity displayCase,
            RenderState renderState,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(
                displayCase,
                renderState,
                partialTick,
                cameraPosition,
                crumblingOverlay
        );
        Direction facing = displayCase.getBlockState().getValue(
                CustomersDisplayCaseBlock.FACING
        );
        List<RenderedItem> renderedItems = new ArrayList<>();
        for (int slot = 0;
                slot < displayCase.getContainerSize();
                slot++) {
            ItemStack stack = displayCase.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Vec3 position = getItemRenderPosition(slot, facing);
            int modelCount = RenderingUtils.getModelCount(
                    stack.getCount(),
                    stack.getMaxStackSize()
            );
            for (int modelIndex = 0;
                    modelIndex < modelCount;
                    modelIndex++) {
                ItemStackRenderState itemRenderState =
                        new ItemStackRenderState();
                itemModelResolver.updateForTopItem(
                        itemRenderState,
                        stack,
                        ItemDisplayContext.FIXED,
                        displayCase.getLevel(),
                        null,
                        (int) displayCase.getBlockPos().asLong()
                                + slot
                                + modelIndex
                );
                renderedItems.add(new RenderedItem(
                        itemRenderState,
                        position.add(
                                0.0D,
                                STACK_HEIGHT * modelIndex,
                                0.0D
                        ),
                        getItemPitch(facing),
                        getItemRotation(facing)
                ));
            }
        }
        renderState.items = renderedItems;
    }

    @Override
    public void submit(
            RenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            CameraRenderState cameraRenderState
    ) {
        for (RenderedItem item : renderState.items) {
            if (item.renderState().isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(
                    item.position().x,
                    item.position().y,
                    item.position().z
            );
            applyItemPitch(poseStack, item.pitch());
            poseStack.mulPose(Axis.YP.rotationDegrees(item.rotation()));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            item.renderState().submit(
                    poseStack,
                    nodeCollector,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }
        CustomersDisplayCaseGlassRenderer.render(
                renderState.blockState,
                poseStack,
                nodeCollector,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY
        );
    }

    static Vec3 getItemSurfacePosition(int slot) {
        return getItemSurfacePosition(slot, Direction.NORTH);
    }

    static Vec3 getItemSurfacePosition(
            int slot,
            Direction facing
    ) {
        Vec3 position = ITEM_SURFACE_POSITIONS.get(slot);
        int column = slot % 3;
        if (column == 1) {
            position = position.add(
                    0.0D,
                    MIDDLE_ITEM_HEIGHT_OFFSET,
                    0.0D
            );
        }
        return switch (facing) {
            case EAST -> new Vec3(
                    1.0D - position.z,
                    position.y,
                    position.x
            );
            case SOUTH -> new Vec3(
                    1.0D - position.x,
                    position.y,
                    1.0D - position.z
            );
            case WEST -> new Vec3(
                    position.z,
                    position.y,
                    1.0D - position.x
            );
            default -> position;
        };
    }

    static Vec3 getItemRenderPosition(
            int slot,
            Direction facing
    ) {
        double pitchHeight = PITCH_ITEMS
                ? PITCHED_ITEM_HEIGHT_OFFSET
                : 0.0D;
        return getItemSurfacePosition(slot, facing).add(
                0.0D,
                SURFACE_OFFSET + pitchHeight,
                0.0D
        );
    }

    private static Vec3 pixels(double x, double y, double z) {
        return new Vec3(x / 16.0D, y / 16.0D, z / 16.0D);
    }

    static float getItemRotation(Direction facing) {
        return switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
    }

    static Vec3 getItemPitch(Direction facing) {
        if (!PITCH_ITEMS) {
            return Vec3.ZERO;
        }
        return switch (facing) {
            case NORTH -> new Vec3(-ITEM_PITCH_DEGREES, 0.0D, 0.0D);
            case EAST -> new Vec3(0.0D, 0.0D, -ITEM_PITCH_DEGREES);
            case SOUTH -> new Vec3(ITEM_PITCH_DEGREES, 0.0D, 0.0D);
            case WEST -> new Vec3(0.0D, 0.0D, ITEM_PITCH_DEGREES);
            default -> Vec3.ZERO;
        };
    }

    private static void applyItemPitch(PoseStack poseStack, Vec3 pitch) {
        if (pitch.x != 0.0D) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) pitch.x));
        }
        if (pitch.z != 0.0D) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) pitch.z));
        }
    }

    public static class RenderState extends BlockEntityRenderState {
        private List<RenderedItem> items = Collections.emptyList();
    }

    private record RenderedItem(
            ItemStackRenderState renderState,
            Vec3 position,
            Vec3 pitch,
            float rotation
    ) {}
}
