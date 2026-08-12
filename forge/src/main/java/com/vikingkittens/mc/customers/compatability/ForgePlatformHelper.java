package com.vikingkittens.mc.customers.compatability;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import net.minecraftforge.fml.ModList;

public final class ForgePlatformHelper implements IPlatformHelper {
    private final Predicate<String> loadedMods;
    private final Function<Holder<Biome>, ResourceKey<VillagerType>> villagerTypes;

    public ForgePlatformHelper() {
        this(modId -> ModList.get().isLoaded(modId), IPlatformHelper::vanillaVillagerTypeForBiome);
    }

    ForgePlatformHelper(Predicate<String> loadedMods) {
        this(loadedMods, IPlatformHelper::vanillaVillagerTypeForBiome);
    }

    ForgePlatformHelper(
            Predicate<String> loadedMods,
            Function<Holder<Biome>, ResourceKey<VillagerType>> villagerTypes
    ) {
        this.loadedMods = loadedMods;
        this.villagerTypes = villagerTypes;
    }

    @Override
    public String platformName() {
        return "Forge";
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
}
