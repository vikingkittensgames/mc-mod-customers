package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class LevelCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void readsClientSideState() {
        Level level = mock(Level.class);
        when(level.isClientSide()).thenReturn(true);

        assertTrue(LevelCUtils.isClientSide(level));
    }
    @Test
    void readsDaytimeState() {
        Level level = mock(Level.class);
        when(level.isBrightOutside()).thenReturn(true);

        assertTrue(LevelCUtils.isDaytime(level));
    }
    @Test
    void readsNighttimeState() {
        Level level = mock(Level.class);
        when(level.isDarkOutside()).thenReturn(false);

        assertFalse(LevelCUtils.isNighttime(level));
    }
    @Test
    void readsBuildHeightBounds() {
        LevelHeightAccessor level = mock(LevelHeightAccessor.class);
        when(level.getMinY()).thenReturn(-64);
        when(level.getMaxY()).thenReturn(320);

        assertEquals(-64, LevelCUtils.getMinBuildHeight(level));
        assertEquals(320, LevelCUtils.getMaxBuildHeight(level));
    }
}
