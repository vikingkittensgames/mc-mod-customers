package com.vikingkittens.mc.customers.compatability;

import com.vikingkittens.mc.customers.config.Config;

public final class NeoForgeConfigHelper implements IConfigHelper {
    @Override
    public boolean customerSpawnerRecipeEnabled() {
        return Config.ENABLE_CUSTOMER_SPAWNER_BLOCK_RECIPE.get();
    }

    @Override
    public boolean supplierSpawnerRecipeEnabled() {
        return Config.ENABLE_SUPPLIER_SPAWNER_BLOCK_RECIPE.get();
    }

    @Override
    public boolean customerLeaderboardRecipeEnabled() {
        return Config.ENABLE_CUSTOMER_LEADERBOARD_BLOCK_RECIPE.get();
    }

    @Override
    public int maxCounterDistance() {
        return Config.MAX_COUNTER_DISTANCE.get();
    }

    @Override
    public int maxLeaderboardDistance() {
        return Config.MAX_LEADERBOARD_DISTANCE.get();
    }

    @Override
    public int defaultMaxCustomers() {
        return Config.MAX_CUSTOMERS.get();
    }

    @Override
    public int customerGiveUpSeconds() {
        return Config.CUSTOMER_GIVE_UP_SECONDS.get();
    }

    @Override
    public boolean buildCommandsEnabled() {
        return Config.ENABLE_BUILD_COMMANDS.get();
    }

    @Override
    public boolean quickSellEnabled() {
        return Config.ENABLE_QUICK_SELL.get();
    }

    @Override
    public boolean economyEnabled() {
        return Config.ENABLE_ECONOMY.get();
    }

    @Override
    public boolean economyUseVillagerShopSystem() {
        return Config.ECONOMY_USE_VILLAGER_SHOP_SYSTEM.get();
    }

    @Override
    public boolean economyUseProjectE() {
        return Config.ECONOMY_USE_PROJECT_E.get();
    }

    @Override
    public boolean forceAutoCost() {
        return Config.FORCE_AUTO_COST.get();
    }
}
