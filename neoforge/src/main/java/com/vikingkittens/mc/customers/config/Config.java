package com.vikingkittens.mc.customers.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOMER_SPAWNER_BLOCK_RECIPE = BUILDER
            .comment("Whether the customer spawner block recipe is enabled.")
            .define(CustomersConfig.ENABLE_CUSTOMER_SPAWNER_BLOCK_RECIPE,
                    CustomersConfig.DEFAULT.customerSpawnerRecipeEnabled());

    public static final ModConfigSpec.BooleanValue ENABLE_SUPPLIER_SPAWNER_BLOCK_RECIPE = BUILDER
            .comment("Whether the supplier spawner block recipe is enabled.")
            .define(CustomersConfig.ENABLE_SUPPLIER_SPAWNER_BLOCK_RECIPE,
                    CustomersConfig.DEFAULT.supplierSpawnerRecipeEnabled());

    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOMER_LEADERBOARD_BLOCK_RECIPE = BUILDER
            .comment("Whether the customer leaderboard block recipe is enabled.")
            .define(CustomersConfig.ENABLE_CUSTOMER_LEADERBOARD_BLOCK_RECIPE,
                    CustomersConfig.DEFAULT.customerLeaderboardRecipeEnabled());

    public static final ModConfigSpec.IntValue MAX_COUNTER_DISTANCE = BUILDER
            .defineInRange(CustomersConfig.MAX_COUNTER_DISTANCE, CustomersConfig.DEFAULT.maxCounterDistance(), 1,
                    Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_LEADERBOARD_DISTANCE = BUILDER
            .defineInRange(CustomersConfig.MAX_LEADERBOARD_DISTANCE, CustomersConfig.DEFAULT.maxLeaderboardDistance(), 1,
                    Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_CUSTOMERS = BUILDER
            .defineInRange(CustomersConfig.MAX_CUSTOMERS, CustomersConfig.DEFAULT.defaultMaxCustomers(), 1,
                    Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CUSTOMER_GIVE_UP_SECONDS = BUILDER
            .defineInRange(CustomersConfig.CUSTOMER_GIVE_UP_SECONDS, CustomersConfig.DEFAULT.customerGiveUpSeconds(), 1,
                    Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ENABLE_BUILD_COMMANDS = BUILDER
            .define(CustomersConfig.ENABLE_BUILD_COMMANDS, CustomersConfig.DEFAULT.buildCommandsEnabled());

    public static final ModConfigSpec.BooleanValue ENABLE_QUICK_SELL = BUILDER
            .define(CustomersConfig.ENABLE_QUICK_SELL, CustomersConfig.DEFAULT.quickSellEnabled());

    public static final ModConfigSpec.BooleanValue CUSTOMERS_ARE_INVULNERABLE = BUILDER
            .comment("Prevent customers, their pets, and suppliers from taking damage.")
            .define(CustomersConfig.CUSTOMERS_ARE_INVULNERABLE,
                    CustomersConfig.DEFAULT.customersAreInvulnerable());

    public static final ModConfigSpec.BooleanValue ENABLE_ECONOMY = BUILDER
            .comment("Enable Economy / Cost Suggestions")
            .define(CustomersConfig.ENABLE_ECONOMY, CustomersConfig.DEFAULT.economyEnabled());

    public static final ModConfigSpec.BooleanValue ECONOMY_USE_VILLAGER_SHOP_SYSTEM = BUILDER
            .comment("Use Villager Shop System Costs")
            .define(CustomersConfig.ECONOMY_USE_VILLAGER_SHOP_SYSTEM,
                    CustomersConfig.DEFAULT.economyUseVillagerShopSystem());

    public static final ModConfigSpec.BooleanValue ECONOMY_USE_PROJECT_E = BUILDER
            .comment("Use ProjectE Costs")
            .define(CustomersConfig.ECONOMY_USE_PROJECT_E, CustomersConfig.DEFAULT.economyUseProjectE());

    public static final ModConfigSpec.BooleanValue FORCE_AUTO_COST = BUILDER
            .comment("Force Automatic Item Costs")
            .define(CustomersConfig.FORCE_AUTO_COST, CustomersConfig.DEFAULT.forceAutoCost());

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}
