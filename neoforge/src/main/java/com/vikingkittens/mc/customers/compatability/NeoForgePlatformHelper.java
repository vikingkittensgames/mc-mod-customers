package com.vikingkittens.mc.customers.compatability;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public final class NeoForgePlatformHelper implements IPlatformHelper {
    private final Predicate<String> loadedMods;
    private final Function<Holder<Biome>, ResourceKey<VillagerType>> villagerTypes;

    public NeoForgePlatformHelper() {
        this(modId -> ModList.get().isLoaded(modId), NeoForgePlatformHelper::findVillagerType);
    }

    NeoForgePlatformHelper(Predicate<String> loadedMods) {
        this(loadedMods, NeoForgePlatformHelper::findVillagerType);
    }

    NeoForgePlatformHelper(
            Predicate<String> loadedMods,
            Function<Holder<Biome>, ResourceKey<VillagerType>> villagerTypes
    ) {
        this.loadedMods = loadedMods;
        this.villagerTypes = villagerTypes;
    }

    @Override
    public String platformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return loadedMods.test(modId);
    }

    @Override
    public void closeContainer(Player player) {
        player.closeContainer();
    }

    @Override
    public ResourceKey<VillagerType> villagerTypeForBiome(Holder<Biome> biome) {
        return villagerTypes.apply(biome);
    }

    private static ResourceKey<VillagerType> findVillagerType(Holder<Biome> biome) {
        BiomeVillagerType mapData = biome.getData(NeoForgeDataMaps.VILLAGER_TYPES);
        return mapData == null
                ? VillagerType.PLAINS
                : mapData.type();
    }
}
