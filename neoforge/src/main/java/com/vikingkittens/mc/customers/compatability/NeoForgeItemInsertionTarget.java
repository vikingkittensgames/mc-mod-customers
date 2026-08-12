package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.IItemHandler;

public final class NeoForgeItemInsertionTarget implements IItemHandler {
    private static final int INPUT_SLOT = 0;
    private final ItemInsertionTarget target;

    public NeoForgeItemInsertionTarget(ItemInsertionTarget target) {
        this.target = target;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : target.insert(stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlot(slot);
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        validateSlot(slot);
        return target.accepts(stack);
    }

    private static void validateSlot(int slot) {
        if (slot != INPUT_SLOT) {
            throw new IndexOutOfBoundsException("Item insertion target slot: " + slot);
        }
    }
}
