package com.vikingkittens.mc.customers.appearance.skins;

import java.util.Optional;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinCustomersVillagerDefinitionTest {
    @Test
    void readsRequiredTextureAndUsesRendererDefaults() {
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "texture": "example:steve"
                        }
                        """)
        ).getOrThrow();

        assertEquals(ResourceLocation.parse("example:steve"), definition.texture());
        assertEquals(SkinCustomersVillagerModel.WIDE, definition.model());
        assertFalse(definition.legacy());
        assertEquals(SkinCustomersVillagerDefinition.DEFAULT_SCALE, definition.scale());
        assertEquals(SkinCustomersVillagerDefinition.DEFAULT_SHADOW_RADIUS, definition.shadowRadius());
        assertEquals(0.0F, definition.nameTagOffset());
        assertTrue(definition.sounds().isEmpty());
        assertEquals(ResourceLocation.parse("example:textures/customers/skins/steve.png"), definition.getTextureLocation());
    }

    @Test
    void readsSlimModelRendererSettingsAndSounds() {
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "texture": "example:alex",
                          "model": "slim",
                          "legacy": true,
                          "scale": 1.1,
                          "shadow_radius": 0.4,
                          "name_tag_offset": 0.2,
                          "sounds": {
                            "ambient": "example:alex_ambient",
                            "hurt": "example:alex_hurt"
                          }
                        }
                        """)
        ).getOrThrow();

        assertEquals(SkinCustomersVillagerModel.SLIM, definition.model());
        assertTrue(definition.legacy());
        assertEquals(1.1F, definition.scale());
        assertEquals(0.4F, definition.shadowRadius());
        assertEquals(0.2F, definition.nameTagOffset());
        assertEquals(Optional.of(ResourceLocation.parse("example:alex_ambient")), definition.getSound(SkinCustomersVillagerSound.AMBIENT));
        assertEquals(Optional.empty(), definition.getSound(SkinCustomersVillagerSound.DEATH));
    }
}
