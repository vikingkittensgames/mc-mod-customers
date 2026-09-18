package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerSpawnerCacheTest {
    private static final BlockPos FIRST_SPAWNER = new BlockPos(10, 64, 10);

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @AfterEach
    void clearCache() {
        CustomerSpawnerCache.clear();
    }

    @Test
    void updateAddsAndRefreshesTheSpawnerCounterBlock() {
        Level level = mock(Level.class);
        CustomerSpawnerBlockEntity spawner = mock(CustomerSpawnerBlockEntity.class);
        when(level.getBlockEntity(FIRST_SPAWNER)).thenReturn(spawner);
        when(level.getBlockState(FIRST_SPAWNER.above()))
                .thenReturn(Blocks.OAK_PLANKS.defaultBlockState())
                .thenReturn(Blocks.BRICKS.defaultBlockState());

        CustomerSpawnerCache.update(level, FIRST_SPAWNER);
        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1)
                .getFirst()
                .counterBlockState()
                .is(Blocks.OAK_PLANKS));

        CustomerSpawnerCache.update(level, FIRST_SPAWNER);
        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1)
                .getFirst()
                .counterBlockState()
                .is(Blocks.BRICKS));
    }

    @Test
    void updateRemovesAnEntryWhoseSpawnerNoLongerExists() {
        Level level = mock(Level.class);
        CustomerSpawnerBlockEntity spawner = mock(CustomerSpawnerBlockEntity.class);
        when(level.getBlockEntity(FIRST_SPAWNER)).thenReturn(spawner).thenReturn(null);
        when(level.getBlockState(FIRST_SPAWNER.above())).thenReturn(Blocks.OAK_PLANKS.defaultBlockState());

        CustomerSpawnerCache.update(level, FIRST_SPAWNER);
        CustomerSpawnerCache.update(level, FIRST_SPAWNER);

        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1).isEmpty());
    }

    @Test
    void findsOnlySameLevelSpawnersInsideTheSphericalDistance() {
        Level level = mock(Level.class);
        Level otherLevel = mock(Level.class);
        BlockPos exactlyFiveBlocksAway = FIRST_SPAWNER.offset(3, 4, 0);
        BlockPos moreThanFiveBlocksAway = FIRST_SPAWNER.offset(4, 4, 0);
        CustomerSpawnerCache.update(level, FIRST_SPAWNER, Blocks.OAK_PLANKS.defaultBlockState());
        CustomerSpawnerCache.update(level, exactlyFiveBlocksAway, Blocks.BRICKS.defaultBlockState());
        CustomerSpawnerCache.update(level, moreThanFiveBlocksAway, Blocks.STONE.defaultBlockState());
        CustomerSpawnerCache.update(otherLevel, FIRST_SPAWNER, Blocks.GOLD_BLOCK.defaultBlockState());

        List<CustomerSpawnerCache.Value> values =
                CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 5);

        assertEquals(
                List.of(FIRST_SPAWNER, exactlyFiveBlocksAway),
                values.stream().map(CustomerSpawnerCache.Value::spawnerPosition).sorted().toList()
        );
    }

    @Test
    void returnsAnEmptyListWhenNoSpawnerIsNearThePosition() {
        assertTrue(CustomerSpawnerCache
                .getValuesNearPosition(mock(Level.class), BlockPos.ZERO, 64)
                .isEmpty());
    }

    @Test
    void placingAndBreakingTheBlockAboveASpawnerUpdatesItsCounterType() {
        ServerLevel level = mock(ServerLevel.class);
        CustomerSpawnerBlockEntity spawner = mock(CustomerSpawnerBlockEntity.class);
        when(level.getBlockEntity(FIRST_SPAWNER)).thenReturn(spawner);
        CustomerSpawnerCache.update(level, FIRST_SPAWNER, Blocks.OAK_PLANKS.defaultBlockState());

        CustomerSpawnerCache.onBlockPlaced(
                level,
                FIRST_SPAWNER.above(),
                Blocks.BRICKS.defaultBlockState(),
                null,
                64,
                ignored -> {}
        );
        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1)
                .getFirst()
                .counterBlockState()
                .is(Blocks.BRICKS));

        CustomerSpawnerCache.onBlockBroken(level, FIRST_SPAWNER.above(), Blocks.BRICKS.defaultBlockState(), 64);
        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1)
                .getFirst()
                .counterBlockState()
                .isAir());
    }

    @Test
    void breakingASpawnerRemovesItEvenWhileItsBlockEntityIsStillAccessible() {
        ServerLevel level = mock(ServerLevel.class);
        CustomerSpawnerBlockEntity spawner = mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlock spawnerBlock = mock(CustomerSpawnerBlock.class);
        BlockState spawnerState = mock(BlockState.class);
        when(spawnerState.getBlock()).thenReturn(spawnerBlock);
        when(level.getBlockEntity(FIRST_SPAWNER)).thenReturn(spawner);
        CustomerSpawnerCache.update(level, FIRST_SPAWNER, Blocks.OAK_PLANKS.defaultBlockState());

        CustomerSpawnerCache.onBlockBroken(level, FIRST_SPAWNER, spawnerState, 64);

        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1).isEmpty());
    }

    @Test
    void matchingPlacementEmitsForEveryNearbySpawnerAndAttributesThePlayer() {
        ServerLevel level = mock(ServerLevel.class);
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        BlockPos secondSpawnerPosition = FIRST_SPAWNER.offset(8, 0, 0);
        CustomerSpawnerBlockEntity firstSpawner = spawner(CustomerSpawnerMode.LUNCH);
        CustomerSpawnerBlockEntity secondSpawner = spawner(CustomerSpawnerMode.DINNER);
        Map<BlockPos, BlockEntity> blockEntities = Map.of(
                FIRST_SPAWNER,
                firstSpawner,
                secondSpawnerPosition,
                secondSpawner
        );
        when(level.getBlockEntity(any(BlockPos.class)))
                .thenAnswer(invocation -> blockEntities.get(invocation.<BlockPos>getArgument(0)));
        when(player.getUUID()).thenReturn(playerId);
        CustomerSpawnerCache.update(level, FIRST_SPAWNER, Blocks.OAK_PLANKS.defaultBlockState());
        CustomerSpawnerCache.update(level, secondSpawnerPosition, Blocks.OAK_PLANKS.defaultBlockState());
        List<CustomerInternalEvents.CounterBlockPlaced> events = new ArrayList<>();
        BlockPos placedPosition = FIRST_SPAWNER.offset(4, 0, 0);

        CustomerSpawnerCache.onBlockPlaced(
                level,
                placedPosition,
                Blocks.OAK_PLANKS.defaultBlockState(),
                player,
                64,
                events::add
        );

        assertEquals(2, events.size());
        assertTrue(events.stream().allMatch(event -> event.playerId().equals(playerId)));
        assertTrue(events.stream().allMatch(event -> event.counterBlockPosition().equals(placedPosition)));
        assertTrue(events.stream().allMatch(event -> event.counterBlockState().is(Blocks.OAK_PLANKS)));
        assertEquals(
                List.of(FIRST_SPAWNER, secondSpawnerPosition),
                events.stream().map(CustomerInternalEvents.CounterBlockPlaced::spawnerPosition).sorted().toList()
        );
    }

    @Test
    void doesNotEmitForDifferentCounterTypeMissingPlayerOrOwnCounterDefinition() {
        ServerLevel level = mock(ServerLevel.class);
        Player player = mock(Player.class);
        CustomerSpawnerBlockEntity spawner = spawner(CustomerSpawnerMode.LUNCH);
        when(level.getBlockEntity(FIRST_SPAWNER)).thenReturn(spawner);
        CustomerSpawnerCache.update(level, FIRST_SPAWNER, Blocks.OAK_PLANKS.defaultBlockState());
        List<CustomerInternalEvents.CounterBlockPlaced> events = new ArrayList<>();

        CustomerSpawnerCache.onBlockPlaced(
                level,
                FIRST_SPAWNER.offset(2, 0, 0),
                Blocks.BRICKS.defaultBlockState(),
                player,
                64,
                events::add
        );
        CustomerSpawnerCache.onBlockPlaced(
                level,
                FIRST_SPAWNER.offset(3, 0, 0),
                Blocks.OAK_PLANKS.defaultBlockState(),
                null,
                64,
                events::add
        );
        CustomerSpawnerCache.onBlockPlaced(
                level,
                FIRST_SPAWNER.above(),
                Blocks.OAK_PLANKS.defaultBlockState(),
                player,
                64,
                events::add
        );

        assertTrue(events.isEmpty());
    }

    @Test
    void blockEntityLoadAndRemovalRegisterAndUnregisterTheSpawner() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState spawnerState = mock(BlockState.class);
        when(type.isValid(spawnerState)).thenReturn(true);
        CustomerSpawnerBlockEntity spawner = new CustomerSpawnerBlockEntity(type, FIRST_SPAWNER, spawnerState);
        ServerLevel level = mock(ServerLevel.class);
        when(level.getBlockState(FIRST_SPAWNER.above())).thenReturn(Blocks.OAK_PLANKS.defaultBlockState());
        spawner.setLevel(level);

        List<CustomerSpawnerCache.Value> loaded =
                CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1);
        spawner.setRemoved();

        assertEquals(1, loaded.size());
        assertSame(FIRST_SPAWNER, loaded.getFirst().spawnerPosition());
        assertTrue(CustomerSpawnerCache.getValuesNearPosition(level, FIRST_SPAWNER, 1).isEmpty());
    }

    private static CustomerSpawnerBlockEntity spawner(CustomerSpawnerMode mode) {
        CustomerSpawnerBlockEntity spawner = mock(CustomerSpawnerBlockEntity.class);
        when(spawner.getSpawnerMode()).thenReturn(mode);
        return spawner;
    }
}
