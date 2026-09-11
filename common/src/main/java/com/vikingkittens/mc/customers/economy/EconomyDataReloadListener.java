package com.vikingkittens.mc.customers.economy;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class EconomyDataReloadListener extends SimplePreparableReloadListener<EconomyData> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path ECONOMY_DIRECTORY = Path.of("config", "customers", "economy");

    @Override
    protected EconomyData prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return new EconomyData(
                EconomyJsonData.load(
                        resourceManager,
                        "customers/economy/items",
                        List.of(ECONOMY_DIRECTORY.resolve("items")),
                        LOGGER
                ),
                EconomyJsonData.load(
                        resourceManager,
                        "customers/economy/conversions",
                        List.of(
                                ECONOMY_DIRECTORY.resolve("conversions.json"),
                                ECONOMY_DIRECTORY.resolve("conversions")
                        ),
                        LOGGER
                )
        );
    }

    @Override
    protected void apply(EconomyData data, ResourceManager resourceManager, ProfilerFiller profiler) {
        Economy.applyData(data);
    }
}
