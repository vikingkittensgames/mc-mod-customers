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

class ForgePlatformHelperTest {
    @Test
    void loadsForgeConfigProvider() {
        assertEquals(ForgeConfigHelper.class, CustomersServices.config().getClass());
    }

    @Test
    void loadsForgeNetworkProvider() {
        assertEquals(ForgeNetworkHelper.class, CustomersServices.network().getClass());
    }

    @Test
    void loadsForgeRegistrationProvider() {
        assertEquals(
                ForgeRegistrationHelper.class,
                CustomersServices.registration().getClass()
        );
    }

    @Test
    void loadsForgeProviderThroughCommonServiceLocator() {
        assertEquals("Forge", CustomersServices.platform().platformName());
    }

    @Test
    void identifiesForge() {
        ForgePlatformHelper helper = new ForgePlatformHelper(ignored -> false);

        assertEquals("Forge", helper.platformName());
    }

    @Test
    void delegatesModDetectionToForge() {
        ForgePlatformHelper helper = new ForgePlatformHelper("mca"::equals);

        assertTrue(helper.isModLoaded("mca"));
        assertFalse(helper.isModLoaded("missing"));
    }

    @Test
    void delegatesVillagerTypeLookupToForge() {
        Holder<Biome> biome = mock(Holder.class);
        ResourceKey<VillagerType> expected = mock(ResourceKey.class);
        ForgePlatformHelper helper = new ForgePlatformHelper(ignored -> false, ignored -> expected);

        assertEquals(expected, helper.villagerTypeForBiome(biome));
    }

    @Test
    void closesPlayerContainerThroughForge() {
        Player player = mock(Player.class);
        ForgePlatformHelper helper = new ForgePlatformHelper(ignored -> false);

        helper.closeContainer(player);

        verify(player).closeContainer();
    }
}
