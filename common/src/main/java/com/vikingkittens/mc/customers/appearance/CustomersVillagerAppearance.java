package com.vikingkittens.mc.customers.appearance;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersRegistry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public interface CustomersVillagerAppearance {
    ResourceKey<Registry<CustomersVillagerAppearance>> APPEARANCE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(
                    Identifier.fromNamespaceAndPath(Customers.MODID, "villager_appearance"));
    CustomersRegistry<CustomersVillagerAppearance> APPEARANCE_REGISTRY =
            CustomersServices.registration().createRegistry(APPEARANCE_REGISTRY_KEY);

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
