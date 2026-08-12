package com.vikingkittens.mc.customers.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeConditionResourceTest {
    private static final Path RECIPES = Path.of("src/main/resources/data/customers/recipe");

    @Test
    void usesForgeRecipeConditions() throws IOException {
        assertForgeCondition("customer_spawner_block");
        assertForgeCondition("supplier_spawner_block");
    }

    private static void assertForgeCondition(String recipe) throws IOException {
        JsonObject json = JsonParser.parseString(Files.readString(RECIPES.resolve(recipe + ".json"))).getAsJsonObject();

        assertTrue(json.has("forge:condition"));
        assertFalse(json.has("neoforge:conditions"));
    }
}
