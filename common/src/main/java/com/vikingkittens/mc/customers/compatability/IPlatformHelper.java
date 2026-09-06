package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public interface IPlatformHelper {
    String platformName();

    boolean isModLoaded(String modId);

    void closeContainer(Player player);

    default ResourceKey<VillagerType> villagerTypeForBiome(Holder<Biome> biome) {
        return vanillaVillagerTypeForBiome(biome);
    }

    static ResourceKey<VillagerType> vanillaVillagerTypeForBiome(Holder<Biome> biome) {
        return VillagerType.byBiome(biome);
    }
}
