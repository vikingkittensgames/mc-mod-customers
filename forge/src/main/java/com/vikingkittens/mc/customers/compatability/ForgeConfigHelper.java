package com.vikingkittens.mc.customers.compatability;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ForgeConfigHelper implements IConfigHelper {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CUSTOMER_SPAWNER_RECIPE =
            BUILDER.define("enableCustomerSpawnerBlockRecipe", true);
    private static final ForgeConfigSpec.BooleanValue SUPPLIER_SPAWNER_RECIPE =
            BUILDER.define("enableSupplierSpawnerBlockRecipe", true);
    private static final ForgeConfigSpec.BooleanValue CUSTOMER_LEADERBOARD_RECIPE =
            BUILDER.define("enableCustomerLeaderboardBlockRecipe", true);
    private static final ForgeConfigSpec.IntValue MAX_COUNTER_DISTANCE =
            BUILDER.defineInRange("maxCounterDistance", 64, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MAX_LEADERBOARD_DISTANCE =
            BUILDER.defineInRange("maxLeaderboardDistance", 64, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue MAX_CUSTOMERS =
            BUILDER.defineInRange("maxCustomers", 4, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue CUSTOMER_GIVE_UP_SECONDS =
            BUILDER.defineInRange("customerGiveUpSeconds", 120, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue BUILD_COMMANDS = BUILDER.define("enableBuildCommands", false);
    private static final ForgeConfigSpec.BooleanValue QUICK_SELL = BUILDER.define("enableQuickSell", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @Override
    public boolean customerSpawnerRecipeEnabled() {
        return CUSTOMER_SPAWNER_RECIPE.get();
    }

    @Override
    public boolean supplierSpawnerRecipeEnabled() {
        return SUPPLIER_SPAWNER_RECIPE.get();
    }

    @Override
    public boolean customerLeaderboardRecipeEnabled() {
        return CUSTOMER_LEADERBOARD_RECIPE.get();
    }

    @Override
    public int maxCounterDistance() {
        return MAX_COUNTER_DISTANCE.get();
    }

    @Override
    public int maxLeaderboardDistance() {
        return MAX_LEADERBOARD_DISTANCE.get();
    }

    @Override
    public int defaultMaxCustomers() {
        return MAX_CUSTOMERS.get();
    }

    @Override
    public int customerGiveUpSeconds() {
        return CUSTOMER_GIVE_UP_SECONDS.get();
    }

    @Override
    public boolean buildCommandsEnabled() {
        return BUILD_COMMANDS.get();
    }

    @Override
    public boolean quickSellEnabled() {
        return QUICK_SELL.get();
    }
}
