package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.architectury.platform.Platform;
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
    public @Nullable Component getVillagerName(CustomersVillager villager) {
        List<ResourceLocation> availableSkinIds = getAvailableSkinIds();
        return selectName(availableSkinIds, skins::get, villager.getVariationSeed())
                .orElse(null);
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

    @Override
    public @Nullable SoundEvent getYesSound(CustomersVillager villager) {
        return getSound(villager, SkinCustomersVillagerSound.YES);
    }

    @Override
    public @Nullable SoundEvent getNoSound(CustomersVillager villager) {
        return getSound(villager, SkinCustomersVillagerSound.NO);
    }

    static Optional<ResourceLocation> selectSkinId(List<ResourceLocation> skinIds, float variationSeed) {
        if (skinIds.isEmpty()) return Optional.empty();
        float boundedSeed = Mth.clamp(variationSeed, 0.0F, Math.nextDown(1.0F));
        int index = Math.min((int)(boundedSeed * skinIds.size()), skinIds.size() - 1);
        return Optional.of(skinIds.get(index));
    }

    static float getSelectedSkinVariation(int skinCount, float variationSeed) {
        if (skinCount <= 0) return 0.0F;
        float boundedSeed = Mth.clamp(variationSeed, 0.0F, Math.nextDown(1.0F));
        float scaledSeed = boundedSeed * skinCount;
        return scaledSeed - Mth.floor(scaledSeed);
    }

    static Optional<Component> selectName(List<Component> names, float variationSeed) {
        if (names.isEmpty()) return Optional.empty();
        float boundedSeed = Mth.clamp(variationSeed, 0.0F, Math.nextDown(1.0F));
        int index = Math.min((int)(boundedSeed * names.size()), names.size() - 1);
        return Optional.of(names.get(index));
    }

    static Optional<Component> selectName(
            List<ResourceLocation> skinIds,
            Function<ResourceLocation, @Nullable SkinCustomersVillagerDefinition> definitionLookup,
            float variationSeed
    ) {
        return selectSkinId(skinIds, variationSeed)
                .map(definitionLookup)
                .flatMap(definition ->
                        selectName(definition.names(), getSelectedSkinVariation(skinIds.size(), variationSeed))
                );
    }

    private List<ResourceLocation> getAvailableSkinIds() {
        return getAvailableSkinIds(
                skinPack.skins(),
                skins::get,
                Platform.isModLoaded("geckolib"),
                Platform::isModLoaded
        );
    }

    static List<ResourceLocation> getAvailableSkinIds(
            List<ResourceLocation> skinIds,
            Function<ResourceLocation, @Nullable SkinCustomersVillagerDefinition> definitionLookup,
            boolean geckoLibLoaded,
            Predicate<String> modLoaded
    ) {
        return skinIds.stream()
                .filter(skinId -> isAvailable(definitionLookup.apply(skinId), geckoLibLoaded, modLoaded))
                .toList();
    }

    static boolean isAvailable(
            @Nullable SkinCustomersVillagerDefinition definition,
            boolean geckoLibLoaded,
            Predicate<String> modLoaded
    ) {
        return definition != null
                && (!definition.model().isGecko() || geckoLibLoaded)
                && definition.requiredMods().stream().allMatch(modLoaded);
    }

    private @Nullable SoundEvent getSound(CustomersVillager villager, SkinCustomersVillagerSound sound) {
        return getSkin(villager)
                .flatMap(definition -> definition.getSound(sound))
                .map(SoundEvent::createVariableRangeEvent)
                .orElse(null);
    }
}
