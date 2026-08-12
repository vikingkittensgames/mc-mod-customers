package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.ItemStack;

public interface ItemInsertionTarget {
    ItemStack insert(ItemStack stack, boolean simulate);

    boolean accepts(ItemStack stack);
}
