package com.vikingkittens.mc.customers.appearance;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.neoforged.neoforge.registries.RegistryBuilder;

import com.vikingkittens.mc.customers.Customers;

public interface CustomersVillagerAppearance {
    ResourceKey<Registry<CustomersVillagerAppearance>> APPEARANCE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(Customers.MODID, "villager_appearance"));
    Registry<CustomersVillagerAppearance> APPEARANCE_REGISTRY =
            new RegistryBuilder<>(APPEARANCE_REGISTRY_KEY).create();

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
}
