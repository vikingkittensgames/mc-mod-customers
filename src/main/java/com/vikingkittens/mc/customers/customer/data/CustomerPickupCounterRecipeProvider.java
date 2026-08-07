package com.vikingkittens.mc.customers.customer.data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;

public class CustomerPickupCounterRecipeProvider extends RecipeProvider.Runner {
    public CustomerPickupCounterRecipeProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(
            HolderLookup.Provider registries,
            RecipeOutput output
    ) {
        return new RecipeProvider(registries, output) {
            @Override
            protected void buildRecipes() {
        for (Map.Entry<
                CustomerPickupCounterVariant,
                ? extends java.util.function.Supplier<
                        CustomerPickupCounterBlock
                >
        > entry : CustomerPickupCounter.BLOCKS.entrySet()) {
            CustomerPickupCounterVariant variant = entry.getKey();
            CustomerPickupCounterBlock block = entry.getValue().get();
            ShapedRecipeBuilder.shaped(
                    items,
                    RecipeCategory.DECORATIONS,
                    block
            )
                    .pattern("IVV")
                    .define('I', Items.IRON_INGOT)
                    .define('V', variant.ingredient().get())
                    .unlockedBy(
                            getHasName(variant.ingredient().get()),
                            has(variant.ingredient().get())
                    )
                    .save(output);
                }
            }
        };
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
