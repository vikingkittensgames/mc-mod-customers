package com.vikingkittens.mc.customers.common;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobUtilsTest {
    /**
     * Initializes Minecraft and NeoForge state before mocking levels.
     */
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void returnsFirstPositionWithClearanceAndAValidSupportArea() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 5, -5, -5, 5, -2);
        BlockPos center = new BlockPos(100, 64, 200);

        mockHeight(level, 95, 195, 67);
        mockHeight(level, 105, 198, 68);
        mockVerticalAir(level, new BlockPos(95, 67, 195), true, true, false);
        mockVerticalAir(level, new BlockPos(105, 68, 198), true, true, true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class, CALLS_REAL_METHODS)) {
            mobUtils.when(() -> MobUtils.hasValidSupportArea(eq(level), any(BlockPos.class)))
                    .thenReturn(true);

            BlockPos result = MobUtils.getRandomSpawnPos(level, center, 5, 3);

            assertEquals(new BlockPos(105, 68, 198), result);
        }

        verify(random, times(4)).nextIntBetweenInclusive(-5, 5);
        verify(level, times(2)).getHeight(
                eq(Heightmap.Types.WORLD_SURFACE),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()
        );
    }

    @Test
    void returnsNullAfterTenFailedRandomPositions() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 2, repeat(0, 20));
        BlockPos center = new BlockPos(8, 64, -12);

        mockHeight(level, 8, -12, 63);
        mockVerticalAir(level, new BlockPos(8, 63, -12), true, true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class, CALLS_REAL_METHODS)) {
            mobUtils.when(() -> MobUtils.hasValidSupportArea(eq(level), any(BlockPos.class)))
                    .thenReturn(false);

            assertNull(MobUtils.getRandomSpawnPos(level, center, 2, 2));
        }

        verify(random, times(20)).nextIntBetweenInclusive(-2, 2);
        verify(level, times(10)).getHeight(Heightmap.Types.WORLD_SURFACE, 8, -12);
    }

    @Test
    void requiredAirBlocksControlsHowManyVerticalBlocksMustBeClear() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 3, 1, -1, 2, -2);
        BlockPos center = new BlockPos(0, 70, 0);

        mockHeight(level, 1, -1, 71);
        mockHeight(level, 2, -2, 72);
        mockVerticalAir(level, new BlockPos(1, 71, -1), true, true, true, false);
        mockVerticalAir(level, new BlockPos(2, 72, -2), true, true, true, true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class, CALLS_REAL_METHODS)) {
            mobUtils.when(() -> MobUtils.hasValidSupportArea(eq(level), any(BlockPos.class)))
                    .thenReturn(true);

            BlockPos result = MobUtils.getRandomSpawnPos(level, center, 3, 4);

            assertEquals(new BlockPos(2, 72, -2), result);
        }

        verify(random, times(4)).nextIntBetweenInclusive(-3, 3);
    }

    @Test
    void worksWithASingleRequiredAirBlock() {
        Level level = mock(Level.class);
        RandomSource random = mockRandom(level, 1, -1, 1);
        BlockPos center = new BlockPos(-4, 40, 9);

        mockHeight(level, -5, 10, 41);
        mockVerticalAir(level, new BlockPos(-5, 41, 10), true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class, CALLS_REAL_METHODS)) {
            mobUtils.when(() -> MobUtils.hasValidSupportArea(eq(level), any(BlockPos.class)))
                    .thenReturn(true);

            BlockPos result = MobUtils.getRandomSpawnPos(level, center, 1, 1);

            assertEquals(new BlockPos(-5, 41, 10), result);
        }

        verify(random, times(2)).nextIntBetweenInclusive(-1, 1);
    }

    @Test
    void searchesDownFromHeightmapForValidPositionWithinVerticalRadius() {
        Level level = mock(Level.class);
        mockRandom(level, 3, 0, 0);
        BlockPos center = new BlockPos(10, 64, 20);
        BlockPos expected = new BlockPos(10, 66, 20);

        mockHeight(level, 10, 20, 80);
        mockVerticalAir(level, expected, true, true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class, CALLS_REAL_METHODS)) {
            mobUtils.when(() -> MobUtils.hasValidSupportArea(level, expected))
                    .thenReturn(true);

            assertEquals(expected, MobUtils.getRandomSpawnPos(level, center, 3, 2));
        }
    }

    @Test
    void searchesUpFromHeightmapForValidPositionWithinVerticalRadius() {
        Level level = mock(Level.class);
        mockRandom(level, 3, 0, 0);
        BlockPos center = new BlockPos(10, 64, 20);
        BlockPos expected = new BlockPos(10, 62, 20);

        mockHeight(level, 10, 20, 40);
        mockVerticalAir(level, expected, true, true);

        try (MockedStatic<MobUtils> mobUtils = mockStatic(MobUtils.class, CALLS_REAL_METHODS)) {
            mobUtils.when(() -> MobUtils.hasValidSupportArea(level, expected))
                    .thenReturn(true);

            assertEquals(expected, MobUtils.getRandomSpawnPos(level, center, 3, 2));
        }
    }

    @Test
    void stopsSearchingAtCenterYBeforeStartingAnotherAttempt() {
        Level level = mock(Level.class);
        mockRandom(level, 3, repeat(0, 20));
        BlockPos center = new BlockPos(10, 64, 20);

        mockHeight(level, 10, 20, 80);
        mockVerticalAir(level, new BlockPos(10, 63, 20), true, true);

        assertNull(MobUtils.getRandomSpawnPos(level, center, 3, 2));

        verify(level, never()).isEmptyBlock(new BlockPos(10, 63, 20));
    }

    @Test
    void acceptsCompleteTwoByTwoSupportArea() {
        BlockPos spawnPos = new BlockPos(10, 65, 20);
        BlockPos supportPos = spawnPos.below();
        Set<BlockPos> validSupports = Set.of(
                supportPos,
                supportPos.east(),
                supportPos.south(),
                supportPos.south().east()
        );

        assertTrue(MobUtils.hasValidSupportArea(spawnPos, validSupports::contains));
    }

    @Test
    void rejectsPositionWhenTheCandidateSupportIsInvalid() {
        BlockPos spawnPos = new BlockPos(10, 65, 20);
        BlockPos supportPos = spawnPos.below();
        Set<BlockPos> validSupports = Set.of(
                supportPos.east(),
                supportPos.south(),
                supportPos.south().east()
        );

        assertFalse(MobUtils.hasValidSupportArea(spawnPos, validSupports::contains));
    }

    @Test
    void rejectsPositionWhenEastAndWestCannotSupportAValidSquare() {
        BlockPos spawnPos = new BlockPos(10, 65, 20);
        BlockPos supportPos = spawnPos.below();
        Set<BlockPos> validSupports = Set.of(
                supportPos,
                supportPos.north(),
                supportPos.south()
        );

        assertFalse(MobUtils.hasValidSupportArea(spawnPos, validSupports::contains));
    }

    @Test
    void rejectsPositionWhenNorthAndSouthCannotSupportAValidSquare() {
        BlockPos spawnPos = new BlockPos(10, 65, 20);
        BlockPos supportPos = spawnPos.below();
        Set<BlockPos> validSupports = Set.of(
                supportPos,
                supportPos.east(),
                supportPos.west()
        );

        assertFalse(MobUtils.hasValidSupportArea(spawnPos, validSupports::contains));
    }

    @Test
    void acceptsAValidSquareInAnyOrientationAroundTheSupport() {
        BlockPos spawnPos = new BlockPos(10, 65, 20);
        BlockPos supportPos = spawnPos.below();
        Set<BlockPos> validSupports = Set.of(
                supportPos,
                supportPos.west(),
                supportPos.north(),
                supportPos.north().west()
        );

        assertTrue(MobUtils.hasValidSupportArea(spawnPos, validSupports::contains));
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
        when(level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)).thenReturn(y);
    }

    private static void mockVerticalAir(Level level, BlockPos basePos, boolean... airBlocks) {
        for (int i = 0; i < airBlocks.length; i++) {
            when(level.isEmptyBlock(basePos.above(i))).thenReturn(airBlocks[i]);
        }
    }
}
