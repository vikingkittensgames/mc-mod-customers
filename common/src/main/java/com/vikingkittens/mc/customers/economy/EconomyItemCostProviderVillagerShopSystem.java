package com.vikingkittens.mc.customers.economy;

import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class EconomyItemCostProviderVillagerShopSystem implements EconomyItemCostProvider {
    static final String MOD_ID = "village_shop_system";
    private static final String CALCULATOR_CLASS =
            "com.github.leopoko.village_shop_system.trade.TradePriceCalculator";
    private static final String REGISTRY_CLASS =
            "com.github.leopoko.village_shop_system.trade.TradeRegistry";

    private final Logger logger;
    private boolean lookupAttempted;
    private boolean apiLoaded;
    private boolean failureLogged;
    private Method calculateSellPrice;
    private Method getSellTradeRatio;
    private Method isSellable;
    private Method registryInitialized;
    private Method initializeRegistry;

    public EconomyItemCostProviderVillagerShopSystem(Logger logger) {
        this.logger = logger;
    }

    @Nullable
    @Override
    public ItemStack calculateItemStackCost(ItemStack item) {
        if (!CustomersServices.config().economyUseVillagerShopSystem()
                || !CustomersServices.platform().isModLoaded(MOD_ID)
                || !loadApi()) {
            return null;
        }
        try {
            if (!(boolean) isSellable.invoke(null, item.getItem())) {
                return null;
            }
            int price = (int) calculateSellPrice.invoke(null, item);
            if (price <= 0) {
                int[] ratio = (int[]) getSellTradeRatio.invoke(null, item.getItem());
                if (ratio != null && ratio.length == 2 && ratio[0] > 0 && ratio[1] > 0) {
                    price = Economy.safeCount(((long) ratio[0] * item.getCount() + ratio[1] - 1) / ratio[1]);
                }
            }
            return price > 0 ? new ItemStack(Items.EMERALD, price) : null;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logFailure("Unable to calculate a cost with Village Shop System", exception);
            return null;
        }
    }

    void serverStarted(MinecraftServer server) {
        if (!CustomersServices.config().economyUseVillagerShopSystem()
                || !CustomersServices.platform().isModLoaded(MOD_ID)
                || !loadApi()) {
            return;
        }
        try {
            if (!(boolean) registryInitialized.invoke(null)) {
                Villager villager = new Villager(EntityType.VILLAGER, server.overworld());
                initializeRegistry.invoke(null, villager);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logFailure("Unable to initialize Village Shop System prices", exception);
        }
    }

    private boolean loadApi() {
        if (lookupAttempted) {
            return apiLoaded;
        }
        lookupAttempted = true;
        try {
            Class<?> calculatorType = Class.forName(CALCULATOR_CLASS);
            Class<?> registryType = Class.forName(REGISTRY_CLASS);
            calculateSellPrice = calculatorType.getMethod("calculateSellPrice", ItemStack.class);
            getSellTradeRatio = calculatorType.getMethod("getSellTradeRatio", Item.class);
            isSellable = calculatorType.getMethod("isSellable", Item.class);
            registryInitialized = registryType.getMethod("isInitialized");
            initializeRegistry = registryType.getMethod("initialize", Entity.class);
            apiLoaded = true;
            return apiLoaded;
        } catch (ReflectiveOperationException | LinkageError exception) {
            logFailure("Village Shop System is loaded but its pricing API is unavailable", exception);
            return false;
        }
    }

    private void logFailure(String message, Throwable exception) {
        if (!failureLogged) {
            failureLogged = true;
            logger.warn(message, exception);
        }
    }
}
