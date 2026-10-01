package com.vikingkittens.mc.customers.fabric;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;
import com.vikingkittens.mc.customers.config.CustomersRecipeConditions;

public final class CustomersFabricRecipeConditions {
    private static final ResourceLocation RECIPE_ENABLED =
            ResourceLocationCUtils.create(
                    Customers.MODID,
                    "recipe_enabled"
            );

    private CustomersFabricRecipeConditions() {}

    public static void initialize() {
        ResourceConditions.register(
                RECIPE_ENABLED,
                condition -> CustomersRecipeConditions.isEnabled(
                        CustomersServices.config(),
                        condition.get("recipe").getAsString()
                )
        );
    }
}
