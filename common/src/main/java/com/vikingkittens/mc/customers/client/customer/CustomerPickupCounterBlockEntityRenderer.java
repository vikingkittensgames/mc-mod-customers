package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.client.common.RenderingUtils;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlockEntity;

public class CustomerPickupCounterBlockEntityRenderer
        implements BlockEntityRenderer<CustomerPickupCounterBlockEntity> {
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
            RenderingUtils.renderItemStackOnSurface(
                    stack,
                    position,
                    poseStack,
                    buffer,
                    packedLight,
                    counter.getLevel(),
                    (int) counter.getBlockPos().asLong() + index
            );
        }
    }

    static List<Vec3> getItemPositions(int itemCount) {
        return ITEM_POSITIONS.subList(
                0,
                Math.min(itemCount, ITEM_POSITIONS.size())
        );
    }

}
