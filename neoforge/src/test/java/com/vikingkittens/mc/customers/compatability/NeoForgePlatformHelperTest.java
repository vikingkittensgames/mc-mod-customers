package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NeoForgePlatformHelperTest {
    @Test
    void loadsNeoForgeConfigProvider() {
        assertEquals(NeoForgeConfigHelper.class, CustomersServices.config().getClass());
    }

    @Test
    void loadsNeoForgeNetworkProvider() {
        assertEquals(NeoForgeNetworkHelper.class, CustomersServices.network().getClass());
    }

    @Test
    void loadsNeoForgeRegistrationProvider() {
        assertEquals(
                NeoForgeRegistrationHelper.class,
                CustomersServices.registration().getClass()
        );
    }

    @Test
    void identifiesNeoForge() {
        NeoForgePlatformHelper helper = new NeoForgePlatformHelper(ignored -> false);

        assertEquals("NeoForge", helper.platformName());
    }

    @Test
    void delegatesModDetectionToNeoForge() {
        NeoForgePlatformHelper helper = new NeoForgePlatformHelper("optional_mod"::equals);

        assertTrue(helper.isModLoaded("optional_mod"));
        assertFalse(helper.isModLoaded("missing"));
    }

    @Test
    void delegatesVillagerTypeLookupToNeoForge() {
        Holder<Biome> biome = mock(Holder.class);
        ResourceKey<VillagerType> expected = mock(ResourceKey.class);
        NeoForgePlatformHelper helper = new NeoForgePlatformHelper(ignored -> false, ignored -> expected);

        assertEquals(expected, helper.villagerTypeForBiome(biome));
    }

    @Test
    void closesPlayerContainerThroughNeoForge() {
        Player player = mock(Player.class);
        NeoForgePlatformHelper helper = new NeoForgePlatformHelper(ignored -> false);

        helper.closeContainer(player);

        verify(player).closeContainer();
    }
}
