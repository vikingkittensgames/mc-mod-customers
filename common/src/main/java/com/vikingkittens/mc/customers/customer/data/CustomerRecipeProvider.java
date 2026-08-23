package com.vikingkittens.mc.customers.customer.data;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxBlock;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;

public final class CustomerRecipeProvider extends RecipeProvider {
    public CustomerRecipeProvider(
            PackOutput output,
            Object ignored
    ) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        buildPickupCounterRecipes(output);
        buildPaymentBoxRecipes(output);
    }

    private void buildPickupCounterRecipes(Consumer<FinishedRecipe> output) {
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

    private void buildPaymentBoxRecipes(Consumer<FinishedRecipe> output) {
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

}
