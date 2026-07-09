package com.vikingkittens.mc.customers.config;

import com.mojang.serialization.MapCodec;
import com.vikingkittens.mc.customers.Customers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class RecipeConditions {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Customers.MODID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<RecipeEnabledCondition>> RECIPE_ENABLED =
            CONDITION_SERIALIZERS.register("recipe_enabled", () -> RecipeEnabledCondition.CODEC);

    public static void register(IEventBus modEventBus) {
        CONDITION_SERIALIZERS.register(modEventBus);
    }
}
