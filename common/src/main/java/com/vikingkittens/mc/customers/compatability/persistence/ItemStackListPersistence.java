package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

final class ItemStackListPersistence {
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SIZE = "Size";
    private static final String TAG_SLOT = "Slot";

    private ItemStackListPersistence() {
    }

    static NonNullList<ItemStack> read(ValueInput input) {
        int size = Math.max(0, input.getIntOr(TAG_SIZE, 0));
        NonNullList<ItemStack> itemStacks =
                NonNullList.withSize(size, ItemStack.EMPTY);
        for (ValueInput itemInput : input.childrenListOrEmpty(TAG_ITEMS)) {
            int slot = itemInput.getIntOr(TAG_SLOT, -1);
            if (slot >= 0 && slot < itemStacks.size()) {
                itemInput.read(ItemStack.MAP_CODEC)
                        .ifPresent(stack -> itemStacks.set(slot, stack));
            }
        }
        return itemStacks;
    }

    static void write(ValueOutput output, List<ItemStack> itemStacks) {
        output.putInt(TAG_SIZE, itemStacks.size());
        ValueOutput.ValueOutputList itemsOutput =
                output.childrenList(TAG_ITEMS);
        for (int slot = 0; slot < itemStacks.size(); slot++) {
            ItemStack itemStack = itemStacks.get(slot);
            if (!itemStack.isEmpty()) {
                ValueOutput itemOutput = itemsOutput.addChild();
                itemOutput.putInt(TAG_SLOT, slot);
                itemOutput.store(ItemStack.MAP_CODEC, itemStack);
            }
        }
    }
}
