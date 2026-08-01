package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

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
        return data.withType(registries, type)
                .withProfession(registries, profession);
    }
    public static boolean hasProfession(
            VillagerData data,
            ResourceKey<VillagerProfession> profession
    ) {
        return data.profession().is(profession);
    }
}
