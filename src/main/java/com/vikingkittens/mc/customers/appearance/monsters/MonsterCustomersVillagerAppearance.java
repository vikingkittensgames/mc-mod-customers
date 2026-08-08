package com.vikingkittens.mc.customers.appearance.monsters;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerType;

public final class MonsterCustomersVillagerAppearance
        implements CustomersVillagerAppearance {
    @Override
    public Component getName() {
        return Component.translatable(
                "appearance.customers.monsters"
        );
    }

    @Override
    public boolean isApplicable(CustomersVillager villager) {
        return villager.getCustomersVillagerType()
                != CustomersVillagerType.SUPPLIER;
    }

    @Override
    public SoundEvent getAmbientSound(CustomersVillager villager) {
        return switch (variation(villager)) {
            case ZOMBIE -> SoundEvents.ZOMBIE_AMBIENT;
            case SKELETON -> SoundEvents.SKELETON_AMBIENT;
            case WITCH -> SoundEvents.WITCH_AMBIENT;
            case HUSK -> SoundEvents.HUSK_AMBIENT;
            case DROWNED -> villager.isInWater()
                    ? SoundEvents.DROWNED_AMBIENT_WATER
                    : SoundEvents.DROWNED_AMBIENT;
            case STRAY -> SoundEvents.STRAY_AMBIENT;
        };
    }

    @Override
    public SoundEvent getHurtSound(CustomersVillager villager) {
        return switch (variation(villager)) {
            case ZOMBIE -> SoundEvents.ZOMBIE_HURT;
            case SKELETON -> SoundEvents.SKELETON_HURT;
            case WITCH -> SoundEvents.WITCH_HURT;
            case HUSK -> SoundEvents.HUSK_HURT;
            case DROWNED -> villager.isInWater()
                    ? SoundEvents.DROWNED_HURT_WATER
                    : SoundEvents.DROWNED_HURT;
            case STRAY -> SoundEvents.STRAY_HURT;
        };
    }

    @Override
    public SoundEvent getDeathSound(CustomersVillager villager) {
        return switch (variation(villager)) {
            case ZOMBIE -> SoundEvents.ZOMBIE_DEATH;
            case SKELETON -> SoundEvents.SKELETON_DEATH;
            case WITCH -> SoundEvents.WITCH_DEATH;
            case HUSK -> SoundEvents.HUSK_DEATH;
            case DROWNED -> villager.isInWater()
                    ? SoundEvents.DROWNED_DEATH_WATER
                    : SoundEvents.DROWNED_DEATH;
            case STRAY -> SoundEvents.STRAY_DEATH;
        };
    }

    @Override
    public @Nullable SoundEvent getStepSound(
            CustomersVillager villager
    ) {
        return switch (variation(villager)) {
            case ZOMBIE -> SoundEvents.ZOMBIE_STEP;
            case SKELETON -> SoundEvents.SKELETON_STEP;
            case WITCH -> null;
            case HUSK -> SoundEvents.HUSK_STEP;
            case DROWNED -> SoundEvents.DROWNED_STEP;
            case STRAY -> SoundEvents.STRAY_STEP;
        };
    }

    private static MonsterCustomersVillagerVariation variation(
            CustomersVillager villager
    ) {
        return MonsterCustomersVillagerVariation.fromSeed(
                villager.getVariationSeed()
        );
    }
}
