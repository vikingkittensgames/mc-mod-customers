package com.vikingkittens.mc.customers.common;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class MobUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RANDOM_SPAWN_ATTEMPTS = 10;

    private MobUtils() {
    }

    static boolean hasValidSupportArea(Level level, BlockPos spawnPos) {
        return hasValidSupportArea(spawnPos, checkPos -> {
            BlockState state = level.getBlockState(checkPos);
            return isAllowedSupport(state) && level.isEmptyBlock(checkPos.above());
        });
    }

    static boolean hasValidSupportArea(BlockPos spawnPos, Predicate<BlockPos> validSupport) {
        BlockPos supportPos = spawnPos.below();

        for (int xStart = -1; xStart <= 0; xStart++) {
            for (int zStart = -1; zStart <= 0; zStart++) {
                boolean valid = true;

                for (int xOffset = 0; xOffset < 2 && valid; xOffset++) {
                    for (int zOffset = 0; zOffset < 2; zOffset++) {
                        BlockPos checkPos = supportPos.offset(
                                xStart + xOffset,
                                0,
                                zStart + zOffset
                        );
                        if (!validSupport.test(checkPos)) {
                            valid = false;
                            break;
                        }
                    }
                }

                if (valid) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isAllowedSupport(BlockState state) {
        return state.isSolid() ||
                state.getBlock() instanceof SlabBlock ||
                state.getBlock() instanceof CarpetBlock ||
                state.getBlock() instanceof StairBlock;
    }

    @Nullable
    public static BlockPos getRandomSpawnPos(
            Level level,
            BlockPos centerPos,
            int radius,
            int requiredAirBlocks,
            int maxAttempts,
            Predicate<BlockPos> isValid
    ) {
        BlockPos.MutableBlockPos safePos = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int randomX = centerPos.getX() + level.getRandom().nextIntBetweenInclusive(-radius, radius);
            int randomZ = centerPos.getZ() + level.getRandom().nextIntBetweenInclusive(-radius, radius);
            int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE, randomX, randomZ);
            boolean outsideVerticalRadius = Math.abs((long) groundY - centerPos.getY()) > radius;
            int finalY = outsideVerticalRadius ? centerPos.getY() : groundY;
            int yStep = Integer.compare(finalY, groundY);

            for (int y = groundY; ; y += yStep) {
                if (Math.abs((long) y - centerPos.getY()) <= radius) {
                    safePos.set(randomX, y, randomZ);

                    int airCount = 0;
                    for (int yOffset = 0; yOffset < requiredAirBlocks; yOffset++) {
                        BlockPos checkPos = safePos.above(yOffset);
                        if (level.isEmptyBlock(checkPos)) {
                            airCount++;
                        }
                    }

                    if (
                            airCount >= requiredAirBlocks &&
                            hasValidSupportArea(level, safePos) &&
                            isValid.test(safePos)
                    ) {
                        return safePos.immutable();
                    }
                }

                if (y == finalY) {
                    break;
                }
            }
        }
        return null;
    }

    public static BlockPos getRandomSpawnPos(
            Level level,
            BlockPos centerPos,
            int radius,
            int requiredAirBlocks
    ) {
        return getRandomSpawnPos(level, centerPos, radius, requiredAirBlocks, MAX_RANDOM_SPAWN_ATTEMPTS, (pos) -> true);
    }
}
