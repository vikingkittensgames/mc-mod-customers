package com.vikingkittens.mc.customers.compatability;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public interface ItemInsertionTarget {
    ItemStack insert(ItemStack stack, boolean simulate);

    default List<ItemStack> insertAll(
            List<ItemStack> stacks,
            boolean simulate
    ) {
        return stacks.stream()
                .map(stack -> insert(stack, simulate))
                .toList();
    }

    boolean accepts(ItemStack stack);
}
