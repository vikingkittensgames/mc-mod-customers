package com.vikingkittens.mc.customers.compatability;

public interface IConfigHelper {
    boolean customerSpawnerRecipeEnabled();

    boolean supplierSpawnerRecipeEnabled();

    int maxCounterDistance();

    int defaultMaxCustomers();

    int customerGiveUpSeconds();

    boolean buildCommandsEnabled();

    boolean quickSellEnabled();
}
