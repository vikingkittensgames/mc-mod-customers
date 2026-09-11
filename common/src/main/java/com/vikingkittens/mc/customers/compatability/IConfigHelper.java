package com.vikingkittens.mc.customers.compatability;

public interface IConfigHelper {
    boolean customerSpawnerRecipeEnabled();

    boolean supplierSpawnerRecipeEnabled();

    boolean customerLeaderboardRecipeEnabled();

    int maxCounterDistance();

    int maxLeaderboardDistance();

    int defaultMaxCustomers();

    int customerGiveUpSeconds();

    boolean buildCommandsEnabled();

    boolean quickSellEnabled();

    boolean economyEnabled();

    boolean economyUseVillagerShopSystem();

    boolean economyUseProjectE();

    boolean forceAutoCost();
}
