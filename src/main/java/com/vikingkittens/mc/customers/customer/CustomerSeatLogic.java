package com.vikingkittens.mc.customers.customer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class CustomerSeatLogic {
    private static final Vec3 CUSTOMER_VEHICLE_ATTACHMENT =
            new Vec3(0.0D, 0.7D, 0.0D);
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

    static Vec3 getCustomerVehicleAttachmentPoint() {
        return CUSTOMER_VEHICLE_ATTACHMENT;
    }

    static boolean isIgnoredSeatPositionEntityType(
            Class<? extends Entity> entityType
    ) {
        return ExperienceOrb.class.isAssignableFrom(entityType)
                || ItemEntity.class.isAssignableFrom(entityType);
    }

    static Vec3 getPassengerPosition(
            Vec3 seatPosition,
            Vec3 passengerAttachment
    ) {
        return seatPosition.subtract(passengerAttachment);
    }

    static Vec3 getDismountLocation(
            Vec3 seatPosition,
            float initialYRotation,
            Predicate<BlockPos> isAir
    ) {
        Vec3 facingDirection = Vec3.directionFromRotation(
                0.0F,
                initialYRotation
        );
        Vec3 behind = seatPosition.subtract(
                facingDirection.x,
                0.0D,
                facingDirection.z
        );
        Vec3 left = seatPosition.add(
                facingDirection.z,
                0.0D,
                -facingDirection.x
        );
        Vec3 right = seatPosition.add(
                -facingDirection.z,
                0.0D,
                facingDirection.x
        );

        for (Vec3 candidate : List.of(behind, left, right)) {
            if (isAir.test(BlockPos.containing(candidate))) {
                return candidate;
            }
        }
        return seatPosition;
    }
}
