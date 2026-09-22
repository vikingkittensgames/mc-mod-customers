package com.vikingkittens.mc.customers.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomersConfigFileTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void readsConfiguredValues() {
        CustomersConfig config = CustomersConfigFile.parse("""
                enableCustomerSpawnerBlockRecipe = false
                enableSupplierSpawnerBlockRecipe = false
                enableCustomerLeaderboardBlockRecipe = false
                maxCounterDistance = 16
                maxLeaderboardDistance = 32
                maxCustomers = 8
                customerGiveUpSeconds = 90
                enableBuildCommands = true
                enableQuickSell = true
                enableEconomy = false
                economyUseVillagerShopSystem = false
                economyUseProjectE = false
                forceAutoCost = true
                """);

        assertEquals(false, config.customerSpawnerRecipeEnabled());
        assertEquals(false, config.supplierSpawnerRecipeEnabled());
        assertEquals(false, config.customerLeaderboardRecipeEnabled());
        assertEquals(16, config.maxCounterDistance());
        assertEquals(32, config.maxLeaderboardDistance());
        assertEquals(8, config.defaultMaxCustomers());
        assertEquals(90, config.customerGiveUpSeconds());
        assertEquals(true, config.buildCommandsEnabled());
        assertEquals(true, config.quickSellEnabled());
        assertEquals(false, config.economyEnabled());
        assertEquals(false, config.economyUseVillagerShopSystem());
        assertEquals(false, config.economyUseProjectE());
        assertEquals(true, config.forceAutoCost());
    }

    @Test
    void keepsDefaultsForMissingOrInvalidValues() {
        CustomersConfig config = CustomersConfigFile.parse("""
                maxCounterDistance = 0
                maxLeaderboardDistance = not-a-number
                enableQuickSell = not-a-boolean
                """);

        assertEquals(CustomersConfig.DEFAULT, config);
    }

    @Test
    void createsTheSharedDefaultConfigurationFile() throws IOException {
        Path path = temporaryDirectory.resolve(CustomersConfig.FILE_NAME);

        CustomersConfig config = CustomersConfigFile.loadOrCreate(path);

        assertEquals(CustomersConfig.DEFAULT, config);
        assertTrue(Files.exists(path));
        assertTrue(Files.readString(path).contains("enableCustomerSpawnerBlockRecipe = true"));
        assertTrue(Files.readString(path).contains("forceAutoCost = false"));
    }
}
