package com.vikingkittens.mc.customers.economy;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.trading.MerchantOffer;

public final class EconomyItemCostProviderTradesAndRecipes implements EconomyItemCostProvider {
    private static final int MAX_PROPAGATION_PASSES = 64;

    private volatile Map<Item, Price> prices = Map.of();

    @Nullable
    @Override
    public ItemStack calculateItemStackCost(ItemStack item) {
        Price price = prices.get(item.getItem());
        if (price == null) {
            return null;
        }
        return new ItemStack(Items.EMERALD, Economy.safeCount(price.multiply(item.getCount()).ceil()));
    }

    public void rebuild(MinecraftServer server) {
        Map<Item, Price> rebuilt = new HashMap<>();
        ServerLevel level = server.overworld();
        Villager villager = new Villager(net.minecraft.world.entity.EntityType.VILLAGER, level);
        scanVillagerTrades(level, villager, rebuilt);
        for (int pass = 0; pass < MAX_PROPAGATION_PASSES; pass++) {
            Map<Item, Price> candidates = new HashMap<>();
            for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
                propagate(holder.value(), server, rebuilt, candidates);
            }
            candidates.keySet().removeAll(rebuilt.keySet());
            if (candidates.isEmpty()) {
                break;
            }
            rebuilt.putAll(candidates);
        }
        prices = Map.copyOf(rebuilt);
    }

    private static void scanVillagerTrades(
            ServerLevel level,
            Villager villager,
            Map<Item, Price> prices
    ) {
        for (Map.Entry<ResourceKey<VillagerProfession>, Int2ObjectMap<VillagerTrades.ItemListing[]>> profession
                : VillagerTrades.TRADES.entrySet()) {
            for (VillagerTrades.ItemListing[] listings : profession.getValue().values()) {
                scanListings(level, listings, villager, prices);
            }
        }
        for (Pair<VillagerTrades.ItemListing[], Integer> tradeGroup
                : VillagerTrades.WANDERING_TRADER_TRADES) {
            scanListings(level, tradeGroup.getLeft(), villager, prices);
        }
    }

    private static void scanListings(
            ServerLevel level,
            VillagerTrades.ItemListing[] listings,
            Villager villager,
            Map<Item, Price> prices
    ) {
        if (listings == null) {
            return;
        }
        for (VillagerTrades.ItemListing listing : listings) {
            try {
                MerchantOffer offer = listing.getOffer(level, villager, villager.getRandom());
                if (offer != null) {
                    addOffer(offer, prices);
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void addOffer(MerchantOffer offer, Map<Item, Price> prices) {
        ItemStack cost = offer.getBaseCostA();
        ItemStack secondCost = offer.getCostB();
        ItemStack result = offer.getResult();
        if (result.is(Items.EMERALD) && !cost.is(Items.EMERALD)) {
            addLowest(prices, cost.getItem(), Price.of(result.getCount(), cost.getCount()));
        } else if (cost.is(Items.EMERALD) && secondCost.isEmpty() && !result.is(Items.EMERALD)) {
            addLowest(prices, result.getItem(), Price.of(cost.getCount(), result.getCount()));
        }
    }

    private static void propagate(
            Recipe<?> recipe,
            MinecraftServer server,
            Map<Item, Price> prices,
            Map<Item, Price> candidates
    ) {
        ItemStack result = recipe.display().stream()
                .map(display -> display.result().resolveForFirstStack(
                        SlotDisplayContext.fromLevel(server.overworld())
                ))
                .filter(stack -> !stack.isEmpty())
                .findFirst()
                .orElse(ItemStack.EMPTY);
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        if (result.isEmpty() || result.is(Items.AIR) || ingredients.isEmpty()) {
            return;
        }
        Price ingredientTotal = Price.ZERO;
        boolean allIngredientsKnown = true;
        int ingredientUnits = 0;
        for (Ingredient ingredient : ingredients) {
            List<ItemStack> alternatives = ingredient.items().map(ItemStack::new).toList();
            if (alternatives.isEmpty()) {
                continue;
            }
            ingredientUnits += Math.max(1, alternatives.getFirst().getCount());
            Price cheapest = null;
            for (ItemStack alternative : alternatives) {
                Price alternativePrice = prices.get(alternative.getItem());
                if (alternativePrice != null) {
                    Price stackPrice = alternativePrice.multiply(Math.max(1, alternative.getCount()));
                    cheapest = cheapest == null || stackPrice.compareTo(cheapest) < 0 ? stackPrice : cheapest;
                }
            }
            if (cheapest == null) {
                allIngredientsKnown = false;
            } else {
                ingredientTotal = ingredientTotal.add(cheapest);
            }
        }
        if (allIngredientsKnown && ingredientUnits > 0 && !prices.containsKey(result.getItem())) {
            addLowest(
                    candidates,
                    result.getItem(),
                    ingredientTotal.divide(Math.max(1, result.getCount()))
            );
        }
        Price resultPrice = prices.get(result.getItem());
        if (resultPrice == null || ingredientUnits == 0) {
            return;
        }
        Price perIngredientUnit = resultPrice.multiply(Math.max(1, result.getCount())).divide(ingredientUnits);
        for (Ingredient ingredient : ingredients) {
            for (ItemStack alternative : ingredient.items().map(ItemStack::new).toList()) {
                if (!prices.containsKey(alternative.getItem())) {
                    addLowest(candidates, alternative.getItem(), perIngredientUnit);
                }
            }
        }
    }

    private static boolean addLowest(Map<Item, Price> prices, Item item, Price candidate) {
        if (candidate.compareTo(Price.ZERO) <= 0) {
            return false;
        }
        Price current = prices.get(item);
        if (current == null || candidate.compareTo(current) < 0) {
            prices.put(item, candidate);
            return true;
        }
        return false;
    }

    private record Price(BigInteger numerator, BigInteger denominator) implements Comparable<Price> {
        private static final Price ZERO = new Price(BigInteger.ZERO, BigInteger.ONE);

        private Price {
            BigInteger divisor = numerator.gcd(denominator);
            numerator = numerator.divide(divisor);
            denominator = denominator.divide(divisor);
        }

        static Price of(long numerator, long denominator) {
            return new Price(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
        }

        Price add(Price other) {
            return new Price(
                    numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
                    denominator.multiply(other.denominator)
            );
        }

        Price multiply(long value) {
            return new Price(numerator.multiply(BigInteger.valueOf(value)), denominator);
        }

        Price divide(long value) {
            return new Price(numerator, denominator.multiply(BigInteger.valueOf(value)));
        }

        BigInteger ceil() {
            return numerator.add(denominator).subtract(BigInteger.ONE).divide(denominator);
        }

        @Override
        public int compareTo(Price other) {
            return numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator));
        }
    }
}
