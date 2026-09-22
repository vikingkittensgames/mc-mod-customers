package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.biome.Biome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NeoForgePlatformHelperTest {
    @Test
    void loadsNeoForgeConfigProvider() {
        assertEquals(NeoForgeConfigHelper.class, CustomersServices.config().getClass());
    }

    @Test
    void loadsNeoForgeRegistrationProvider() {
        assertEquals(
                NeoForgeRegistrationHelper.class,
                CustomersServices.registration().getClass()
        );
    }

    @Test
    void delegatesVillagerTypeLookupToNeoForge() {
        Holder<Biome> biome = mock(Holder.class);
        ResourceKey<VillagerType> expected = mock(ResourceKey.class);
        NeoForgePlatformHelper helper = new NeoForgePlatformHelper(ignored -> expected);

        assertEquals(expected, helper.villagerTypeForBiome(biome));
    }

}
