package com.vikingkittens.mc.customers.config;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.crafting.conditions.ICondition;

import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IConfigHelper;

public record RecipeEnabledCondition(String recipe) implements ICondition {
    public static final String CUSTOMER_SPAWNER_BLOCK = "customer_spawner_block";
    public static final String SUPPLIER_SPAWNER_BLOCK = "supplier_spawner_block";
    public static final String CUSTOMER_LEADERBOARD_BLOCK = "customer_leaderboard_block";
    @Override
    public boolean test(IContext context) {
        return test(CustomersServices.config());
    }

    boolean test(IConfigHelper config) {
        return switch (recipe) {
            case CUSTOMER_SPAWNER_BLOCK -> config.customerSpawnerRecipeEnabled();
            case SUPPLIER_SPAWNER_BLOCK -> config.supplierSpawnerRecipeEnabled();
            case CUSTOMER_LEADERBOARD_BLOCK -> config.customerLeaderboardRecipeEnabled();
            default -> false;
        };
    }

    @Override
    public net.minecraft.resources.ResourceLocation getID() {
        return new net.minecraft.resources.ResourceLocation("customers", "recipe_enabled");
    }
}
