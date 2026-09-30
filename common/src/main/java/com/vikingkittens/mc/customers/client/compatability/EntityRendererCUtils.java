package com.vikingkittens.mc.customers.client.compatability;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;

public final class EntityRendererCUtils {
    private EntityRendererCUtils() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static EntityRenderState createRenderState(
            EntityRenderer<?, ?> renderer,
            Entity entity,
            float partialTick
    ) {
        return ((EntityRenderer) renderer).createRenderState(entity, partialTick);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void submit(
            EntityRenderer<?, ?> renderer,
            EntityRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            CameraRenderState cameraRenderState
    ) {
        ((EntityRenderer) renderer).submit(renderState, poseStack, nodeCollector, cameraRenderState);
    }
}
