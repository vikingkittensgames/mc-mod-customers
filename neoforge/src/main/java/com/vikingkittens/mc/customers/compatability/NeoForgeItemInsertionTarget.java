package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class NeoForgeItemInsertionTarget implements ResourceHandler<ItemResource> {
    private static final int INPUT_SLOT = 0;
    private final ItemInsertionTarget target;

    public NeoForgeItemInsertionTarget(ItemInsertionTarget target) {
        this.target = target;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemResource getResource(int slot) {
        validateSlot(slot);
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int slot) {
        validateSlot(slot);
        return 0;
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        validateSlot(slot);
        return 64;
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        validateSlot(slot);
        return !resource.isEmpty() && target.accepts(resource.toStack());
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        validateSlot(slot);
        if (amount <= 0 || resource.isEmpty() || !target.accepts(resource.toStack())) {
            return 0;
        }
        ItemStack remainder = target.insert(resource.toStack(amount), false);
        return amount - remainder.getCount();
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        validateSlot(slot);
        return 0;
    }

    private static void validateSlot(int slot) {
        if (slot != INPUT_SLOT) {
            throw new IndexOutOfBoundsException("Item insertion target slot: " + slot);
        }
    }
}
