package com.vikingkittens.mc.customers.client.appearance.skins;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerGeckoResourcesTest {
    @Test
    void expandsCustomersAssetIds() {
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
                ResourceLocationCUtils.parse("example:geo/customers/skins/shopkeeper.geo.json"),
                SkinCustomersVillagerGeckoResources.getModelResource(definition)
        );
        assertEquals(
                ResourceLocationCUtils.parse("example:animations/customers/skins/shopkeeper.animation.json"),
                SkinCustomersVillagerGeckoResources.getAnimationResource(definition)
        );
    }

    @Test
    void preservesCompleteResourcesFromAnotherMod() {
        SkinCustomersVillagerDefinition definition = decode("""
                {
                  "texture": "ribbits:textures/entity/ribbit.png",
                  "model": "ribbits:geo/merchant_ribbit.geo.json",
                  "animation": "ribbits:animations/ribbit.animation.json",
                  "sitting_pivot": [0, 2.1, -0.5],
                  "animations": {
                    "idle": "idle",
                    "walk": "walk",
                    "sit": "idle"
                  },
                  "required_mods": ["ribbits"]
                }
                """);

        assertEquals(
                ResourceLocationCUtils.parse("ribbits:geo/merchant_ribbit.geo.json"),
                SkinCustomersVillagerGeckoResources.getModelResource(definition)
        );
        assertEquals(
                ResourceLocationCUtils.parse("ribbits:animations/ribbit.animation.json"),
                SkinCustomersVillagerGeckoResources.getAnimationResource(definition)
        );
        assertEquals(
                ResourceLocationCUtils.parse("ribbits:textures/entity/ribbit.png"),
                definition.getTextureLocation()
        );
    }

    private static SkinCustomersVillagerDefinition decode(String json) {
        return SkinCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(json)
        ).result().orElseThrow();
    }
}
