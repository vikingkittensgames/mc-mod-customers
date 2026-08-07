package com.vikingkittens.mc.customers.client.customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlockEntity;

public class CustomerPickupCounterBlockEntityRenderer implements BlockEntityRenderer<
        CustomerPickupCounterBlockEntity,
        CustomerPickupCounterBlockEntityRenderer.RenderState> {
    private static final float ITEM_SCALE = 0.35F;
    private static final float STACK_OFFSET = 0.075F;
    private static final float STACK_HEIGHT = 0.03F;
    private static final List<Vec3> ITEM_POSITIONS = List.of(
            new Vec3(0.5D, 0.08D, 0.5D),
            new Vec3(0.25D, 0.08D, 0.25D),
            new Vec3(0.5D, 0.082D, 0.25D),
            new Vec3(0.75D, 0.08D, 0.25D),
            new Vec3(0.25D, 0.082D, 0.5D),
            new Vec3(0.75D, 0.082D, 0.5D),
            new Vec3(0.25D, 0.08D, 0.75D),
            new Vec3(0.5D, 0.082D, 0.75D),
            new Vec3(0.75D, 0.08D, 0.75D)
    );
    private final ItemModelResolver itemModelResolver;

    public CustomerPickupCounterBlockEntityRenderer(
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
            CustomerPickupCounterBlockEntity counter,
            RenderState renderState,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(
                counter,
                renderState,
                partialTick,
                cameraPosition,
                crumblingOverlay
        );
        List<ItemStack> items = counter.getDisplayItems();
        List<Vec3> positions = getItemPositions(items.size());
        List<RenderedItem> renderedItems = new ArrayList<>();
        Random random = new Random();
        for (int index = 0; index < positions.size(); index++) {
            Vec3 position = positions.get(index);
            ItemStack stack = items.get(index);
            int modelCount = getModelCount(stack.getCount(), stack.getMaxStackSize());
            random.setSeed(Item.getId(stack.getItem()) + stack.getDamageValue());
            for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
                float xOffset = modelCount == 1
                        ? 0.0F
                        : (random.nextFloat() * 2.0F - 1.0F) * STACK_OFFSET;
                float zOffset = modelCount == 1
                        ? 0.0F
                        : (random.nextFloat() * 2.0F - 1.0F) * STACK_OFFSET;
                ItemStackRenderState itemRenderState = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(
                        itemRenderState,
                        stack,
                        ItemDisplayContext.FIXED,
                        counter.getLevel(),
                        null,
                        (int) counter.getBlockPos().asLong() + index + modelIndex
                );
                renderedItems.add(new RenderedItem(
                        itemRenderState,
                        position.add(xOffset, STACK_HEIGHT * modelIndex, zOffset)
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
            poseStack.translate(item.position().x, item.position().y, item.position().z);
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
    }

    static List<Vec3> getItemPositions(int itemCount) {
        return ITEM_POSITIONS.subList(0, Math.min(itemCount, ITEM_POSITIONS.size()));
    }

    static int getModelCount(int itemCount, int maxStackSize) {
        if (itemCount <= 1) {
            return 1;
        }
        return 1 + Mth.ceil((float) itemCount / maxStackSize * 4.0F);
    }

    public static class RenderState extends BlockEntityRenderState {
        private List<RenderedItem> items = Collections.emptyList();
    }

    private record RenderedItem(ItemStackRenderState renderState, Vec3 position) {
    }
}
