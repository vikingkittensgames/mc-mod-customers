package com.vikingkittens.mc.customers.client.common;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerProfileUtilsTest {
    @Test
    void usesCapitalizedFakePlayerNames() {
        assertEquals("Alex", PlayerProfileUtils.getName(PlayerProfileUtils.FakePlayers.getId(0)));
        assertEquals("Herobrine", PlayerProfileUtils.getName(PlayerProfileUtils.FakePlayers.getId(3)));
        assertEquals("Zuri", PlayerProfileUtils.getName(PlayerProfileUtils.FakePlayers.getId(6)));
    }

    @Test
    void usesTheMatchingFakePlayerSkinTexture() {
        assertEquals(
                ResourceLocation.fromNamespaceAndPath(
                        "customers",
                        "textures/customers/skins/makena.png"
                ),
                PlayerProfileUtils.getPicture(PlayerProfileUtils.FakePlayers.getId(4))
        );
    }
}
