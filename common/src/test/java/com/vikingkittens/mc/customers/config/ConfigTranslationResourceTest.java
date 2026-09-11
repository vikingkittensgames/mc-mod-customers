package com.vikingkittens.mc.customers.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigTranslationResourceTest {
    private static final Path LANGUAGE_FILE =
            Path.of("src/main/resources/assets/customers/lang/en_us.json");

    @Test
    void providesFriendlyNamesForEconomyAndLeaderboardDistanceOptions() throws IOException {
        JsonObject translations =
                JsonParser.parseString(Files.readString(LANGUAGE_FILE)).getAsJsonObject();
        Map<String, String> expected = Map.of(
                "maxLeaderboardDistance", "Maximum Leaderboard Distance",
                "enableEconomy", "Enable Economy / Cost Suggestions",
                "economyUseVillagerShopSystem", "Use Villager Shop System Costs",
                "economyUseProjectE", "Use ProjectE Costs",
                "forceAutoCost", "Force Automatic Item Costs"
        );

        expected.forEach((name, friendlyName) -> assertEquals(
                friendlyName,
                translations.get("customers.configuration." + name).getAsString()
        ));
    }
}
