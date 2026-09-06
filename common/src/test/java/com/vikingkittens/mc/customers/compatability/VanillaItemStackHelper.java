package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class VanillaItemStackHelper implements IItemStackHelper {
    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        ItemStack remainder = stack.getItem().getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.copy();
    }
}
