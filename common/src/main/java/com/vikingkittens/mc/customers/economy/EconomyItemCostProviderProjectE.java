package com.vikingkittens.mc.customers.economy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class EconomyItemCostProviderProjectE implements EconomyItemCostProvider {
    static final String MOD_ID = "projecte";
    private static final String EMC_PROXY_CLASS = "moze_intel.projecte.api.proxy.IEMCProxy";
    private static final int WHEAT_PER_EMERALD = 20;

    private final Logger logger;
    private boolean lookupAttempted;
    private boolean failureLogged;
    private Object emcProxy;
    private Method getValue;

    public EconomyItemCostProviderProjectE(Logger logger) {
        this.logger = logger;
    }

    @Nullable
    @Override
    public ItemStack calculateItemStackCost(ItemStack item) {
        if (!CustomersServices.config().economyUseProjectE()
                || !CustomersServices.platform().isModLoaded(MOD_ID)
                || !loadApi()) {
            return null;
        }
        try {
            long itemEmc = (long) getValue.invoke(emcProxy, item);
            long wheatEmc = (long) getValue.invoke(emcProxy, Items.WHEAT.getDefaultInstance());
            if (itemEmc <= 0 || wheatEmc <= 0) {
                return null;
            }
            BigInteger numerator = BigInteger.valueOf(itemEmc).multiply(BigInteger.valueOf(item.getCount()));
            BigInteger denominator = BigInteger.valueOf(wheatEmc).multiply(BigInteger.valueOf(WHEAT_PER_EMERALD));
            BigInteger emeralds = numerator.add(denominator).subtract(BigInteger.ONE).divide(denominator);
            return new ItemStack(Items.EMERALD, Economy.safeCount(emeralds));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logFailure("Unable to calculate a cost with ProjectE", exception);
            return null;
        }
    }

    private boolean loadApi() {
        if (lookupAttempted) {
            return emcProxy != null;
        }
        lookupAttempted = true;
        try {
            Class<?> proxyType = Class.forName(EMC_PROXY_CLASS);
            Field instance = proxyType.getField("INSTANCE");
            emcProxy = instance.get(null);
            getValue = proxyType.getMethod("getValue", ItemStack.class);
            return true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            logFailure("ProjectE is loaded but its EMC API is unavailable", exception);
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
