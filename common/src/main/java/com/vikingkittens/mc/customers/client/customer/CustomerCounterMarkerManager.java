package com.vikingkittens.mc.customers.client.customer;

import java.util.List;

import net.minecraft.core.BlockPos;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;

final class CustomerCounterMarkerManager {
    static final long DURATION_MILLIS = 90_000L;
    private static final long ROTATION_DURATION_MILLIS = 4_500L;
    private static final float ROTATION_DEGREES_PER_MILLISECOND = 0.08F;
    private static final double BOB_RADIANS_PER_MILLISECOND = 0.002D;
    private static final float BOB_DISTANCE = 0.1F;

    private static List<CustomerCounterMarker> markers = List.of();
    private static List<BlockPos> surroundingPositions = List.of();
    private static long expiresAt;

    private CustomerCounterMarkerManager() {
    }

    static void show(List<CustomerCounterMarker> newMarkers, List<BlockPos> newSurroundingPositions, long currentTime) {
        markers = List.copyOf(newMarkers);
        surroundingPositions = List.copyOf(newSurroundingPositions);
        expiresAt = currentTime + DURATION_MILLIS;
    }

    static List<CustomerCounterMarker> get(long currentTime) {
        if (currentTime >= expiresAt) {
            clear();
        }
        return markers;
    }

    static List<BlockPos> getSurroundingPositions(long currentTime) {
        get(currentTime);
        return surroundingPositions;
    }

    static float getRotationDegrees(long currentTime) {
        return (currentTime % ROTATION_DURATION_MILLIS)
                * ROTATION_DEGREES_PER_MILLISECOND;
    }

    static float getBobOffset(long currentTime) {
        return (float) Math.sin(currentTime * BOB_RADIANS_PER_MILLISECOND)
                * BOB_DISTANCE;
    }

    static void clear() {
        markers = List.of();
        surroundingPositions = List.of();
        expiresAt = 0L;
    }
}
