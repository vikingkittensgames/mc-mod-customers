package com.vikingkittens.mc.customers.client.common;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class SpawnerIconRenderer {
    public static final float HEIGHT = 1.0F + 0.1F / 16.0F;

    private SpawnerIconRenderer() {}

    public static void render(ResourceLocation icon, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(icon));
        Matrix4f pose = poseStack.last().pose();
        PoseStack.Pose currentPose = poseStack.last();
        vertex(vertices, pose, currentPose, 0.0F, 0.0F, 0.0F, 1.0F, light, overlay);
        vertex(vertices, pose, currentPose, 0.0F, 1.0F, 0.0F, 0.0F, light, overlay);
        vertex(vertices, pose, currentPose, 1.0F, 1.0F, 1.0F, 0.0F, light, overlay);
        vertex(vertices, pose, currentPose, 1.0F, 0.0F, 1.0F, 1.0F, light, overlay);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f pose, PoseStack.Pose normal, float x, float z,
            float u, float v, int light, int overlay) {
        vertices.addVertex(pose, x, HEIGHT, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normal, 0.0F, 1.0F, 0.0F);
    }
}
