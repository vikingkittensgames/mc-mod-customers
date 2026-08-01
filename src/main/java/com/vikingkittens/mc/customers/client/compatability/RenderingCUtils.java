package com.vikingkittens.mc.customers.client.compatability;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Provides version-compatible rendering submissions.
 */
public final class RenderingCUtils {
    private RenderingCUtils() {
    }

    public static void renderDebugBoxes(
            RenderLevelStageEvent event,
            List<DebugBoxC> boxes
    ) {
        if (boxes.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugFilledBox());
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPosition = event.getCamera().getPosition();

        for (DebugBoxC box : boxes) {
            AABB bounds = box.bounds();
            int color = box.color();
            LevelRenderer.addChainedFilledBoxVertices(
                    poseStack,
                    buffer,
                    bounds.minX - cameraPosition.x,
                    bounds.minY - cameraPosition.y,
                    bounds.minZ - cameraPosition.z,
                    bounds.maxX - cameraPosition.x,
                    bounds.maxY - cameraPosition.y,
                    bounds.maxZ - cameraPosition.z,
                    ((color >> 16) & 0xFF) / 255.0F,
                    ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F,
                    ((color >> 24) & 0xFF) / 255.0F
            );
        }
        bufferSource.endBatch();
    }

    static List<Vec3[]> getBoxFaces(AABB bounds) {
        Vec3 minMinMin = new Vec3(bounds.minX, bounds.minY, bounds.minZ);
        Vec3 maxMinMin = new Vec3(bounds.maxX, bounds.minY, bounds.minZ);
        Vec3 maxMaxMin = new Vec3(bounds.maxX, bounds.maxY, bounds.minZ);
        Vec3 minMaxMin = new Vec3(bounds.minX, bounds.maxY, bounds.minZ);
        Vec3 minMinMax = new Vec3(bounds.minX, bounds.minY, bounds.maxZ);
        Vec3 maxMinMax = new Vec3(bounds.maxX, bounds.minY, bounds.maxZ);
        Vec3 maxMaxMax = new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ);
        Vec3 minMaxMax = new Vec3(bounds.minX, bounds.maxY, bounds.maxZ);

        return List.of(
                new Vec3[]{minMinMin, maxMinMin, maxMaxMin, minMaxMin},
                new Vec3[]{maxMinMax, minMinMax, minMaxMax, maxMaxMax},
                new Vec3[]{minMinMax, minMinMin, minMaxMin, minMaxMax},
                new Vec3[]{maxMinMin, maxMinMax, maxMaxMax, maxMaxMin},
                new Vec3[]{minMinMax, maxMinMax, maxMinMin, minMinMin},
                new Vec3[]{minMaxMin, maxMaxMin, maxMaxMax, minMaxMax}
        );
    }
}
