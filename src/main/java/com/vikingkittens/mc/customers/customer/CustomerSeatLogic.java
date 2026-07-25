package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class CustomerSeatLogic {
    private CustomerSeatLogic() {
    }

    static boolean isSeatName(String blockPath) {
        String normalizedPath = blockPath.toLowerCase(Locale.ROOT);
        return normalizedPath.contains("seat")
                || normalizedPath.contains("chair")
                || normalizedPath.contains("stool")
                || normalizedPath.contains("bench")
                || normalizedPath.contains("cushion");
    }

    static boolean isSeatHeight(double height) {
        return height >= 1.0D / 3.0D && height <= 2.0D / 3.0D;
    }

    static double getBestSeatHeight(VoxelShape collisionShape) {
        if (collisionShape.isEmpty()) {
            return 0.5D;
        }

        Map<Double, Double> surfaceAreas = new HashMap<>();
        for (AABB box : collisionShape.toAabbs()) {
            double surfaceArea = (box.maxX - box.minX) * (box.maxZ - box.minZ);
            surfaceAreas.merge(box.maxY, surfaceArea, Double::sum);
        }

        return surfaceAreas.entrySet().stream()
                .max(Map.Entry.<Double, Double>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .orElse(0.5D);
    }

    static boolean shouldDiscardEmptySeat(int emptyTicks) {
        return emptyTicks >= 20 * 60 * 2;
    }
}