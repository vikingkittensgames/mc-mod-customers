package com.vikingkittens.mc.customers.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.config.CustomersRecipeConditions;

public final class CustomersFabricRecipeConditions {
    private CustomersFabricRecipeConditions() {}

    public static void initialize() {
        ResourceConditions.register(RecipeEnabledCondition.TYPE);
    }

    private record RecipeEnabledCondition(String recipe) implements ResourceCondition {
        private static final MapCodec<RecipeEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(Codec.STRING.fieldOf("recipe").forGetter(RecipeEnabledCondition::recipe))
                        .apply(builder, RecipeEnabledCondition::new)
        );
        private static final ResourceConditionType<RecipeEnabledCondition> TYPE = ResourceConditionType.create(
                Identifier.fromNamespaceAndPath(Customers.MODID, "recipe_enabled"), CODEC
        );

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.RegistryInfoLookup registryLookup) {
            return CustomersRecipeConditions.isEnabled(CustomersServices.config(), recipe);
        }
    }
}
