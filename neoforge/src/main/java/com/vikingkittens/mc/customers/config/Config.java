package com.vikingkittens.mc.customers.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOMER_SPAWNER_BLOCK_RECIPE = BUILDER
            .comment("Whether the customer spawner block recipe is enabled.")
            .define("enableCustomerSpawnerBlockRecipe", true);

    public static final ModConfigSpec.BooleanValue ENABLE_SUPPLIER_SPAWNER_BLOCK_RECIPE = BUILDER
            .comment("Whether the supplier spawner block recipe is enabled.")
            .define("enableSupplierSpawnerBlockRecipe", true);

    public static final ModConfigSpec.IntValue MAX_COUNTER_DISTANCE = BUILDER
            .defineInRange("maxCounterDistance", 64, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_CUSTOMERS = BUILDER
            .defineInRange("maxCustomers", 4, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CUSTOMER_GIVE_UP_SECONDS = BUILDER
            .defineInRange("customerGiveUpSeconds", 120, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ENABLE_BUILD_COMMANDS = BUILDER
            .define("enableBuildCommands", false);

    public static final ModConfigSpec.BooleanValue ENABLE_QUICK_SELL = BUILDER
            .define("enableQuickSell", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
