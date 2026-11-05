package com.vikingkittens.mc.customers.client.common;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class RenderingUtils {
    private static final float ITEM_SCALE = 0.35F;
    private static final float STACK_OFFSET = 0.075F;
    private static final float STACK_HEIGHT = 0.03F;

    private RenderingUtils() {}

    public static void renderItemStackOnSurface(
            ItemStack stack,
            Vec3 position,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Level level,
            int renderSeed
    ) {
        renderItemStackOnSurface(
                stack,
                position,
                poseStack,
                buffer,
                packedLight,
                level,
                renderSeed,
                0.0F,
                true
        );
    }

    public static void renderCenteredItemStackOnSurface(
            ItemStack stack,
            Vec3 position,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Level level,
            int renderSeed
    ) {
        renderCenteredItemStackOnSurface(
                stack,
                position,
                poseStack,
                buffer,
                packedLight,
                level,
                renderSeed,
                0.0F
        );
    }

    public static void renderCenteredItemStackOnSurface(
            ItemStack stack,
            Vec3 position,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Level level,
            int renderSeed,
            float surfaceRotationDegrees
    ) {
        renderItemStackOnSurface(
                stack,
                position,
                poseStack,
                buffer,
                packedLight,
                level,
                renderSeed,
                surfaceRotationDegrees,
                false
        );
    }

    private static void renderItemStackOnSurface(
            ItemStack stack,
            Vec3 position,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Level level,
            int renderSeed,
            float surfaceRotationDegrees,
            boolean scatterModels
    ) {
        int modelCount = getModelCount(
                stack.getCount(),
                stack.getMaxStackSize()
        );
        Random random = new Random(
                Item.getId(stack.getItem()) + stack.getDamageValue()
        );
        for (int modelIndex = 0;
                modelIndex < modelCount;
                modelIndex++) {
            float xOffset = modelCount == 1 || !scatterModels
                    ? 0.0F
                    : (random.nextFloat() * 2.0F - 1.0F)
                            * STACK_OFFSET;
            float zOffset = modelCount == 1 || !scatterModels
                    ? 0.0F
                    : (random.nextFloat() * 2.0F - 1.0F)
                            * STACK_OFFSET;
            poseStack.pushPose();
            poseStack.translate(
                    position.x + xOffset,
                    position.y + STACK_HEIGHT * modelIndex,
                    position.z + zOffset
            );
            poseStack.mulPose(
                    Axis.YP.rotationDegrees(surfaceRotationDegrees)
            );
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    level,
                    renderSeed + modelIndex
            );
            poseStack.popPose();
        }
    }

    static int getModelCount(int itemCount, int maxStackSize) {
        if (itemCount <= 1) {
            return 1;
        }
        return 1 + Mth.ceil(
                (float) itemCount / maxStackSize * 4.0F
        );
    }
}
