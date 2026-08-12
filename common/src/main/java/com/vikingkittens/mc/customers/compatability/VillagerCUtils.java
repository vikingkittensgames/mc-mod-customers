package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

/**
 * Provides version-compatible villager data operations.
 */
public final class VillagerCUtils {
    private VillagerCUtils() {
    }

    public static VillagerData withTypeAndProfession(
            VillagerData data,
            RegistryAccess registries,
            ResourceKey<VillagerType> type,
            ResourceKey<VillagerProfession> profession
    ) {
        VillagerType villagerType = registries
                .registryOrThrow(Registries.VILLAGER_TYPE)
                .getOrThrow(type);
        VillagerProfession villagerProfession = registries
                .registryOrThrow(Registries.VILLAGER_PROFESSION)
                .getOrThrow(profession);
        return data.setType(villagerType)
                .setProfession(villagerProfession);
    }

    public static boolean hasProfession(
            VillagerData data,
            ResourceKey<VillagerProfession> profession
    ) {
        return data.getProfession() == BuiltInRegistries.VILLAGER_PROFESSION.get(profession.location());
    }
}
