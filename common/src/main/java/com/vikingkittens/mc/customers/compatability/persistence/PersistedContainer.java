package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;

public class PersistedContainer implements Container {
    private final Runnable changeListener;
    private final Predicate<Player> validity;
    private NonNullList<ItemStack> items;

    public PersistedContainer(int size, Runnable changeListener) {
        this(size, changeListener, player -> true);
    }

    public PersistedContainer(int size, Runnable changeListener, Predicate<Player> validity) {
        items = NonNullList.withSize(size, ItemStack.EMPTY);
        this.changeListener = changeListener;
        this.validity = validity;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = getItem(slot);
        if (!existing.isEmpty() && !ItemStackCUtils.isSameItemAndTags(existing, stack)) {
            return stack;
        }

        int available = Math.min(getMaxStackSize(stack), stack.getMaxStackSize()) - existing.getCount();
        if (available <= 0) {
            return stack;
        }

        int inserted = Math.min(available, stack.getCount());
        if (!simulate) {
            if (existing.isEmpty()) {
                setItem(slot, stack.copyWithCount(inserted));
            } else {
                ItemStack combined = existing.copy();
                combined.grow(inserted);
                setItem(slot, combined);
            }
        }
        return inserted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
    }

    @Override
    public void setChanged() {
        changeListener.run();
    }

    @Override
    public boolean stillValid(Player player) {
        return validity.test(player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag wrapper = new CompoundTag();
        PersistenceCUtils.writer(wrapper, registries)
                .putItemStacks("inventory", items);
        return wrapper.getCompoundOrEmpty("inventory");
    }

    public void write(ValueOutput output) {
        ItemStackListPersistence.write(output, items);
    }

    public void read(ValueInput input) {
        items = ItemStackListPersistence.read(input);
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag serialized) {
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("inventory", serialized);
        List<ItemStack> loadedItems = PersistenceCUtils.reader(wrapper, registries)
                .getItemStacks("inventory");
        items = NonNullList.withSize(loadedItems.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < loadedItems.size(); slot++) {
            items.set(slot, loadedItems.get(slot));
        }
    }
}
