package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlock;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerBlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeSpawnerLootTableCompatibilityTest {
    private static final Path LOOT_TABLES = Path.of(
            "build/sourceSets/main/data/customers/loot_tables/blocks"
    );

    @Test
    void copiesCommonSpawnerLootTablesToTheMinecraft1201Path()
            throws IOException {
        for (Map.Entry<String, String> entry : Map.of(
                CustomerSpawnerBlock.NAME, "customers:customer_spawner_block",
                SupplierSpawnerBlock.NAME, "customers:supplier_spawner_block"
        ).entrySet()) {
            Path lootTable = LOOT_TABLES.resolve(entry.getKey() + ".json");
            assertTrue(Files.exists(lootTable));

            JsonObject json = JsonParser.parseString(Files.readString(lootTable))
                    .getAsJsonObject();
            assertEquals(
                    entry.getValue(),
                    json.getAsJsonArray("pools")
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
