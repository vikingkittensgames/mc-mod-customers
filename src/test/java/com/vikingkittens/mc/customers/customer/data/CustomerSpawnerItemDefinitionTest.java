package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSpawnerItemDefinitionTest {
    private static final Path ITEMS = Path.of(
            "src/main/resources/assets/customers/items"
    );

    @Test
    void definesSpawnerBlockItemModels() throws IOException {
        assertItemDefinition("customer_spawner_block");
        assertItemDefinition("supplier_spawner_block");
    }

    private static void assertItemDefinition(String name) throws IOException {
        Path path = ITEMS.resolve(name + ".json");
        assertTrue(Files.exists(path));
        JsonObject model = JsonParser.parseString(Files.readString(path))
                .getAsJsonObject()
                .getAsJsonObject("model");
        assertEquals("minecraft:model", model.get("type").getAsString());
        assertEquals(
                "customers:item/" + name,
                model.get("model").getAsString()
        );
    }
}
