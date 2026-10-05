package com.vikingkittens.mc.customers.appearance;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public interface CustomersVillagerAppearance {
    Component getName();

    default boolean isApplicable(CustomersVillager villager) {
        return true;
    }

    /**
     * Creates persistent appearance-specific properties when this appearance is selected for a Customer or Supplier.
     *
     * <p>This method runs on the logical server after the appearance, variation seed, level, and originating spawner
     * position are available. The returned properties are synchronized to clients and remain unchanged for the lifetime
     * of the entity unless explicitly replaced.</p>
     *
     * @param customer Customer or Supplier receiving this appearance
     * @return non-null property names and values to persist with the entity
     */
    default Map<String, String> getAdditionalProperties(CustomersVillager customer) {
        return Map.of();
    }

    default @Nullable Component getVillagerName(CustomersVillager villager) {
        return null;
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
