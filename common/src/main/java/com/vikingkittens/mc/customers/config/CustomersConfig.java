package com.vikingkittens.mc.customers.config;

import com.vikingkittens.mc.customers.compatability.IConfigHelper;

public record CustomersConfig(
        boolean customerSpawnerRecipeEnabled,
        boolean supplierSpawnerRecipeEnabled,
        boolean customerLeaderboardRecipeEnabled,
        int maxCounterDistance,
        int maxLeaderboardDistance,
        int defaultMaxCustomers,
        int customerGiveUpSeconds,
        boolean buildCommandsEnabled,
        boolean quickSellEnabled,
        boolean customersAreInvulnerable,
        boolean economyEnabled,
        boolean economyUseVillagerShopSystem,
        boolean economyUseProjectE,
        boolean forceAutoCost
) implements IConfigHelper {
    public static final String FILE_NAME = "customers-common.toml";
    public static final String ENABLE_CUSTOMER_SPAWNER_BLOCK_RECIPE = "enableCustomerSpawnerBlockRecipe";
    public static final String ENABLE_SUPPLIER_SPAWNER_BLOCK_RECIPE = "enableSupplierSpawnerBlockRecipe";
    public static final String ENABLE_CUSTOMER_LEADERBOARD_BLOCK_RECIPE = "enableCustomerLeaderboardBlockRecipe";
    public static final String MAX_COUNTER_DISTANCE = "maxCounterDistance";
    public static final String MAX_LEADERBOARD_DISTANCE = "maxLeaderboardDistance";
    public static final String MAX_CUSTOMERS = "maxCustomers";
    public static final String CUSTOMER_GIVE_UP_SECONDS = "customerGiveUpSeconds";
    public static final String ENABLE_BUILD_COMMANDS = "enableBuildCommands";
    public static final String ENABLE_QUICK_SELL = "enableQuickSell";
    public static final String CUSTOMERS_ARE_INVULNERABLE = "customersAreInvulnerable";
    public static final String ENABLE_ECONOMY = "enableEconomy";
    public static final String ECONOMY_USE_VILLAGER_SHOP_SYSTEM = "economyUseVillagerShopSystem";
    public static final String ECONOMY_USE_PROJECT_E = "economyUseProjectE";
    public static final String FORCE_AUTO_COST = "forceAutoCost";

    public static final CustomersConfig DEFAULT = new CustomersConfig(
            true, true, true, 64, 64, 4, 120, false, true, true, true, true, true, false
    );
}
