package com.vikingkittens.mc.customers.economy;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EconomyItemCostProviderDefault implements EconomyItemCostProvider {
    static final String WARNING_HEADING = "Customers auto cost default used (limit 20)";
    private static final int ITEM_LIMIT = 20;
    private static final long ONE_HOUR_MILLIS = 60L * 60L * 1_000L;

    private final Logger logger;
    private final Set<ResourceLocation> itemIds = new LinkedHashSet<>();
    private long serverStartMillis;
    private long lastFlushedDay;
    private boolean firstFlushPending;

    public EconomyItemCostProviderDefault(Logger logger) {
        this.logger = logger;
    }

    @Override
    public ItemStack calculateItemStackCost(ItemStack item) {
        if (itemIds.size() < ITEM_LIMIT) {
            itemIds.add(EconomyJsonData.itemId(item));
        }
        return new ItemStack(Items.EMERALD, item.getCount());
    }

    void serverStarted(long serverDay, long nowMillis) {
        itemIds.clear();
        lastFlushedDay = serverDay;
        serverStartMillis = nowMillis;
        firstFlushPending = true;
    }

    void tick(long serverDay, long nowMillis) {
        boolean dayElapsed = serverDay != lastFlushedDay;
        boolean restartHourElapsed = firstFlushPending && nowMillis - serverStartMillis >= ONE_HOUR_MILLIS;
        if (!dayElapsed && !restartHourElapsed) {
            return;
        }
        if (!itemIds.isEmpty()) {
            logger.warn("{}: {}", WARNING_HEADING, List.copyOf(itemIds));
            itemIds.clear();
        }
        lastFlushedDay = serverDay;
        firstFlushPending = false;
    }
}
