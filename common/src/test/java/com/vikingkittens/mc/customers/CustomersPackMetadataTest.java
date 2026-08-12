package com.vikingkittens.mc.customers;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomersPackMetadataTest {
    @Test
    void supportsMinecraft1211ResourceAndDataPacks() throws Exception {
        try (var stream = getClass().getResourceAsStream("/pack.mcmeta")) {
            assertNotNull(stream);
            JsonObject pack = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject().getAsJsonObject("pack");

            assertEquals(34, pack.get("pack_format").getAsInt());
            assertEquals(
                    34,
                    pack.getAsJsonObject("supported_formats").get("min_inclusive").getAsInt()
            );
            assertEquals(
                    48,
                    pack.getAsJsonObject("supported_formats").get("max_inclusive").getAsInt()
            );
        }
    }
}
