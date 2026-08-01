package com.vikingkittens.mc.customers.compatability;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class LevelCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void readsServerSideState() {
        Level level = mock(Level.class);
        assertFalse(LevelCUtils.isClientSide(level));
    }
    @Test
    void readsDaytimeState() {
        Level level = mock(Level.class);
        when(level.isDay()).thenReturn(true);

        assertTrue(LevelCUtils.isDaytime(level));
    }
    @Test
    void readsNighttimeState() {
        Level level = mock(Level.class);
        when(level.isNight()).thenReturn(false);

        assertFalse(LevelCUtils.isNighttime(level));
    }
    @Test
    void readsBuildHeightBounds() {
        LevelHeightAccessor level = mock(LevelHeightAccessor.class);
        when(level.getMinBuildHeight()).thenReturn(-64);
        when(level.getMaxBuildHeight()).thenReturn(320);

        assertEquals(-64, LevelCUtils.getMinBuildHeight(level));
        assertEquals(320, LevelCUtils.getMaxBuildHeight(level));
    }
}
