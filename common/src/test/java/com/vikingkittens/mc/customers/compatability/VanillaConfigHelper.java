package com.vikingkittens.mc.customers.compatability;

public final class VanillaConfigHelper implements IConfigHelper {
    @Override
    public boolean customerSpawnerRecipeEnabled() {
        return true;
    }

    @Override
    public boolean supplierSpawnerRecipeEnabled() {
        return true;
    }

    @Override
    public int maxCounterDistance() {
        return 64;
    }

    @Override
    public int maxLeaderboardDistance() {
        return 64;
    }

    @Override
    public int defaultMaxCustomers() {
        return 4;
    }

    @Override
    public int customerGiveUpSeconds() {
        return 120;
    }

    @Override
    public boolean buildCommandsEnabled() {
        return false;
    }

    @Override
    public boolean quickSellEnabled() {
        return false;
    }
}
