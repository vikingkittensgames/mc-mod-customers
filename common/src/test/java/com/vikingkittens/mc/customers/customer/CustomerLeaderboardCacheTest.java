package com.vikingkittens.mc.customers.customer;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerLeaderboardCacheTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @AfterEach
    void clearCache() {
        CustomerLeaderboardCache.clear();
    }

    @Test
    void findsTheClosestCachedLeaderboardInTheConfiguredCube() {
        Level level = mock(Level.class);
        Level otherLevel = mock(Level.class);
        BlockPos center = BlockPos.ZERO;
        CustomerLeaderboardBlockEntity farther =
                mock(CustomerLeaderboardBlockEntity.class);
        CustomerLeaderboardBlockEntity closer =
                mock(CustomerLeaderboardBlockEntity.class);
        CustomerLeaderboardBlockEntity outside =
                mock(CustomerLeaderboardBlockEntity.class);
        CustomerLeaderboardBlockEntity otherDimension =
                mock(CustomerLeaderboardBlockEntity.class);

        CustomerLeaderboardCache.update(
                level,
                new BlockPos(8, 0, 0),
                farther
        );
        CustomerLeaderboardCache.update(
                level,
                new BlockPos(3, 0, 0),
                closer
        );
        CustomerLeaderboardCache.update(
                level,
                new BlockPos(65, 0, 0),
                outside
        );
        CustomerLeaderboardCache.update(
                otherLevel,
                BlockPos.ZERO,
                otherDimension
        );

        assertSame(
                closer,
                CustomerLeaderboardCache.findClosest(level, center, 64)
        );
    }

    @Test
    void preservesTheExistingCubeDistanceSemantics() {
        Level level = mock(Level.class);
        CustomerLeaderboardBlockEntity corner =
                mock(CustomerLeaderboardBlockEntity.class);

        CustomerLeaderboardCache.update(
                level,
                new BlockPos(64, 64, 64),
                corner
        );

        assertSame(
                corner,
                CustomerLeaderboardCache.findClosest(
                        level,
                        BlockPos.ZERO,
                        64
                )
        );
    }

    @Test
    void revisionChangesOnlyWhenCachedContentsChange() {
        Level level = mock(Level.class);
        BlockPos position = new BlockPos(8, 64, 8);
        CustomerLeaderboardBlockEntity leaderboard =
                mock(CustomerLeaderboardBlockEntity.class);
        long initialRevision = CustomerLeaderboardCache.getRevision();

        CustomerLeaderboardCache.update(level, position, leaderboard);
        assertEquals(
                initialRevision + 1,
                CustomerLeaderboardCache.getRevision()
        );

        CustomerLeaderboardCache.update(level, position, leaderboard);
        assertEquals(
                initialRevision + 1,
                CustomerLeaderboardCache.getRevision()
        );

        CustomerLeaderboardCache.remove(level, position);
        assertEquals(
                initialRevision + 2,
                CustomerLeaderboardCache.getRevision()
        );

        CustomerLeaderboardCache.remove(level, position);
        assertEquals(
                initialRevision + 2,
                CustomerLeaderboardCache.getRevision()
        );
    }

    @Test
    void chunkLifecycleAddsAndRemovesLeaderboards() {
        Level level = mock(Level.class);
        LevelChunk chunk = mock(LevelChunk.class);
        BlockPos position = new BlockPos(8, 64, 8);
        CustomerLeaderboardBlockEntity leaderboard =
                mock(CustomerLeaderboardBlockEntity.class);
        when(chunk.getBlockEntities())
                .thenReturn(Map.of(position, leaderboard));

        CustomerLeaderboardCache.onChunkLoaded(level, chunk);

        assertSame(
                leaderboard,
                CustomerLeaderboardCache.findClosest(level, position, 64)
        );

        CustomerLeaderboardCache.onChunkUnloaded(level, chunk);

        assertNull(
                CustomerLeaderboardCache.findClosest(level, position, 64)
        );
    }

    @Test
    void leaderboardLookupUsesTheCacheWithoutScanningBlocks() {
        Level level = mock(Level.class);
        BlockPos position = new BlockPos(8, 64, 8);
        CustomerLeaderboardBlockEntity leaderboard =
                mock(CustomerLeaderboardBlockEntity.class);
        CustomerLeaderboardCache.update(level, position, leaderboard);

        assertSame(
                leaderboard,
                CustomerLeaderboardBlockEntity.findClosest(
                        level,
                        BlockPos.ZERO,
                        64
                )
        );
        verify(level, never()).getBlockState(any(BlockPos.class));
    }

    @Test
    void removedBlockEntityRemovesItselfFromTheCache() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        ServerLevel level = mock(ServerLevel.class);
        BlockPos position = new BlockPos(8, 64, 8);
        CustomerLeaderboardBlockEntity leaderboard =
                new CustomerLeaderboardBlockEntity(type, position, state);
        leaderboard.setLevel(level);
        CustomerLeaderboardCache.update(level, position, leaderboard);

        leaderboard.setRemoved();

        assertNull(
                CustomerLeaderboardCache.findClosest(level, position, 64)
        );
    }
}
