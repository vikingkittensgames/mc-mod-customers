package com.vikingkittens.mc.customers.compatability;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class NeoForgeItemInsertionTarget implements ResourceHandler<ItemResource> {
    private static final int INPUT_SLOT = 0;
    private final ItemInsertionTarget target;
    private final PendingInsertions pendingInsertions =
            new PendingInsertions();

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
    public int insert(
            int slot,
            ItemResource resource,
            int amount,
            TransactionContext transaction
    ) {
        validateSlot(slot);
        if (amount <= 0 || resource.isEmpty() || !target.accepts(resource.toStack())) {
            return 0;
        }
        ItemStack remainder = target.insert(resource.toStack(amount), true);
        int inserted = amount - remainder.getCount();
        if (inserted > 0) {
            pendingInsertions.add(resource.toStack(inserted), transaction);
        }
        return inserted;
    }

    @Override
    public int extract(
            int slot,
            ItemResource resource,
            int amount,
            TransactionContext transaction
    ) {
        validateSlot(slot);
        return 0;
    }

    private static void validateSlot(int slot) {
        if (slot != INPUT_SLOT) {
            throw new IndexOutOfBoundsException("Item insertion target slot: " + slot);
        }
    }

    private final class PendingInsertions
            extends SnapshotJournal<List<ItemStack>> {
        private final List<ItemStack> insertions = new ArrayList<>();

        private void add(
                ItemStack stack,
                TransactionContext transaction
        ) {
            updateSnapshots(transaction);
            insertions.add(stack);
        }

        @Override
        protected List<ItemStack> createSnapshot() {
            return List.copyOf(insertions);
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            insertions.clear();
            insertions.addAll(snapshot);
        }

        @Override
        protected void onRootCommit(List<ItemStack> originalState) {
            List<ItemStack> committed = List.copyOf(insertions);
            insertions.clear();
            committed.forEach(stack -> target.insert(stack, false));
        }
    }
}
