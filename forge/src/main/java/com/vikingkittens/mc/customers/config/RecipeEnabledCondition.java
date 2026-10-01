package com.vikingkittens.mc.customers.config;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IConfigHelper;
import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;

public record RecipeEnabledCondition(String recipe) implements ICondition {
    public static final String CUSTOMER_SPAWNER_BLOCK =
            "customer_spawner_block";
    public static final String SUPPLIER_SPAWNER_BLOCK =
            "supplier_spawner_block";
    public static final String CUSTOMER_LEADERBOARD_BLOCK =
            "customer_leaderboard_block";
    public static final ResourceLocation ID =
            ResourceLocationCUtils.create(
                    Customers.MODID,
                    "recipe_enabled"
            );
    public static final IConditionSerializer<RecipeEnabledCondition> SERIALIZER =
            new IConditionSerializer<>() {
                @Override
                public void write(
                        JsonObject json,
                        RecipeEnabledCondition condition
                ) {
                    json.addProperty("recipe", condition.recipe());
                }

                @Override
                public RecipeEnabledCondition read(JsonObject json) {
                    return new RecipeEnabledCondition(
                            GsonHelper.getAsString(json, "recipe")
                    );
                }

                @Override
                public ResourceLocation getID() {
                    return ID;
                }
            };

    @Override
    public boolean test(IContext context) {
        return test(CustomersServices.config());
    }

    boolean test(IConfigHelper config) {
        return switch (recipe) {
            case CUSTOMER_SPAWNER_BLOCK ->
                    config.customerSpawnerRecipeEnabled();
            case SUPPLIER_SPAWNER_BLOCK ->
                    config.supplierSpawnerRecipeEnabled();
            case CUSTOMER_LEADERBOARD_BLOCK ->
                    config.customerLeaderboardRecipeEnabled();
            default -> false;
        };
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
