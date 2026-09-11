package com.vikingkittens.mc.customers.economy;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public final class EconomyCost {
    private EconomyCost() {}

    public static ItemStack scale(ItemStack fullItemStack, int offeredItemCount, @Nullable ItemStack fullCost) {
        if (fullItemStack.isEmpty() || fullCost == null || fullCost.isEmpty() || offeredItemCount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack scaled = fullCost.copy();
        long proportionalCount = (long) fullCost.getCount() * offeredItemCount / fullItemStack.getCount();
        scaled.setCount(Economy.safeCount(proportionalCount));
        return scaled;
    }
}
