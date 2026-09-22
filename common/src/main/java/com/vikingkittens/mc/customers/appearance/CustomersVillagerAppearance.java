package com.vikingkittens.mc.customers.appearance;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public interface CustomersVillagerAppearance {
    Component getName();

    default boolean isApplicable(CustomersVillager villager) {
        return true;
    }

    default @Nullable SoundEvent getAmbientSound(CustomersVillager villager) {
        return null;
    }

    default @Nullable SoundEvent getHurtSound(CustomersVillager villager) {
        return null;
    }

    default @Nullable SoundEvent getDeathSound(CustomersVillager villager) {
        return null;
    }

    default @Nullable SoundEvent getStepSound(CustomersVillager villager) {
        return null;
    }

    default @Nullable SoundEvent getYesSound(CustomersVillager villager) {
        return null;
    }

    default @Nullable SoundEvent getNoSound(CustomersVillager villager) {
        return null;
    }
}
