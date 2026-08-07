package com.vikingkittens.mc.customers.common;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

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

    @Test
    void searchesEntitiesInTheSameEvenSizedBox() {
        Level level = mock(Level.class);
        Entity entity = mock(Entity.class);
        when(level.getEntitiesOfClass(
                eq(Entity.class),
                any(AABB.class),
                any()
        )).thenReturn(List.of(entity));

        List<Entity> entities = SearchUtils.findEntitiesInBox(
                level,
                Entity.class,
                BlockPos.ZERO,
                64,
                candidate -> true
        );

        assertEquals(List.of(entity), entities);
        verify(level).getEntitiesOfClass(
                eq(Entity.class),
                eq(new AABB(-32, -32, -32, 32, 32, 32)),
                any()
        );
    }
}
