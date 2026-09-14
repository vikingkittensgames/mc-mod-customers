package com.vikingkittens.mc.customers.advancements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomersAdvancementResourcesTest {
    @Test
    void definesTheBuilderAndServerTree() throws IOException {
        assertParent("builder/root", "customers:root");
        assertParent("builder/customer", "customers:builder/root");
        assertParent("builder/supplier", "customers:builder/root");
        assertParent("server/root", "customers:root");
        assertParent("server/customer/root", "customers:server/root");
        assertParent("server/supplier/root", "customers:server/root");
        assertParent("server/customer/customer_served", "customers:server/customer/root");
    }

    @Test
    void usesBuiltInCraftingTriggersAndTheCustomerServedTrigger() throws IOException {
        assertTrigger("builder/customer", "crafted_customer_spawner", "minecraft:recipe_crafted");
        assertTrigger("builder/supplier", "crafted_supplier_spawner", "minecraft:recipe_crafted");
        assertTrigger("server/customer/customer_served", "customer_served", "customers:customer_served");
    }

    @Test
    void includesTheCustomersAdvancementBackground() {
        assertNotNull(getClass().getResource(
                "/assets/customers/textures/gui/advancements/backgrounds/emerald.png"
        ));
    }

    private static void assertParent(String advancement, String expectedParent) throws IOException {
        assertEquals(expectedParent, read(advancement).get("parent").getAsString());
    }

    private static void assertTrigger(String advancement, String criterion, String expectedTrigger) throws IOException {
        JsonObject criteria = read(advancement).getAsJsonObject("criteria");
        assertEquals(expectedTrigger, criteria.getAsJsonObject(criterion).get("trigger").getAsString());
    }

    private static JsonObject read(String advancement) throws IOException {
        String path = "/data/customers/advancement/" + advancement + ".json";
        try (var input = CustomersAdvancementResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(input, path);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
