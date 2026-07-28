package com.vikingkittens.mc.customers.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionUtilsTest {
    @ParameterizedTest
    @MethodSource("directionCases")
    void findsClosestHorizontalDirection(
            BlockPos start,
            BlockPos end,
            Direction expected
    ) {
        assertEquals(
                expected,
                PositionUtils.getClosestHorizontalDirection(start, end)
        );
    }

    static Stream<Arguments> directionCases() {
        BlockPos start = new BlockPos(10, 64, 10);
        return Stream.of(
                Arguments.of(start, new BlockPos(15, 64, 10), Direction.EAST),
                Arguments.of(start, new BlockPos(5, 64, 10), Direction.WEST),
                Arguments.of(start, new BlockPos(10, 64, 15), Direction.SOUTH),
                Arguments.of(start, new BlockPos(10, 64, 5), Direction.NORTH),
                Arguments.of(start, new BlockPos(15, 100, 12), Direction.EAST),
                Arguments.of(start, new BlockPos(5, 20, 8), Direction.WEST),
                Arguments.of(start, new BlockPos(12, 100, 15), Direction.SOUTH),
                Arguments.of(start, new BlockPos(8, 20, 5), Direction.NORTH),
                Arguments.of(start, new BlockPos(15, 64, 15), Direction.SOUTH),
                Arguments.of(start, new BlockPos(5, 64, 5), Direction.NORTH),
                Arguments.of(start, new BlockPos(10, 100, 10), Direction.NORTH)
        );
    }
}
