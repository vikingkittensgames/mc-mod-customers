package com.vikingkittens.mc.customers.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

import com.vikingkittens.mc.customers.client.customer.CustomerWantedItemsRenderer;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {
    @Shadow
    protected abstract boolean shouldShowName(Entity entity);

    @Inject(method = "render", at = @At("RETURN"))
    private void renderCustomerWantedItems(
            Entity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo callback
    ) {
        CustomerWantedItemsRenderer.render(
                entity,
                CustomerWantedItemsRenderer.isNameTagRendered(
                        entity,
                        shouldShowName(entity),
                        Minecraft.getInstance()
                                .getEntityRenderDispatcher()
                                .distanceToSqr(entity)
                ),
                poseStack,
                buffer,
                packedLight
        );
    }
}
