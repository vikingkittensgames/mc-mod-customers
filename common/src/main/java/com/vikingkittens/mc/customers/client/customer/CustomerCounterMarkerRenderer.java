package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;

import com.vikingkittens.mc.customers.client.compatability.DebugBoxC;
import com.vikingkittens.mc.customers.client.compatability.RenderingCUtils;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

public final class CustomerCounterMarkerRenderer {
    private static final float SCALE = 0.5F;
    private static final double HEIGHT_OFFSET = 1.25D;
    private static final float SURROUNDING_SCALE = 0.25F;
    private static final float SURROUNDING_RED = 54.0F / 255.0F;
    private static final float SURROUNDING_GREEN = 153.0F / 255.0F;
    private static final float SURROUNDING_BLUE = 28.0F / 255.0F;
    private static final float SURROUNDING_ALPHA = 0.6F;
    private static final float MARKER_ALPHA = 0.85F;

    public static void render(
            PoseStack poseStack,
            CameraRenderState cameraRenderState,
            Matrix4f modelViewMatrix
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
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
        int surroundingColor = ((int) (SURROUNDING_ALPHA * 255.0F) << 24)
                | ((int) (SURROUNDING_RED * 255.0F) << 16)
                | ((int) (SURROUNDING_GREEN * 255.0F) << 8)
                | (int) (SURROUNDING_BLUE * 255.0F);
        double halfScale = SURROUNDING_SCALE / 2.0D;
        List<DebugBoxC> markerBoxes = markers.stream()
                .map(marker -> markerBox(marker, bob))
                .toList();
        List<DebugBoxC> surroundingBoxes = surroundingPositions.stream()
                .map(position -> {
                    double centerX = position.getX() + 0.5D;
                    double centerY = position.getY() + HEIGHT_OFFSET + bob;
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
        RenderingCUtils.renderDebugBoxes(
                poseStack,
                cameraRenderState,
                modelViewMatrix,
                java.util.stream.Stream.concat(markerBoxes.stream(), surroundingBoxes.stream()).toList()
        );
    }

    private static DebugBoxC markerBox(CustomerCounterMarker marker, float bob) {
        double centerX = marker.position().getX() + 0.5D;
        double centerY = marker.position().getY() + HEIGHT_OFFSET + bob;
        double centerZ = marker.position().getZ() + 0.5D;
        double halfScale = SCALE / 2.0D;
        return new DebugBoxC(
                new AABB(
                        centerX - halfScale,
                        centerY - halfScale,
                        centerZ - halfScale,
                        centerX + halfScale,
                        centerY + halfScale,
                        centerZ + halfScale
                ),
                markerColor(marker.spawnerMode())
        );
    }

    private static int markerColor(CustomerSpawnerMode mode) {
        int rgb = switch (mode) {
            case CONTINUOUS -> 0x36A3FF;
            case MANUAL -> 0xB66CFF;
            case DAY -> 0xFFE066;
            case NIGHT -> 0x5965D8;
            case BREAKFAST -> 0xFFB347;
            case LUNCH -> 0x66CC66;
            case DINNER -> 0xE86A5A;
        };
        return ((int) (MARKER_ALPHA * 255.0F) << 24) | rgb;
    }
}
