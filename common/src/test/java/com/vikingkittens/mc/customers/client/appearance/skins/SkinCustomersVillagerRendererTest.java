package com.vikingkittens.mc.customers.client.appearance.skins;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerRendererTest {
    @Test
    void createsWidePlayerSkinWithTheSelectedTexture() {
        Identifier texture = Identifier.fromNamespaceAndPath("test", "textures/skins/wide.png");
        var skin = SkinCustomersVillagerRenderer.createPlayerSkin(texture, false);

        assertEquals(texture, skin.body().texturePath());
        assertEquals(PlayerModelType.WIDE, skin.model());
    }

    @Test
    void createsSlimPlayerSkinWithTheSelectedTexture() {
        Identifier texture = Identifier.fromNamespaceAndPath("test", "textures/skins/slim.png");
        var skin = SkinCustomersVillagerRenderer.createPlayerSkin(texture, true);

        assertEquals(texture, skin.body().texturePath());
        assertEquals(PlayerModelType.SLIM, skin.model());
    }
}
