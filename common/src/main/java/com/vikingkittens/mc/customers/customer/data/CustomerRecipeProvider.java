package com.vikingkittens.mc.customers.customer.data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardBlock;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxBlock;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;

public final class CustomerRecipeProvider extends RecipeProvider {
    public CustomerRecipeProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildPickupCounterRecipes(output);
        buildPaymentBoxRecipes(output);
        buildLeaderboardRecipe(output);
    }

    private void buildPickupCounterRecipes(RecipeOutput output) {
        for (Map.Entry<
                CustomerOverlayBlockVariant,
                ? extends java.util.function.Supplier<
                        CustomerPickupCounterBlock
                >
        > entry : CustomerPickupCounter.BLOCKS.entrySet()) {
            CustomerOverlayBlockVariant variant = entry.getKey();
            CustomerPickupCounterBlock block = entry.getValue().get();
            String name = CustomerPickupCounter.getBlockName(variant);

            ShapedRecipeBuilder.shaped(
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
                    .save(
                            output,
                            ResourceLocation.fromNamespaceAndPath(
                                    Customers.MODID,
                                    name
                            )
                    );
        }
    }

    private void buildPaymentBoxRecipes(RecipeOutput output) {
        for (Map.Entry<
                CustomerOverlayBlockVariant,
                ? extends java.util.function.Supplier<
                        CustomerPaymentBoxBlock
                >
        > entry : CustomerPaymentBox.BLOCKS.entrySet()) {
            CustomerOverlayBlockVariant variant = entry.getKey();
            CustomerPaymentBoxBlock block = entry.getValue().get();
            String name = CustomerPaymentBox.getBlockName(variant);

            ShapedRecipeBuilder.shaped(
                    RecipeCategory.DECORATIONS,
                    block
            )
                    .pattern("VGV")
                    .pattern("VEV")
                    .pattern("VVV")
                    .define('V', variant.ingredient().get())
                    .define('G', Items.GOLD_INGOT)
                    .define('E', Items.EMERALD)
                    .unlockedBy(
                            getHasName(variant.ingredient().get()),
                            has(variant.ingredient().get())
                    )
                    .save(
                            output,
                            ResourceLocation.fromNamespaceAndPath(
                                    Customers.MODID,
                                    name
                            )
                    );
        }
    }

    private void buildLeaderboardRecipe(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, CustomerLeaderboard.BLOCK.get())
                .pattern("SIS")
                .pattern("PEP")
                .pattern("SXS")
                .define('S', Ingredient.of(
                        Items.STRIPPED_ACACIA_LOG,
                        Items.STRIPPED_BIRCH_LOG,
                        Items.STRIPPED_CHERRY_LOG,
                        Items.STRIPPED_DARK_OAK_LOG,
                        Items.STRIPPED_JUNGLE_LOG,
                        Items.STRIPPED_MANGROVE_LOG,
                        Items.STRIPPED_OAK_LOG,
                        Items.STRIPPED_SPRUCE_LOG,
                        Items.STRIPPED_CRIMSON_STEM,
                        Items.STRIPPED_WARPED_STEM
                ))
                .define('I', Items.IRON_INGOT)
                .define('P', Items.PAPER)
                .define('E', Items.EMERALD)
                .define('X', Items.INK_SAC)
                .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                .save(
                        output,
                        ResourceLocation.fromNamespaceAndPath(
                                Customers.MODID,
                                CustomerLeaderboardBlock.NAME
                        )
                );
    }
}
