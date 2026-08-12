package com.vikingkittens.mc.customers.config;

import com.mojang.serialization.MapCodec;

import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.vikingkittens.mc.customers.Customers;

public final class RecipeConditions {
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.CONDITION_SERIALIZERS, Customers.MODID);

    public static final RegistryObject<MapCodec<RecipeEnabledCondition>> RECIPE_ENABLED =
            CONDITION_SERIALIZERS.register("recipe_enabled", () -> RecipeEnabledCondition.CODEC);

    private RecipeConditions() {}

    public static void register(IEventBus modEventBus) {
        CONDITION_SERIALIZERS.register(modEventBus);
    }
}
