package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.compatability.DebugBoxC;
import com.vikingkittens.mc.customers.client.compatability.RenderingCUtils;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlock;

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

        bufferSource.endBatch();

        int surroundingColor = ARGB.colorFromFloat(
                SURROUNDING_ALPHA,
                SURROUNDING_RED,
                SURROUNDING_GREEN,
                SURROUNDING_BLUE
        );
        double halfScale = SURROUNDING_SCALE / 2.0D;
        List<DebugBoxC> surroundingBoxes = surroundingPositions.stream()
                .map(position -> {
                    double centerX = position.getX() + 0.5D;
                    double centerY =
                            position.getY() + HEIGHT_OFFSET + bob;
                    double centerZ = position.getZ() + 0.5D;
                    return new DebugBoxC(
                            new AABB(
                                    centerX - halfScale,
                                    centerY - halfScale,
                                    centerZ - halfScale,
                                    centerX + halfScale,
                                    centerY + halfScale,
                                    centerZ + halfScale
                            ),
                            surroundingColor
                    );
                })
                .toList();
        RenderingCUtils.renderDebugBoxes(event, surroundingBoxes);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CustomerCounterMarkerManager.clear();
    }
}
