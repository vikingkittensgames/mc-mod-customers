package com.vikingkittens.mc.customers.fabric;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.loader.api.FabricLoader;

import com.vikingkittens.mc.customers.compatability.IConfigHelper;
import com.vikingkittens.mc.customers.compatability.IPlatformHelper;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;
import com.vikingkittens.mc.customers.config.CustomersConfig;
import com.vikingkittens.mc.customers.config.CustomersConfigFile;

public final class CustomersFabricCompatibility implements IConfigHelper, IPlatformHelper, IRegistrationHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CustomersConfig CONFIG = loadConfig();

    public static CustomersConfig config() {
        return CONFIG;
    }


    @Override
    public <T> void registerDataPackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec) {
        DynamicRegistries.registerSynced(key, codec);
    }

    @Override public boolean customerSpawnerRecipeEnabled() { return CONFIG.customerSpawnerRecipeEnabled(); }
    @Override public boolean supplierSpawnerRecipeEnabled() { return CONFIG.supplierSpawnerRecipeEnabled(); }
    @Override public boolean customerLeaderboardRecipeEnabled() { return CONFIG.customerLeaderboardRecipeEnabled(); }
    @Override public int maxCounterDistance() { return CONFIG.maxCounterDistance(); }
    @Override public int maxLeaderboardDistance() { return CONFIG.maxLeaderboardDistance(); }
    @Override public int defaultMaxCustomers() { return CONFIG.defaultMaxCustomers(); }
    @Override public int customerGiveUpSeconds() { return CONFIG.customerGiveUpSeconds(); }
    @Override public boolean buildCommandsEnabled() { return CONFIG.buildCommandsEnabled(); }
    @Override public boolean quickSellEnabled() { return CONFIG.quickSellEnabled(); }
    @Override public boolean economyEnabled() { return CONFIG.economyEnabled(); }
    @Override public boolean economyUseVillagerShopSystem() { return CONFIG.economyUseVillagerShopSystem(); }
    @Override public boolean economyUseProjectE() { return CONFIG.economyUseProjectE(); }
    @Override public boolean forceAutoCost() { return CONFIG.forceAutoCost(); }

    private static CustomersConfig loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CustomersConfig.FILE_NAME);
        try {
            return CustomersConfigFile.loadOrCreate(path);
        } catch (IOException exception) {
            LOGGER.error("Unable to read Customers configuration {}; using defaults", path, exception);
            return CustomersConfig.DEFAULT;
        }
    }
}
