package com.vikingkittens.mc.customers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.common.conditions.ICondition;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public record RecipeEnabledCondition(String recipe) implements ICondition {
    public static final MapCodec<RecipeEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(Codec.STRING.fieldOf("recipe").forGetter(RecipeEnabledCondition::recipe))
                    .apply(builder, RecipeEnabledCondition::new)
    );

    @Override
    public boolean test(IContext context) {
        return CustomersRecipeConditions.isEnabled(CustomersServices.config(), recipe);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
