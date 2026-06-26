package com.vikingkittens.mc.customers.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SearchUtils {
    public static List<BlockPos> findBlocksInSphere(Level level, BlockPos center, int radius, Predicate<BlockState> predicate) {
        List<BlockPos> matchingBlocks = new ArrayList<>();
        double radiusSq = radius * radius;

        BlockPos minPos = center.offset(-radius, -radius, -radius);
        BlockPos maxPos = center.offset(radius, radius, radius);

        BlockPos.betweenClosedStream(minPos, maxPos).forEach(pos -> {
            if (center.distSqr(pos) <= radiusSq) {
                BlockState state = level.getBlockState(pos);

                if (predicate.test(state)) {
                    matchingBlocks.add(pos.immutable());
                }
            }
        });

        return matchingBlocks;
    }

    public static <T extends Entity> List<T> findEntitiesInSphere(Level level, Class<T> entityClass, BlockPos center, double radius, Predicate<T> predicate) {
        double radiusSq = radius * radius;

        AABB boundingBox = new AABB(center).inflate(radius);

        return level.getEntitiesOfClass(entityClass, boundingBox, entity -> {
            if (!predicate.test(entity)) {
                return false;
            }
            return entity.distanceToSqr(center.getX(), center.getY(), center.getZ()) <= radiusSq;
        });
    }
}
