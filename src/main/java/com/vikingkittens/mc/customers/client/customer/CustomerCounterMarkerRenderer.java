package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlock;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
    private static final float ROTATION_DEGREES_PER_TICK = 4.0F;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        List<CustomerCounterMarker> markers = CustomerCounterMarkerManager.get(Util.getMillis());
        if (markers.isEmpty()) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float animationTick = minecraft.level.getGameTime() + partialTick;
        float bob = (float)Math.sin(animationTick * 0.1F) * 0.1F;
        Vec3 cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();

        for (CustomerCounterMarker marker : markers) {
            BlockState markerState = CustomerSpawnerBlock.getMarkerState(marker.spawnerMode());

            poseStack.pushPose();
            poseStack.translate(
                    marker.position().getX() + 0.5D - cameraPosition.x(),
                    marker.position().getY() + 1.75D + bob - cameraPosition.y(),
                    marker.position().getZ() + 0.5D - cameraPosition.z()
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    animationTick * ROTATION_DEGREES_PER_TICK
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
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CustomerCounterMarkerManager.clear();
    }
}
