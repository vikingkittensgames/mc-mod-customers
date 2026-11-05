package com.vikingkittens.mc.customers.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

public final class SpawnerInventoryLocks {
    private static final String TAG_INVENTORY_LOCKED = "inventoryLocked";
    private static final String TAG_LOCKED_INVENTORY = "lockedInventory";
    private final boolean[] locked;

    public SpawnerInventoryLocks(int size) {
        locked = new boolean[size];
    }

    public boolean isLocked(int slot) {
        return slot >= 0 && slot < locked.length && locked[slot];
    }

    public void setLocked(int slot, boolean value) {
        if (slot >= 0 && slot < locked.length) {
            locked[slot] = value;
        }
    }

    public void write(DataWriter output) {
        List<String> values = new ArrayList<>(locked.length);
        for (boolean value : locked) {
            values.add(Boolean.toString(value));
        }
        output.putStrings(TAG_INVENTORY_LOCKED, values);
    }

    public void read(DataReader input) {
        java.util.Arrays.fill(locked, false);
        List<String> values = input.getStrings(TAG_INVENTORY_LOCKED);
        for (int slot = 0; slot < Math.min(locked.length, values.size()); slot++) {
            locked[slot] = Boolean.parseBoolean(values.get(slot));
        }
    }

    public void writeLockedInventory(DataWriter output, Container inventory) {
        List<ItemStack> stacks = new ArrayList<>(locked.length);
        for (int slot = 0; slot < locked.length; slot++) {
            stacks.add(isLocked(slot) ? inventory.getItem(slot).copy() : ItemStack.EMPTY);
        }
        output.putItemStacks(TAG_LOCKED_INVENTORY, stacks);
    }

    public void readLockedInventory(DataReader input, Container inventory) {
        List<ItemStack> stacks = input.getItemStacks(TAG_LOCKED_INVENTORY, locked.length);
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                inventory.setItem(slot, stacks.get(slot).copy());
                setLocked(slot, true);
            }
        }
    }

    public void dropUnlocked(Level level, BlockPos pos, Container inventory) {
        for (int slot = 0; slot < locked.length; slot++) {
            if (!isLocked(slot)) {
                ItemStack stack = inventory.removeItemNoUpdate(slot);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                }
            }
        }
    }
}
