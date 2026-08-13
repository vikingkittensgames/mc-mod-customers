package com.vikingkittens.mc.customers.appearance.monsters;

import net.minecraft.util.Mth;

public enum MonsterCustomersVillagerVariation {
    ZOMBIE,
    SKELETON,
    WITCH,
    HUSK,
    DROWNED,
    STRAY;

    public static MonsterCustomersVillagerVariation fromSeed(float variationSeed) {
        float normalizedSeed = Mth.clamp(variationSeed, 0.0F, Math.nextDown(1.0F));
        return values()[(int)(normalizedSeed * values().length)];
    }
}
