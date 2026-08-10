package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPaymentBoxGeneratedServerDataTest {
    private static final Path GENERATED =
            Path.of("src/generated/resources/data/customers");

    @Test
    void generatesRecipeAndSelfDropLootForEveryVariant()
            throws IOException {
        for (CustomerOverlayBlockVariant variant
                : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPaymentBox.getBlockName(variant);
            Path recipePath = GENERATED.resolve(
                    "recipe/" + name + ".json"
            );
            Path lootPath = GENERATED.resolve(
                    "loot_table/blocks/" + name + ".json"
            );

            assertTrue(Files.exists(recipePath));
            assertTrue(Files.exists(lootPath));

            JsonObject recipe = read(recipePath);
            JsonArray pattern = recipe.getAsJsonArray("pattern");
            assertEquals("VGV", pattern.get(0).getAsString());
            assertEquals("VEV", pattern.get(1).getAsString());
            assertEquals("VVV", pattern.get(2).getAsString());
            JsonObject keys = recipe.getAsJsonObject("key");
            assertEquals(
                    "minecraft:gold_ingot",
                    keys.getAsJsonObject("G")
                            .get("item")
                            .getAsString()
            );
            assertEquals(
                    "minecraft:emerald",
                    keys.getAsJsonObject("E")
                            .get("item")
                            .getAsString()
            );
            assertEquals(
                    variant.ingredient().get().asItem()
                            .builtInRegistryHolder()
                            .key()
                            .location()
                            .toString(),
                    keys.getAsJsonObject("V")
                            .get("item")
                            .getAsString()
            );
            assertEquals(
                    "customers:" + name,
                    recipe.getAsJsonObject("result")
                            .get("id")
                            .getAsString()
            );

            JsonObject loot = read(lootPath);
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

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path))
                .getAsJsonObject();
    }
}
