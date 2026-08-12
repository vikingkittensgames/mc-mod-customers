package com.vikingkittens.mc.customers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.common.conditions.ICondition;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public record RecipeEnabledCondition(String recipe) implements ICondition {
    public static final String CUSTOMER_SPAWNER_BLOCK = "customer_spawner_block";
    public static final String SUPPLIER_SPAWNER_BLOCK = "supplier_spawner_block";

    public static final MapCodec<RecipeEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(Codec.STRING.fieldOf("recipe").forGetter(RecipeEnabledCondition::recipe))
                    .apply(builder, RecipeEnabledCondition::new)
    );

    @Override
    public boolean test(IContext context) {
        return switch (recipe) {
            case CUSTOMER_SPAWNER_BLOCK -> CustomersServices.config().customerSpawnerRecipeEnabled();
            case SUPPLIER_SPAWNER_BLOCK -> CustomersServices.config().supplierSpawnerRecipeEnabled();
            default -> false;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
