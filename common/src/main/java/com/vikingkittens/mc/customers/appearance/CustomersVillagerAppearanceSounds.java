package com.vikingkittens.mc.customers.appearance;

import org.jetbrains.annotations.Nullable;

import net.minecraft.sounds.SoundEvent;

public final class CustomersVillagerAppearanceSounds {
    private CustomersVillagerAppearanceSounds() {}

    public static SoundEvent getYesSound(
            @Nullable CustomersVillagerAppearance appearance,
            CustomersVillager villager,
            SoundEvent fallback
    ) {
        if (appearance == null) {
            return fallback;
        }
        SoundEvent sound = appearance.getYesSound(villager);
        return sound == null ? fallback : sound;
    }

    public static SoundEvent getNoSound(
            @Nullable CustomersVillagerAppearance appearance,
            CustomersVillager villager,
            SoundEvent fallback
    ) {
        if (appearance == null) {
            return fallback;
        }
        SoundEvent sound = appearance.getNoSound(villager);
        return sound == null ? fallback : sound;
    }
}
