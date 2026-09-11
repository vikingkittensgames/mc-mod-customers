package com.vikingkittens.mc.customers.economy;

import java.math.BigInteger;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class Economy {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EconomyItemCostProviderVillagerShopSystem VILLAGER_SHOP_SYSTEM =
            new EconomyItemCostProviderVillagerShopSystem(LOGGER);
    private static final EconomyItemCostProviderProjectE PROJECT_E = new EconomyItemCostProviderProjectE(LOGGER);
    private static final EconomyItemCostProviderTradesAndRecipes TRADES_AND_RECIPES =
            new EconomyItemCostProviderTradesAndRecipes();
    private static final EconomyItemCostProviderDefault DEFAULT = new EconomyItemCostProviderDefault(LOGGER);

    private static volatile EconomyItemCostProviderManual manual =
            new EconomyItemCostProviderManual(List.of());
    private static volatile EconomyCurrency currency = new EconomyCurrency(List.of());

    private Economy() {}

    @Nullable
    public static ItemStack calculateItemStackCost(ItemStack item) {
        return calculateItemStackCost(
                item,
                isEnabled(),
                List.of(manual, VILLAGER_SHOP_SYSTEM, PROJECT_E, TRADES_AND_RECIPES, DEFAULT),
                currency
        );
    }

    @Nullable
    static ItemStack calculateItemStackCost(
            ItemStack item,
            boolean enabled,
            List<EconomyItemCostProvider> providers,
            EconomyCurrency currency
    ) {
        if (!enabled || item.isEmpty()) {
            return null;
        }
        for (EconomyItemCostProvider provider : providers) {
            ItemStack cost = provider.calculateItemStackCost(item);
            if (cost != null && !cost.isEmpty()) {
                return currency.convert(cost);
            }
        }
        return null;
    }

    public static boolean isEnabled() {
        return CustomersServices.config().economyEnabled();
    }

    public static boolean forceAutoCost() {
        return isEnabled() && CustomersServices.config().forceAutoCost();
    }

    public static void serverStarted(MinecraftServer server) {
        DEFAULT.serverStarted(server.overworld().getDayTime() / 24_000L, System.currentTimeMillis());
        if (isEnabled()) {
            TRADES_AND_RECIPES.rebuild(server);
            VILLAGER_SHOP_SYSTEM.serverStarted(server);
        }
    }

    static void applyData(EconomyData data) {
        manual = new EconomyItemCostProviderManual(data.itemCosts());
        currency = new EconomyCurrency(data.currencyConversions());
    }

    public static void serverTick(MinecraftServer server) {
        if (isEnabled()) {
            DEFAULT.tick(server.overworld().getDayTime() / 24_000L, System.currentTimeMillis());
        }
    }

    static int safeCount(long count) {
        return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, (int) count);
    }

    static int safeCount(BigInteger count) {
        return count.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0
                ? Integer.MAX_VALUE
                : Math.max(1, count.intValue());
    }
}
