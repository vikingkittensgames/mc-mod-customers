package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;

public final class SkinPackCustomersVillagerAppearance implements CustomersVillagerAppearance {
    private final Registry<SkinCustomersVillagerDefinition> skins;
    private final SkinPackCustomersVillagerDefinition skinPack;

    public SkinPackCustomersVillagerAppearance(RegistryAccess registryAccess, SkinPackCustomersVillagerDefinition skinPack) {
        this.skins = registryAccess.registryOrThrow(SkinCustomersVillagerRegistries.SKINS);
        this.skinPack = skinPack;
    }

    @Override
    public Component getName() {
        return skinPack.getName();
    }

    @Override
    public boolean isApplicable(CustomersVillager villager) {
        return !getAvailableSkinIds().isEmpty();
    }

    public Optional<SkinCustomersVillagerDefinition> getSkin(CustomersVillager villager) {
        return selectSkinId(getAvailableSkinIds(), villager.getVariationSeed()).map(skins::get);
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(CustomersVillager villager) {
        return getSound(villager, SkinCustomersVillagerSound.AMBIENT);
    }

    @Override
    public @Nullable SoundEvent getHurtSound(CustomersVillager villager) {
        return getSound(villager, SkinCustomersVillagerSound.HURT);
    }

    @Override
    public @Nullable SoundEvent getDeathSound(CustomersVillager villager) {
        return getSound(villager, SkinCustomersVillagerSound.DEATH);
    }

    @Override
    public @Nullable SoundEvent getStepSound(CustomersVillager villager) {
        return getSound(villager, SkinCustomersVillagerSound.STEP);
    }

    static Optional<ResourceLocation> selectSkinId(List<ResourceLocation> skinIds, float variationSeed) {
        if (skinIds.isEmpty()) return Optional.empty();
        float boundedSeed = Mth.clamp(variationSeed, 0.0F, Math.nextDown(1.0F));
        int index = Math.min((int)(boundedSeed * skinIds.size()), skinIds.size() - 1);
        return Optional.of(skinIds.get(index));
    }

    private List<ResourceLocation> getAvailableSkinIds() {
        return skinPack.skins().stream().filter(skins::containsKey).toList();
    }

    private @Nullable SoundEvent getSound(CustomersVillager villager, SkinCustomersVillagerSound sound) {
        return getSkin(villager)
                .flatMap(definition -> definition.getSound(sound))
                .map(SoundEvent::createVariableRangeEvent)
                .orElse(null);
    }
}
