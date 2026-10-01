package com.vikingkittens.mc.customers.config;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;

public final class RecipeConditions {
    private RecipeConditions() {}

    public static void register(IEventBus ignored) {
        CraftingHelper.register(RecipeEnabledCondition.SERIALIZER);
    }
}
