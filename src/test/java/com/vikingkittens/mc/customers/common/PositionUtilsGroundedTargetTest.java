package com.vikingkittens.mc.customers.common;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PositionUtilsGroundedTargetTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void keepsPositionAlreadyAboveGround() {
        LevelReader level = createLevel(y -> y >= 64);

        assertEquals(
                new BlockPos(10, 64, 20),
                PositionUtils.findGroundedTargetPosition(
                        level,
                        new BlockPos(10, 64, 20)
                )
        );
    }

    @Test
    void descendsThroughAirToGround() {
        LevelReader level = createLevel(y -> y >= 64);

        assertEquals(
                new BlockPos(10, 64, 20),
                PositionUtils.findGroundedTargetPosition(
                        level,
                        new BlockPos(10, 80, 20)
                )
        );
    }

    @Test
    void risesOutOfBlocksBeforeFindingGroundSurface() {
        LevelReader level = createLevel(y -> y >= 64);

        assertEquals(
                new BlockPos(10, 64, 20),
                PositionUtils.findGroundedTargetPosition(
                        level,
                        new BlockPos(10, 60, 20)
                )
        );
    }

    @Test
    void rejectsPositionWithoutTwoAirBlocksAboveGround() {
        LevelReader level = createLevel(y -> y == 64);

        assertNull(PositionUtils.findGroundedTargetPosition(
                level,
                new BlockPos(10, 64, 20)
        ));
    }

    @Test
    void rejectsPositionWithoutGroundBeforeMinimumBuildHeight() {
        LevelReader level = createLevel(y -> true);

        assertNull(PositionUtils.findGroundedTargetPosition(
                level,
                new BlockPos(10, 80, 20)
        ));
    }

    @Test
    void rejectsPositionWithoutAirBeforeMaximumBuildHeight() {
        LevelReader level = createLevel(y -> false);

        assertNull(PositionUtils.findGroundedTargetPosition(
                level,
                new BlockPos(10, 60, 20)
        ));
    }

    private static LevelReader createLevel(IntPredicate airAtY) {
        LevelReader level = mock(LevelReader.class);
        BlockState air = mock(BlockState.class);
        BlockState solid = mock(BlockState.class);
        when(air.isAir()).thenReturn(true);
        when(solid.isAir()).thenReturn(false);
        when(level.getMinBuildHeight()).thenReturn(0);
        when(level.getMaxBuildHeight()).thenReturn(256);
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation ->
                airAtY.test(invocation.<BlockPos>getArgument(0).getY())
                        ? air
                        : solid
        );
        return level;
    }
}
