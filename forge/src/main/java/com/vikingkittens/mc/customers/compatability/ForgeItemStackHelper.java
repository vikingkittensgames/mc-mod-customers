package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.ItemStack;

public final class ForgeItemStackHelper implements IItemStackHelper {
    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.hasCraftingRemainingItem()
                ? stack.getCraftingRemainingItem()
                : ItemStack.EMPTY;
    }
}
