package com.vikingkittens.mc.customers.customer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class CustomerLeaderboardCache {
    private record Key(
            Level level,
            BlockPos leaderboardPosition
    ) {}

    private static final Map<Key, CustomerLeaderboardBlockEntity> CACHE =
            new HashMap<>();
    private static long revision;

    private CustomerLeaderboardCache() {}

    public static void update(Level level, BlockPos position) {
        if (level.getBlockEntity(position)
                instanceof CustomerLeaderboardBlockEntity leaderboard) {
            update(level, position, leaderboard);
        } else {
            remove(level, position);
        }
    }

    static void update(
            Level level,
            BlockPos position,
            CustomerLeaderboardBlockEntity leaderboard
    ) {
        Key key = new Key(level, position.immutable());
        CustomerLeaderboardBlockEntity previous =
                CACHE.put(key, leaderboard);
        if (previous != leaderboard) {
            revision++;
        }
    }

    public static void remove(Level level, BlockPos position) {
        if (CACHE.remove(new Key(level, position)) != null) {
            revision++;
        }
    }

    public static long getRevision() {
        return revision;
    }

    public static CustomerLeaderboardBlockEntity findClosest(
            Level level,
            BlockPos position,
            int maxDistance
    ) {
        return CACHE.entrySet().stream()
                .filter(entry -> entry.getKey().level() == level)
                .filter(entry -> isInsideCube(
                        entry.getKey().leaderboardPosition(),
                        position,
                        maxDistance
                ))
                .min(Comparator.comparingDouble(entry ->
                        entry.getKey()
                                .leaderboardPosition()
                                .distSqr(position)))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private static boolean isInsideCube(
            BlockPos candidate,
            BlockPos center,
            int maxDistance
    ) {
        return Math.abs((long)candidate.getX() - center.getX())
                        <= maxDistance
                && Math.abs((long)candidate.getY() - center.getY())
                        <= maxDistance
                && Math.abs((long)candidate.getZ() - center.getZ())
                        <= maxDistance;
    }

    public static void onBlockPlaced(
            Level level,
            BlockPos position,
            BlockState state
    ) {
        if (state.getBlock() instanceof CustomerLeaderboardBlock) {
            update(level, position);
        }
    }

    public static void onBlockBroken(
            Level level,
            BlockPos position,
            BlockState state
    ) {
        if (state.getBlock() instanceof CustomerLeaderboardBlock) {
            remove(level, position);
        }
    }

    public static void onChunkLoaded(Level level, LevelChunk chunk) {
        chunk.getBlockEntities().forEach((position, blockEntity) -> {
            if (blockEntity
                    instanceof CustomerLeaderboardBlockEntity leaderboard) {
                update(level, position, leaderboard);
            }
        });
    }

    public static void onChunkUnloaded(Level level, LevelChunk chunk) {
        chunk.getBlockEntities().forEach((position, blockEntity) -> {
            if (blockEntity instanceof CustomerLeaderboardBlockEntity) {
                remove(level, position);
            }
        });
    }

    static void clear() {
        if (!CACHE.isEmpty()) {
            CACHE.clear();
            revision++;
        }
    }
}
