package com.vikingkittens.mc.customers.client.appearance.monsters;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonsterCustomersVillagerWitchRendererTest {
    @Test
    void usesTheMinecraft26WitchTextureLocation() {
        assertEquals(
                Identifier.withDefaultNamespace(
                        "textures/entity/witch/witch.png"
                ),
                MonsterCustomersVillagerWitchRenderer.textureLocation()
        );
    }
}
