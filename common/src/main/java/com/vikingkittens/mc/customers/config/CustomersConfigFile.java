package com.vikingkittens.mc.customers.config;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class CustomersConfigFile {
    private CustomersConfigFile() {}

    public static CustomersConfig loadOrCreate(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.notExists(path)) {
            try {
                Files.writeString(path, defaultContents(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            } catch (FileAlreadyExistsException ignored) {}
        }
        return parse(Files.readString(path, StandardCharsets.UTF_8));
    }

    static CustomersConfig parse(String contents) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(contents));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Customers configuration", exception);
        }

        return new CustomersConfig(
                booleanValue(properties, CustomersConfig.ENABLE_CUSTOMER_SPAWNER_BLOCK_RECIPE,
                        CustomersConfig.DEFAULT.customerSpawnerRecipeEnabled()),
                booleanValue(properties, CustomersConfig.ENABLE_SUPPLIER_SPAWNER_BLOCK_RECIPE,
                        CustomersConfig.DEFAULT.supplierSpawnerRecipeEnabled()),
                booleanValue(properties, CustomersConfig.ENABLE_CUSTOMER_LEADERBOARD_BLOCK_RECIPE,
                        CustomersConfig.DEFAULT.customerLeaderboardRecipeEnabled()),
                positiveIntValue(properties, CustomersConfig.MAX_COUNTER_DISTANCE,
                        CustomersConfig.DEFAULT.maxCounterDistance()),
                positiveIntValue(properties, CustomersConfig.MAX_LEADERBOARD_DISTANCE,
                        CustomersConfig.DEFAULT.maxLeaderboardDistance()),
                positiveIntValue(properties, CustomersConfig.MAX_CUSTOMERS, CustomersConfig.DEFAULT.defaultMaxCustomers()),
                positiveIntValue(properties, CustomersConfig.CUSTOMER_GIVE_UP_SECONDS,
                        CustomersConfig.DEFAULT.customerGiveUpSeconds()),
                booleanValue(properties, CustomersConfig.ENABLE_BUILD_COMMANDS,
                        CustomersConfig.DEFAULT.buildCommandsEnabled()),
                booleanValue(properties, CustomersConfig.ENABLE_QUICK_SELL, CustomersConfig.DEFAULT.quickSellEnabled()),
                booleanValue(properties, CustomersConfig.CUSTOMERS_ARE_INVULNERABLE,
                        CustomersConfig.DEFAULT.customersAreInvulnerable()),
                booleanValue(properties, CustomersConfig.ENABLE_ECONOMY, CustomersConfig.DEFAULT.economyEnabled()),
                booleanValue(properties, CustomersConfig.ECONOMY_USE_VILLAGER_SHOP_SYSTEM,
                        CustomersConfig.DEFAULT.economyUseVillagerShopSystem()),
                booleanValue(properties, CustomersConfig.ECONOMY_USE_PROJECT_E,
                        CustomersConfig.DEFAULT.economyUseProjectE()),
                booleanValue(properties, CustomersConfig.FORCE_AUTO_COST, CustomersConfig.DEFAULT.forceAutoCost())
        );
    }

    private static boolean booleanValue(Properties properties, String key, boolean defaultValue) {
        return switch (properties.getProperty(key, "")) {
            case "true" -> true;
            case "false" -> false;
            default -> defaultValue;
        };
    }

    private static int positiveIntValue(Properties properties, String key, int defaultValue) {
        try {
            int value = Integer.parseInt(properties.getProperty(key, ""));
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static String defaultContents() {
        return """
                # Whether the customer spawner block recipe is enabled.
                enableCustomerSpawnerBlockRecipe = true
                # Whether the supplier spawner block recipe is enabled.
                enableSupplierSpawnerBlockRecipe = true
                # Whether the customer leaderboard block recipe is enabled.
                enableCustomerLeaderboardBlockRecipe = true
                # Range: > 1
                maxCounterDistance = 64
                # Range: > 1
                maxLeaderboardDistance = 64
                # Range: > 1
                maxCustomers = 4
                # Range: > 1
                customerGiveUpSeconds = 120
                enableBuildCommands = false
                enableQuickSell = true
                # Prevent customers, their pets, and suppliers from taking damage.
                customersAreInvulnerable = true
                # Enable Economy / Cost Suggestions
                enableEconomy = true
                # Use Villager Shop System Costs
                economyUseVillagerShopSystem = true
                # Use ProjectE Costs
                economyUseProjectE = true
                # Force Automatic Item Costs
                forceAutoCost = false
                """;
    }
}
