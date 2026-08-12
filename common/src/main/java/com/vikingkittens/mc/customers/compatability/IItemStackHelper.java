package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.ItemStack;

public interface IItemStackHelper {
    ItemStack getCraftingRemainder(ItemStack stack);
}
