package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CustomerPickupCounterGeneratedServerDataTest {
    private static final List<Path> GENERATED = List.of(
            Path.of("../neoforge/src/generated/serverResources/data/customers")
    );

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void generatesRecipesAndLootForEveryVariant() throws IOException {
        for (Path generated : GENERATED) {
        for (CustomerOverlayBlockVariant variant
                : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPickupCounter.getBlockName(variant);
            Path recipePath = generated.resolve(
                    "recipe/" + name + ".json"
            );
            Path lootPath = generated.resolve(
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
}
