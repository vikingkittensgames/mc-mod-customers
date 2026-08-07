package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPickupCounterGeneratedServerDataTest {
    private static final Path GENERATED =
            Path.of("src/generated/resources/data/customers");

    @Test
    void generatesRecipesAndLootForEveryVariant() throws IOException {
        for (CustomerPickupCounterVariant variant
                : CustomerPickupCounterVariants.ALL) {
            String name = CustomerPickupCounter.getBlockName(variant);
            Path recipePath = GENERATED.resolve(
                    "recipe/" + name + ".json"
            );
            Path lootPath = GENERATED.resolve(
                    "loot_table/blocks/" + name + ".json"
            );

            assertTrue(Files.exists(recipePath));
            assertTrue(Files.exists(lootPath));

            JsonObject recipe = JsonParser.parseString(
                    Files.readString(recipePath)
            ).getAsJsonObject();
            assertEquals(
                    "IVV",
                    recipe.getAsJsonArray("pattern")
                            .get(0)
                            .getAsString()
            );
            assertEquals(
                    "minecraft:iron_ingot",
                    recipe.getAsJsonObject("key")
                            .get("I")
                            .getAsString()
            );
            assertEquals(
                    "customers:" + name,
                    recipe.getAsJsonObject("result")
                            .get("id")
                            .getAsString()
            );

            JsonObject loot = JsonParser.parseString(
                    Files.readString(lootPath)
            ).getAsJsonObject();
            assertEquals(
                    "customers:" + name,
                    loot.getAsJsonArray("pools")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonArray("entries")
                            .get(0)
                            .getAsJsonObject()
                            .get("name")
                            .getAsString()
            );
        }
    }
}
