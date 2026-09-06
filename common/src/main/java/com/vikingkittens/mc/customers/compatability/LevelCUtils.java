package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

/**
 * Provides version-compatible level state and build-height access.
 */
public final class LevelCUtils {
    private LevelCUtils() {
    }
    public static boolean isClientSide(Level level) {
        return level.isClientSide();
    }
    public static boolean isDaytime(Level level) {
        return level.isBrightOutside();
    }
    public static boolean isNighttime(Level level) {
        return level.isDarkOutside();
    }
    public static int getMinBuildHeight(LevelHeightAccessor level) {
        return level.getMinY();
    }
    public static int getMaxBuildHeight(LevelHeightAccessor level) {
        return level.getMaxY();
    }
}
