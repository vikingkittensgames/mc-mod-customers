package com.vikingkittens.mc.customers.client.compatability;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.state.CameraRenderState;
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

        CameraRenderState cameraRenderState =
                event.getLevelRenderState().cameraRenderState;
        if (!cameraRenderState.initialized) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();
        DrawableGizmoPrimitives primitives =
                new DrawableGizmoPrimitives();

        for (DebugBoxC box : boxes) {
            for (Vec3[] face : getBoxFaces(box.bounds())) {
                primitives.addQuad(
                        face[0],
                        face[1],
                        face[2],
                        face[3],
                        box.color()
                );
            }
        }

        PoseStack poseStack = event.getPoseStack();
        primitives.render(
                poseStack,
                bufferSource,
                cameraRenderState,
                event.getModelViewMatrix()
        );
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
