package com.vikingkittens.mc.customers.customer.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import net.neoforged.neoforge.common.conditions.ICondition;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxBlock;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;

public final class CustomerRecipeProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public CustomerRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return registries.thenCompose(registryLookup -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            RecipeOutput recipeOutput = new Output(
                    cachedOutput,
                    registryLookup,
                    output.createRegistryElementsPathProvider(Registries.RECIPE),
                    output.createRegistryElementsPathProvider(Registries.ADVANCEMENT),
                    futures
            );
            new Recipes(registryLookup, recipeOutput).buildRecipes();
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Customers Recipes";
    }

    private static final class Recipes extends RecipeProvider {
        private Recipes(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        protected void buildRecipes() {
            buildPickupCounterRecipes();
            buildPaymentBoxRecipes();
        }

        private void buildPickupCounterRecipes() {
            for (Map.Entry<CustomerOverlayBlockVariant, ? extends java.util.function.Supplier<CustomerPickupCounterBlock>> entry
                    : CustomerPickupCounter.BLOCKS.entrySet()) {
                CustomerOverlayBlockVariant variant = entry.getKey();
                CustomerPickupCounterBlock block = entry.getValue().get();
                String name = CustomerPickupCounter.getBlockName(variant);

                shaped(RecipeCategory.DECORATIONS, block)
                        .pattern("IVV")
                        .define('I', Items.IRON_INGOT)
                        .define('V', variant.ingredient().get())
                        .unlockedBy(getHasName(variant.ingredient().get()), has(variant.ingredient().get()))
                        .save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Customers.MODID, name)));
            }
        }

        private void buildPaymentBoxRecipes() {
            for (Map.Entry<CustomerOverlayBlockVariant, ? extends java.util.function.Supplier<CustomerPaymentBoxBlock>> entry
                    : CustomerPaymentBox.BLOCKS.entrySet()) {
                CustomerOverlayBlockVariant variant = entry.getKey();
                CustomerPaymentBoxBlock block = entry.getValue().get();
                String name = CustomerPaymentBox.getBlockName(variant);

                shaped(RecipeCategory.DECORATIONS, block)
                        .pattern("VGV")
                        .pattern("VEV")
                        .pattern("VVV")
                        .define('V', variant.ingredient().get())
                        .define('G', Items.GOLD_INGOT)
                        .define('E', Items.EMERALD)
                        .unlockedBy(getHasName(variant.ingredient().get()), has(variant.ingredient().get()))
                        .save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Customers.MODID, name)));
            }
        }
    }

    private static final class Output implements RecipeOutput {
        private final Set<ResourceKey<Recipe<?>>> recipeKeys = new HashSet<>();
        private final CachedOutput cachedOutput;
        private final HolderLookup.Provider registries;
        private final PackOutput.PathProvider recipes;
        private final PackOutput.PathProvider advancements;
        private final List<CompletableFuture<?>> futures;

        private Output(
                CachedOutput cachedOutput,
                HolderLookup.Provider registries,
                PackOutput.PathProvider recipes,
                PackOutput.PathProvider advancements,
                List<CompletableFuture<?>> futures
        ) {
            this.cachedOutput = cachedOutput;
            this.registries = registries;
            this.recipes = recipes;
            this.advancements = advancements;
            this.futures = futures;
        }

        @Override
        public void accept(ResourceKey<Recipe<?>> recipeKey, Recipe<?> recipe, AdvancementHolder advancement) {
            if (!recipeKeys.add(recipeKey)) {
                throw new IllegalStateException("Duplicate recipe " + recipeKey.identifier());
            }
            futures.add(DataProvider.saveStable(
                    cachedOutput,
                    registries,
                    Recipe.CODEC,
                    recipe,
                    recipes.json(recipeKey.identifier())
            ));
            if (advancement != null) {
                futures.add(DataProvider.saveStable(
                        cachedOutput,
                        registries,
                        Advancement.CODEC,
                        advancement.value(),
                        advancements.json(advancement.id())
                ));
            }
        }

        public void accept(
                ResourceKey<Recipe<?>> recipeKey,
                Recipe<?> recipe,
                AdvancementHolder advancement,
                ICondition... conditions
        ) {
            accept(recipeKey, recipe, advancement);
        }

        @Override
        public Advancement.Builder advancement() {
            return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        }

        @Override
        public void includeRootAdvancement() {
            AdvancementHolder advancement = Advancement.Builder.recipeAdvancement()
                    .addCriterion(
                            "impossible",
                            CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
                    )
                    .build(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            futures.add(DataProvider.saveStable(
                    cachedOutput,
                    registries,
                    Advancement.CODEC,
                    advancement.value(),
                    advancements.json(advancement.id())
            ));
        }
    }
}
