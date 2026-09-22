package com.vikingkittens.mc.customers.config;

import com.vikingkittens.mc.customers.compatability.IConfigHelper;

public final class CustomersRecipeConditions {
    public static final String CUSTOMER_SPAWNER_BLOCK = "customer_spawner_block";
    public static final String SUPPLIER_SPAWNER_BLOCK = "supplier_spawner_block";
    public static final String CUSTOMER_LEADERBOARD_BLOCK = "customer_leaderboard_block";

    private CustomersRecipeConditions() {}

    public static boolean isEnabled(IConfigHelper config, String recipe) {
        return switch (recipe) {
            case CUSTOMER_SPAWNER_BLOCK -> config.customerSpawnerRecipeEnabled();
            case SUPPLIER_SPAWNER_BLOCK -> config.supplierSpawnerRecipeEnabled();
            case CUSTOMER_LEADERBOARD_BLOCK -> config.customerLeaderboardRecipeEnabled();
            default -> false;
        };
    }
}
