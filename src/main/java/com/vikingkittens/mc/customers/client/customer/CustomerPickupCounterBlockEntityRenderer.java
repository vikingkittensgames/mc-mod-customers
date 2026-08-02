package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlockEntity;

public class CustomerPickupCounterBlockEntityRenderer
        implements BlockEntityRenderer<CustomerPickupCounterBlockEntity> {
    private static final float ITEM_SCALE = 0.35F;
    private static final float STACK_OFFSET = 0.075F;
    private static final float STACK_HEIGHT = 0.03F;
    private static final List<Vec3> ITEM_POSITIONS = List.of(
            new Vec3(0.5D, 0.08D, 0.5D),
            new Vec3(0.25D, 0.08D, 0.25D),
            new Vec3(0.5D, 0.08D, 0.25D),
            new Vec3(0.75D, 0.08D, 0.25D),
            new Vec3(0.25D, 0.08D, 0.5D),
            new Vec3(0.75D, 0.08D, 0.5D),
            new Vec3(0.25D, 0.08D, 0.75D),
            new Vec3(0.5D, 0.08D, 0.75D),
            new Vec3(0.75D, 0.08D, 0.75D)
    );
    private final Random random = new Random();

    public CustomerPickupCounterBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            CustomerPickupCounterBlockEntity counter,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        List<ItemStack> items = counter.getDisplayItems();
        List<Vec3> positions = getItemPositions(items.size());
        for (int index = 0; index < positions.size(); index++) {
            Vec3 position = positions.get(index);
            ItemStack stack = items.get(index);
            int modelCount = getModelCount(
                    stack.getCount(),
                    stack.getMaxStackSize()
            );
            random.setSeed(
                    Item.getId(stack.getItem()) + stack.getDamageValue()
            );
            for (int modelIndex = 0;
                    modelIndex < modelCount;
                    modelIndex++) {
                float xOffset = modelCount == 1
                        ? 0.0F
                        : (random.nextFloat() * 2.0F - 1.0F)
                                * STACK_OFFSET;
                float zOffset = modelCount == 1
                        ? 0.0F
                        : (random.nextFloat() * 2.0F - 1.0F)
                                * STACK_OFFSET;
                poseStack.pushPose();
                poseStack.translate(
                        position.x + xOffset,
                        position.y + STACK_HEIGHT * modelIndex,
                        position.z + zOffset
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
                        counter.getLevel(),
                        (int) counter.getBlockPos().asLong()
                                + index
                                + modelIndex
                );
                poseStack.popPose();
            }
        }
    }

    static List<Vec3> getItemPositions(int itemCount) {
        return ITEM_POSITIONS.subList(
                0,
                Math.min(itemCount, ITEM_POSITIONS.size())
        );
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
