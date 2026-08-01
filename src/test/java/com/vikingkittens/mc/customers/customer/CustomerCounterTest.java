package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerCounterTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void findsAirAboveSafeSupport() {
        BlockPos counter = new BlockPos(0, 10, 0);
        BlockPos target = new BlockPos(1, 9, 0);
        Map<BlockPos, BlockState> states = new HashMap<>();
        states.put(target, Blocks.AIR.defaultBlockState());
        states.put(target.above(), Blocks.AIR.defaultBlockState());
        states.put(target.above(2), Blocks.AIR.defaultBlockState());
        states.put(target.below(), Blocks.STONE.defaultBlockState());

        assertEquals(List.of(target), findPositions(counter, states, null));
    }

    @Test
    void ignoresDiagonalPosition() {
        BlockPos counter = new BlockPos(0, 10, 0);
        BlockPos diagonalPosition = new BlockPos(1, 9, 1);
        Map<BlockPos, BlockState> states = new HashMap<>();
        states.put(diagonalPosition, Blocks.AIR.defaultBlockState());
        states.put(diagonalPosition.above(), Blocks.AIR.defaultBlockState());
        states.put(diagonalPosition.above(2), Blocks.AIR.defaultBlockState());
        states.put(diagonalPosition.below(), Blocks.STONE.defaultBlockState());

        assertTrue(findPositions(counter, states, null).isEmpty());
    }
    @Test
    void rejectsAvoidBlockPosition() {
        BlockPos counter = new BlockPos(0, 10, 0);
        BlockPos target = new BlockPos(1, 9, 0);
        Map<BlockPos, BlockState> states = new HashMap<>();
        states.put(target, Blocks.RED_CARPET.defaultBlockState());
        states.put(target.above(), Blocks.AIR.defaultBlockState());
        states.put(target.above(2), Blocks.AIR.defaultBlockState());
        states.put(target.below(), Blocks.STONE.defaultBlockState());

        assertTrue(findPositions(
                counter,
                states,
                Blocks.RED_CARPET.defaultBlockState()
        ).isEmpty());
    }

    @Test
    void rejectsUnsafeSupport() {
        BlockPos counter = new BlockPos(0, 10, 0);
        BlockPos target = new BlockPos(1, 9, 0);
        Map<BlockPos, BlockState> states = new HashMap<>();
        states.put(target, Blocks.AIR.defaultBlockState());
        states.put(target.below(), Blocks.MAGMA_BLOCK.defaultBlockState());

        assertTrue(findPositions(counter, states, null).isEmpty());
    }

    @Test
    void usesSeatBlockForMarkerPosition() {
        BlockPos navigationPosition = new BlockPos(1, 9, 0);
        BlockPos seatPosition = navigationPosition.below();
        Level level = mock(Level.class);
        when(level.getBlockState(seatPosition))
                .thenReturn(Blocks.OAK_STAIRS.defaultBlockState());
        when(level.getEntities(
                any(Entity.class),
                any(AABB.class),
                any(Predicate.class)
        )).thenReturn(List.of());
        Entity collisionEntity = mock(Entity.class);

        assertEquals(
                seatPosition,
                CustomerCounter.getMarkerPosition(
                        level,
                        new CustomerCounter.SurroundingPosition(
                                BlockPos.ZERO,
                                navigationPosition
                        ),
                        collisionEntity,
                        null
                )
        );
    }

    @Test
    void keepsNavigationPositionForNonSeatMarker() {
        BlockPos navigationPosition = new BlockPos(1, 9, 0);
        BlockPos supportPosition = navigationPosition.below();
        Level level = mock(Level.class);
        when(level.getBlockState(supportPosition))
                .thenReturn(Blocks.STONE.defaultBlockState());
        Entity collisionEntity = mock(Entity.class);

        assertEquals(
                navigationPosition,
                CustomerCounter.getMarkerPosition(
                        level,
                        new CustomerCounter.SurroundingPosition(
                                BlockPos.ZERO,
                                navigationPosition
                        ),
                        collisionEntity,
                        null
                )
        );
    }

    @Test
    void usesLowCollisionBlockForMarkerPosition() {
        BlockPos navigationPosition = new BlockPos(1, 9, 0);
        BlockPos carpetPosition = navigationPosition.below();
        Level level = mock(Level.class);
        when(level.getBlockState(carpetPosition))
                .thenReturn(Blocks.BLUE_CARPET.defaultBlockState());

        assertEquals(
                carpetPosition,
                CustomerCounter.getMarkerPosition(
                        level,
                        new CustomerCounter.SurroundingPosition(
                                BlockPos.ZERO,
                                navigationPosition
                        ),
                        mock(Entity.class),
                        null
                )
        );
    }

    @Test
    void doesNotLowerMarkerIntoAir() {
        BlockPos navigationPosition = new BlockPos(1, 9, 0);
        Level level = mock(Level.class);
        when(level.getBlockState(navigationPosition.below()))
                .thenReturn(Blocks.AIR.defaultBlockState());

        assertEquals(
                navigationPosition,
                CustomerCounter.getMarkerPosition(
                        level,
                        new CustomerCounter.SurroundingPosition(
                                BlockPos.ZERO,
                                navigationPosition
                        ),
                        mock(Entity.class),
                        null
                )
        );
    }
    private static List<BlockPos> findPositions(
            BlockPos counter,
            Map<BlockPos, BlockState> states,
            BlockState avoidState
    ) {
        Level level = mock(Level.class);
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation ->
                states.getOrDefault(
                        invocation.getArgument(0),
                        Blocks.STONE.defaultBlockState()
                )
        );
        when(level.getEntities(
                any(Entity.class),
                any(AABB.class),
                any(Predicate.class)
        )).thenReturn(List.of());
        Entity collisionEntity = mock(Entity.class);

        return CustomerCounter.findValidSurroundingPositions(
                        level,
                        List.of(counter),
                        collisionEntity,
                        avoidState
                ).stream()
                .map(CustomerCounter.SurroundingPosition::getPosition)
                .toList();
    }
}
