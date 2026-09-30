package com.vikingkittens.mc.customers.compatability;

import java.util.function.Function;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.biome.Biome;

import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public final class NeoForgePlatformHelper implements IPlatformHelper {
    private final Function<Holder<Biome>, ResourceKey<VillagerType>> villagerTypes;

    public NeoForgePlatformHelper() {
        this(NeoForgePlatformHelper::findVillagerType);
    }

    NeoForgePlatformHelper(
            Function<Holder<Biome>, ResourceKey<VillagerType>> villagerTypes
    ) {
        this.villagerTypes = villagerTypes;
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
