package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Provides version-compatible entity creation, positioning, and mounting.
 */
public final class EntityCUtils {
    private EntityCUtils() {
    }
    public static <T extends Entity> T create(
            EntityType<T> entityType,
            Level level
    ) {
        return entityType.create(level);
    }
    public static void snapTo(
            Entity entity,
            Vec3 position,
            float yRotation,
            float xRotation
    ) {
        entity.moveTo(
                position.x,
                position.y,
                position.z,
                yRotation,
                xRotation
        );
    }
    public static void snapTo(
            Entity entity,
            BlockPos position,
            float yRotation,
            float xRotation
    ) {
        entity.moveTo(position, yRotation, xRotation);
    }
    public static boolean startRiding(
            Entity passenger,
            Entity vehicle,
            boolean force
    ) {
        return passenger.startRiding(vehicle, force);
    }
}
