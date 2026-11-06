package com.vikingkittens.mc.customers.common;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerPresetDefinitionTest {
    @Test
    void decodesCustomerPresetSettingsAndLockedInventory() {
        String json = """
                {
                  "name": "Cookies & Milk Customers",
                  "icon": "minecraft:textures/item/cookie.png",
                  "settings": {
                    "mode": "night",
                    "level_settings": [{
                      "automatic_cost": true,
                      "rows": [{"items": [{"id": "minecraft:cookie", "count": 3}]}]
                    }]
                  }
                }
                """;

        SpawnerPresetDefinition preset = SpawnerPresetDefinition.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();

        assertEquals("Cookies & Milk Customers", preset.name());
        assertEquals("minecraft:textures/item/cookie.png", preset.icon().orElseThrow().toString());
        assertEquals("night", preset.settings().getString("mode"));
        var level = preset.settings().getList("level_settings", 10).getCompound(0);
        assertTrue(level.getBoolean("automatic_cost"));
        assertEquals(3, level.getList("rows", 10).getCompound(0).getList("items", 10).getCompound(0).getInt("count"));
    }
}
