package com.vikingkittens.mc.customers.common;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobUtilsTest {
    @Test
    void returnsFirstPositionWithEnoughAirBlocksAfterSkippingInvalidRandomPositions() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 5, -5, -5, 0, 1, 5, -2);
        BlockPos center = new BlockPos(100, 64, 200);

        mockHeight(level, 95, 195, 70);
        mockHeight(level, 100, 201, 71);
        mockHeight(level, 105, 198, 72);
        mockVerticalAir(level, new BlockPos(95, 70, 195), true, true, false);
        mockVerticalAir(level, new BlockPos(105, 72, 198), true, true, true);

        BlockPos result = MobUtils.getRandomSpawnPos(level, center, 5, 3);

        assertEquals(new BlockPos(105, 72, 198), result);
        verify(random, times(6)).nextIntBetweenInclusive(-5, 5);
        verify(level, times(3)).getHeight(eq(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void returnsNullAfterTenFailedRandomPositions() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 2, repeat(0, 20));
        BlockPos center = new BlockPos(8, 64, -12);

        mockHeight(level, 8, -12, 63);

        BlockPos result = MobUtils.getRandomSpawnPos(level, center, 2, 2);

        assertNull(result);
        verify(random, times(20)).nextIntBetweenInclusive(-2, 2);
        verify(level, times(10)).getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 8, -12);
        verify(level, times(20)).isEmptyBlock(any(BlockPos.class));
    }

    @Test
    void requiredAirBlocksControlsHowManyVerticalBlocksMustBeClear() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 3, 1, -1, 2, -2);
        BlockPos center = new BlockPos(0, 70, 0);

        mockHeight(level, 1, -1, 80);
        mockHeight(level, 2, -2, 81);
        mockVerticalAir(level, new BlockPos(1, 80, -1), true, true, true, false);
        mockVerticalAir(level, new BlockPos(2, 81, -2), true, true, true, true);

        BlockPos result = MobUtils.getRandomSpawnPos(level, center, 3, 4);

        assertEquals(new BlockPos(2, 81, -2), result);
        verify(random, times(4)).nextIntBetweenInclusive(-3, 3);
    }

    @Test
    void worksWithASingleRequiredAirBlock() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 1, -1, 1);
        BlockPos center = new BlockPos(-4, 40, 9);

        mockHeight(level, -5, 10, 41);
        mockVerticalAir(level, new BlockPos(-5, 41, 10), true);

        BlockPos result = MobUtils.getRandomSpawnPos(level, center, 1, 1);

        assertEquals(new BlockPos(-5, 41, 10), result);
        verify(random, times(2)).nextIntBetweenInclusive(-1, 1);
        verify(level, times(1)).isEmptyBlock(new BlockPos(-5, 41, 10));
    }

    private static RandomSource mockRandom(Level level, int radius, int... offsets) {
        RandomSource random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);

        Integer firstOffset = offsets[0];
        Integer[] remainingOffsets = Arrays.stream(offsets).skip(1).boxed().toArray(Integer[]::new);
        when(random.nextIntBetweenInclusive(-radius, radius)).thenReturn(firstOffset, remainingOffsets);
        return random;
    }

    private static int[] repeat(int value, int times) {
        int[] values = new int[times];
        Arrays.fill(values, value);
        return values;
    }

    private static void mockHeight(Level level, int x, int z, int y) {
        when(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)).thenReturn(y);
    }

    private static void mockVerticalAir(Level level, BlockPos basePos, boolean... airBlocks) {
        for (int i = 0; i < airBlocks.length; i++) {
            when(level.isEmptyBlock(basePos.above(i))).thenReturn(airBlocks[i]);
        }
    }
}
