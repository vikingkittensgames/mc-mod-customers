package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerCounterCacheTest {
    private static final BlockPos SPAWNER_POSITION = BlockPos.ZERO;
    private static final BlockPos COUNTER_POSITION =
            new BlockPos(1, 0, 0);

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void lazilyScansOnceAndReturnsTheCachedPositions() {
        Level level = levelWithCounter();
        CustomerCounterCache cache = new CustomerCounterCache();

        assertEquals(
                List.of(COUNTER_POSITION),
                cache.getPositions(
                        level,
                        SPAWNER_POSITION,
                        Blocks.OAK_PLANKS.defaultBlockState(),
                        1,
                        Blocks.SPAWNER
                )
        );
        clearInvocations(level);

        assertEquals(
                List.of(COUNTER_POSITION),
                cache.getPositions(
                        level,
                        SPAWNER_POSITION,
                        Blocks.OAK_PLANKS.defaultBlockState(),
                        1,
                        Blocks.SPAWNER
                )
        );
        verify(level, never()).getBlockState(any(BlockPos.class));
    }

    @Test
    void cachesAnEmptyResult() {
        Level level = mock(Level.class);
        when(level.getBlockState(any(BlockPos.class)))
                .thenReturn(Blocks.AIR.defaultBlockState());
        CustomerCounterCache cache = new CustomerCounterCache();

        assertTrue(cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.OAK_PLANKS.defaultBlockState(),
                1,
                Blocks.SPAWNER
        ).isEmpty());
        clearInvocations(level);

        assertTrue(cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.OAK_PLANKS.defaultBlockState(),
                1,
                Blocks.SPAWNER
        ).isEmpty());
        verify(level, never()).getBlockState(any(BlockPos.class));
    }

    @Test
    void invalidationCausesTheNextQueryToScanAgain() {
        Level level = levelWithCounter();
        CustomerCounterCache cache = new CustomerCounterCache();
        cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.OAK_PLANKS.defaultBlockState(),
                1,
                Blocks.SPAWNER
        );
        clearInvocations(level);

        cache.invalidate();
        cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.OAK_PLANKS.defaultBlockState(),
                1,
                Blocks.SPAWNER
        );

        verify(level, atLeastOnce()).getBlockState(any(BlockPos.class));
    }

    @Test
    void changedLookupParametersCauseAnotherScan() {
        Level level = levelWithCounter();
        CustomerCounterCache cache = new CustomerCounterCache();
        cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.OAK_PLANKS.defaultBlockState(),
                1,
                Blocks.SPAWNER
        );
        clearInvocations(level);

        cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.BRICKS.defaultBlockState(),
                2,
                Blocks.SPAWNER
        );

        verify(level, atLeastOnce()).getBlockState(any(BlockPos.class));
    }

    @Test
    void airCounterDefinitionCachesEmptyWithoutScanning() {
        Level level = mock(Level.class);
        CustomerCounterCache cache = new CustomerCounterCache();

        assertTrue(cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.AIR.defaultBlockState(),
                64,
                Blocks.SPAWNER
        ).isEmpty());
        assertTrue(cache.getPositions(
                level,
                SPAWNER_POSITION,
                Blocks.AIR.defaultBlockState(),
                64,
                Blocks.SPAWNER
        ).isEmpty());
        verify(level, never()).getBlockState(any(BlockPos.class));
    }

    private static Level levelWithCounter() {
        Level level = mock(Level.class);
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(invocation ->
                        COUNTER_POSITION.equals(
                                invocation.getArgument(0)
                        )
                                ? Blocks.OAK_PLANKS.defaultBlockState()
                                : Blocks.AIR.defaultBlockState());
        return level;
    }
}
