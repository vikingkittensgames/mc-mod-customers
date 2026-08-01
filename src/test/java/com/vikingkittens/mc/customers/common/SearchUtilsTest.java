package com.vikingkittens.mc.customers.common;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SearchUtilsTest {
    @Test
    void searchesAnEvenSizedBoxCenteredOnThePosition() {
        Level level = mock(Level.class);

        List<BlockPos> positions = SearchUtils.findBlocksInBox(
                level,
                BlockPos.ZERO,
                2,
                (pos, state) -> true
        );

        assertEquals(8, positions.size());
        assertEquals(
                List.of(
                        new BlockPos(-1, -1, -1),
                        new BlockPos(-1, -1, 0),
                        new BlockPos(-1, 0, -1),
                        new BlockPos(-1, 0, 0),
                        new BlockPos(0, -1, -1),
                        new BlockPos(0, -1, 0),
                        new BlockPos(0, 0, -1),
                        new BlockPos(0, 0, 0)
                ).stream().sorted().toList(),
                positions.stream().sorted().toList()
        );
    }
}
