package com.vikingkittens.mc.customers.client.appearance.skins;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerGeckoResourcesTest {
    @Test
    void expandsCustomersAssetIdsToGeckoLibFiveLogicalIds() {
        SkinCustomersVillagerDefinition definition = decode("""
                {
                  "texture": "example:shopkeeper",
                  "model": "example:shopkeeper",
                  "animation": "example:shopkeeper",
                  "sitting_pivot": [0, 12, 0],
                  "animations": {
                    "idle": "idle",
                    "walk": "walk",
                    "sit": "sit"
                  }
                }
                """);

        assertEquals(
                Identifier.parse("example:customers/skins/shopkeeper"),
                SkinCustomersVillagerGeckoResources.getModelResource(definition)
        );
        assertEquals(
                Identifier.parse("example:customers/skins/shopkeeper"),
                SkinCustomersVillagerGeckoResources.getAnimationResource(definition)
        );
    }

    @Test
    void convertsCompleteChristmasDwellerResourcesToGeckoLibFiveLogicalIds() {
        SkinCustomersVillagerDefinition definition = decode("""
                {
                  "texture": "santa_dweller:textures/entity/santa_dweller_texture.png",
                  "model": "santa_dweller:geckolib/models/santa_dweller.geo.json",
                  "animation": "santa_dweller:geckolib/animations/santa_dweller.animation.json",
                  "sitting_pivot": [0, 22.5, -10],
                  "animations": {
                    "idle": "animation.santa_dweller.idle",
                    "walk": "animation.santa_dweller.calm_move",
                    "sit": "animation.santa_dweller.calm_idle"
                  },
                  "required_mods": ["santa_dweller"]
                }
                """);

        assertEquals(
                Identifier.parse("santa_dweller:santa_dweller"),
                SkinCustomersVillagerGeckoResources.getModelResource(definition)
        );
        assertEquals(
                Identifier.parse("santa_dweller:santa_dweller"),
                SkinCustomersVillagerGeckoResources.getAnimationResource(definition)
        );
        assertEquals(
                Identifier.parse("santa_dweller:textures/entity/santa_dweller_texture.png"),
                definition.getTextureLocation()
        );
    }

    private static SkinCustomersVillagerDefinition decode(String json) {
        return SkinCustomersVillagerDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }
}
