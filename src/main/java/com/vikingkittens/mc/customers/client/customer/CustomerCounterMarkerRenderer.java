package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerCounterMarkerRenderer {
    private static final float SCALE = 0.5F;
    private static final double HEIGHT_OFFSET = 1.25D;
    private static final float SURROUNDING_SCALE = 0.25F;
    private static final float SURROUNDING_RED = 54.0F / 255.0F;
    private static final float SURROUNDING_GREEN = 153.0F / 255.0F;
    private static final float SURROUNDING_BLUE = 28.0F / 255.0F;
    private static final float SURROUNDING_ALPHA = 0.6F;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterEntities event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        CameraRenderState cameraRenderState =
                event.getLevelRenderState().cameraRenderState;
        if (!cameraRenderState.initialized) {
            return;
        }

        long currentTime = Util.getMillis();
        List<CustomerCounterMarker> markers = CustomerCounterMarkerManager.get(currentTime);
        List<BlockPos> surroundingPositions = CustomerCounterMarkerManager.getSurroundingPositions(currentTime);
        if (markers.isEmpty() && surroundingPositions.isEmpty()) {
            return;
        }

        float bob = CustomerCounterMarkerManager.getBobOffset(currentTime);
        Vec3 cameraPosition = cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();

        for (CustomerCounterMarker marker : markers) {
            BlockState markerState = CustomerSpawnerBlock.getMarkerState(marker.spawnerMode());

            poseStack.pushPose();
            poseStack.translate(
                    marker.position().getX() + 0.5D - cameraPosition.x(),
                    marker.position().getY() + HEIGHT_OFFSET + bob - cameraPosition.y(),
                    marker.position().getZ() + 0.5D - cameraPosition.z()
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    CustomerCounterMarkerManager.getRotationDegrees(currentTime)
            ));
            poseStack.scale(SCALE, SCALE, SCALE);
            poseStack.translate(-0.5D, -0.5D, -0.5D);

            minecraft.getBlockRenderer().renderSingleBlock(
                    markerState,
                    poseStack,
                    bufferSource,
                    LevelRenderer.getLightColor(
                            minecraft.level,
                            marker.position().above()
                    ),
                    OverlayTexture.NO_OVERLAY
            );
            poseStack.popPose();
        }

        DrawableGizmoPrimitives surroundingBoxes = new DrawableGizmoPrimitives();
        int surroundingColor = ARGB.colorFromFloat(
                SURROUNDING_ALPHA,
                SURROUNDING_RED,
                SURROUNDING_GREEN,
                SURROUNDING_BLUE
        );
        for (BlockPos position : surroundingPositions) {
            Vec3 center = new Vec3(
                    position.getX() + 0.5D,
                    position.getY() + HEIGHT_OFFSET + bob - cameraPosition.y(),
                    position.getZ() + 0.5D
            );
            center = center.add(0.0D, cameraPosition.y(), 0.0D);
            for (Vec3[] face : getBoxFaces(center, SURROUNDING_SCALE)) {
                surroundingBoxes.addQuad(
                        face[0],
                        face[1],
                        face[2],
                        face[3],
                        surroundingColor
                );
            }
        }
        surroundingBoxes.render(
                poseStack,
                bufferSource,
                cameraRenderState,
                event.getModelViewMatrix()
        );
        bufferSource.endBatch();
    }

    /**
     * Creates the six quad faces for a filled box centered at the supplied position.
     */
    static List<Vec3[]> getBoxFaces(Vec3 center, double size) {
        double halfSize = size / 2.0D;
        double minX = center.x() - halfSize;
        double minY = center.y() - halfSize;
        double minZ = center.z() - halfSize;
        double maxX = center.x() + halfSize;
        double maxY = center.y() + halfSize;
        double maxZ = center.z() + halfSize;

        Vec3 minMinMin = new Vec3(minX, minY, minZ);
        Vec3 maxMinMin = new Vec3(maxX, minY, minZ);
        Vec3 maxMaxMin = new Vec3(maxX, maxY, minZ);
        Vec3 minMaxMin = new Vec3(minX, maxY, minZ);
        Vec3 minMinMax = new Vec3(minX, minY, maxZ);
        Vec3 maxMinMax = new Vec3(maxX, minY, maxZ);
        Vec3 maxMaxMax = new Vec3(maxX, maxY, maxZ);
        Vec3 minMaxMax = new Vec3(minX, maxY, maxZ);

        return List.of(
                new Vec3[]{minMinMin, maxMinMin, maxMaxMin, minMaxMin},
                new Vec3[]{maxMinMax, minMinMax, minMaxMax, maxMaxMax},
                new Vec3[]{minMinMax, minMinMin, minMaxMin, minMaxMax},
                new Vec3[]{maxMinMin, maxMinMax, maxMaxMax, maxMaxMin},
                new Vec3[]{minMinMax, maxMinMax, maxMinMin, minMinMin},
                new Vec3[]{minMaxMin, maxMaxMin, maxMaxMax, minMaxMax}
        );
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CustomerCounterMarkerManager.clear();
    }
}
