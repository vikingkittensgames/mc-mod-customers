package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinCustomersVillagerDefinitionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

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
        assertTrue(definition.names().isEmpty());
        assertTrue(definition.sounds().isEmpty());
        assertTrue(definition.animation().isEmpty());
        assertTrue(definition.sittingPivot().isEmpty());
        assertTrue(definition.animations().isEmpty());
        assertTrue(definition.requiredMods().isEmpty());
        assertEquals(ResourceLocation.parse("example:textures/customers/skins/steve.png"), definition.getTextureLocation());
    }

    @Test
    void readsLiteralAndTranslatedVillagerNames() {
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "texture": "example:steve",
                          "names": [
                            "Steve",
                            {"translate": "name.example.shopkeeper"}
                          ]
                        }
                        """)
        ).getOrThrow();

        assertEquals(List.of(
                Component.literal("Steve"),
                Component.translatable("name.example.shopkeeper")
        ), definition.names());
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
                            "hurt": "example:alex_hurt",
                            "yes": "example:alex_yes",
                            "no": "example:alex_no"
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
        assertEquals(Optional.of(ResourceLocation.parse("example:alex_yes")), definition.getSound(SkinCustomersVillagerSound.YES));
        assertEquals(Optional.of(ResourceLocation.parse("example:alex_no")), definition.getSound(SkinCustomersVillagerSound.NO));
        assertEquals(Optional.empty(), definition.getSound(SkinCustomersVillagerSound.DEATH));
    }

    @Test
    void readsGeckoModelAndAnimationSettings() {
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "texture": "example:shopkeeper",
                          "model": "example:shopkeeper",
                          "animation": "example:shopkeeper",
                          "head_bone": "face",
                          "head_tracking": {
                            "maximum_yaw": 50.0,
                            "pitch_multiplier": 0.5
                          },
                          "sitting_pivot": [1.0, 11.0, -2.0],
                          "animations": {
                            "idle": "misc.idle",
                            "walk": "move.walk",
                            "sit": "pose.sit"
                          }
                        }
                        """)
        ).getOrThrow();

        assertEquals(SkinCustomersVillagerModel.gecko(ResourceLocation.parse("example:shopkeeper")), definition.model());
        assertEquals(ResourceLocation.parse("example:shopkeeper"), definition.animation().orElseThrow());
        assertEquals("face", definition.getHeadBone());
        assertEquals(50.0F, definition.getHeadTracking().maximumYaw());
        assertEquals(0.5F, definition.getHeadTracking().pitchMultiplier());
        assertEquals(new SkinCustomersVillagerPoint(1.0F, 11.0F, -2.0F), definition.sittingPivot().orElseThrow());
        assertEquals("pose.sit", definition.animations().get("sit"));
    }

    @Test
    void rejectsBareAndIncompleteGeckoModels() {
        assertThrows(IllegalStateException.class, () -> SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"texture\":\"example:test\",\"model\":\"shopkeeper\"}")
        ).getOrThrow());
        assertThrows(IllegalStateException.class, () -> SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"texture\":\"example:test\",\"model\":\"example:shopkeeper\"}")
        ).getOrThrow());
    }

    @Test
    void rejectsGeckoPropertiesOnVanillaModels() {
        assertThrows(IllegalStateException.class, () -> SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {"texture":"example:test","model":"wide","head_bone":"head"}
                        """)
        ).getOrThrow());
    }

    @Test
    void preservesCompleteTextureResourcesAndReadsRequiredMods() {
        SkinCustomersVillagerDefinition definition = SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "texture": "ribbits:textures/entity/ribbit.png",
                          "model": "ribbits:geo/merchant_ribbit.geo.json",
                          "animation": "ribbits:animations/ribbit.animation.json",
                          "sitting_pivot": [0.0, 2.1, -0.5],
                          "animations": {
                            "idle": "idle",
                            "walk": "walk",
                            "sit": "idle"
                          },
                          "required_mods": ["ribbits"]
                        }
                        """)
        ).getOrThrow();

        assertEquals(ResourceLocation.parse("ribbits:textures/entity/ribbit.png"), definition.getTextureLocation());
        assertEquals(List.of("ribbits"), definition.requiredMods());
    }

    @Test
    void rejectsBlankRequiredModIds() {
        assertThrows(IllegalStateException.class, () -> SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {"texture":"example:test","required_mods":[""]}
                        """)
        ).getOrThrow());
    }
}
