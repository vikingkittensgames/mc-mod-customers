package com.vikingkittens.mc.customers.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import com.vikingkittens.mc.customers.client.customer.CustomerWantedItemsRenderer;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {
    @Inject(method = "renderNameTag", at = @At("TAIL"))
    private void renderCustomerWantedItems(
            Entity entity,
            Component nameTag,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float partialTick,
            CallbackInfo callback
    ) {
        CustomerWantedItemsRenderer.render(
                entity,
                true,
                poseStack,
                buffer,
                packedLight
        );
    }
}
