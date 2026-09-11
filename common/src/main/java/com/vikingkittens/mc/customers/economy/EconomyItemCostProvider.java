package com.vikingkittens.mc.customers.economy;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public interface EconomyItemCostProvider {
    @Nullable
    ItemStack calculateItemStackCost(ItemStack item);
}
