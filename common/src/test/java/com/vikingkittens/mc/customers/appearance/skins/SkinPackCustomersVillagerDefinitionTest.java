package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinPackCustomersVillagerDefinitionTest {
    @Test
    void readsAppearanceNameAndOrderedSkinIds() {
        SkinPackCustomersVillagerDefinition definition = SkinPackCustomersVillagerDefinition.CODEC.parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("""
                        {
                          "name": "Example Skins",
                          "skins": [
                            "example:steve",
                            "example:alex"
                          ]
                        }
                        """)
        ).getOrThrow();

        assertEquals("Example Skins", definition.getName().getString());
        assertEquals(List.of(ResourceLocation.parse("example:steve"), ResourceLocation.parse("example:alex")), definition.skins());
    }
}
