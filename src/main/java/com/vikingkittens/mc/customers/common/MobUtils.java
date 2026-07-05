package com.vikingkittens.mc.customers.common;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class MobUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_RANDOM_SPAWN_ATTEMPTS = 10;

    private MobUtils() {
    }

    @Nullable
    public static BlockPos getRandomSpawnPos(Level level, BlockPos centerPos, int radius, int requiredAirBlocks) {
        BlockPos.MutableBlockPos safePos = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < MAX_RANDOM_SPAWN_ATTEMPTS; attempt++) {
            int randomX = centerPos.getX() + level.getRandom().nextIntBetweenInclusive(-radius, radius);
            int randomZ = centerPos.getZ() + level.getRandom().nextIntBetweenInclusive(-radius, radius);
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomX, randomZ);
            safePos.set(randomX, groundY, randomZ);

            int airCount = 0;
            for (int yOffset = 0; yOffset < requiredAirBlocks; yOffset++) {
                BlockPos checkPos = safePos.above(yOffset);
                if (level.isEmptyBlock(checkPos)) {
                    airCount++;
                }
            }

            LOGGER.debug(
                    "Finding Safe Pos: attempt={}, target={}, pos={}, airCount={}, requiredAirBlocks={}",
                    attempt,
                    centerPos,
                    safePos,
                    airCount,
                    requiredAirBlocks
            );
            if (airCount >= requiredAirBlocks) {
                return safePos.immutable();
            }
        }
        return null;
    }
}
