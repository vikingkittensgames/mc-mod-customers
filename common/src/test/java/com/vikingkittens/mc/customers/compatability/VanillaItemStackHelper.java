package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class VanillaItemStackHelper implements IItemStackHelper {
    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        Item remainder = stack.getItem().getCraftingRemainingItem();
        return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
    }
}
