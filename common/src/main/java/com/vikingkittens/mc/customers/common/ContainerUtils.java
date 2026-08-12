package com.vikingkittens.mc.customers.common;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class ContainerUtils {
    private ContainerUtils() {}

    public static boolean tryInsertStacked(Container container, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (getAvailableCapacity(container, stack) < stack.getCount()) {
            return false;
        }

        int remaining = stack.getCount();
        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack) &&
                    container.canPlaceItem(slot, stack)) {
                int inserted = Math.min(remaining, Math.max(0, container.getMaxStackSize(existing) - existing.getCount()));
                if (inserted > 0) {
                    ItemStack combined = existing.copy();
                    combined.grow(inserted);
                    container.setItem(slot, combined);
                    remaining -= inserted;
                }
            }
        }
        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            if (container.getItem(slot).isEmpty() && container.canPlaceItem(slot, stack)) {
                int inserted = Math.min(remaining, container.getMaxStackSize(stack));
                container.setItem(slot, stack.copyWithCount(inserted));
                remaining -= inserted;
            }
        }
        return remaining == 0;
    }

    private static int getAvailableCapacity(Container container, ItemStack stack) {
        int capacity = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.canPlaceItem(slot, stack)) {
                continue;
            }
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty()) {
                capacity += container.getMaxStackSize(stack);
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                capacity += Math.max(0, container.getMaxStackSize(existing) - existing.getCount());
            }
            if (capacity >= stack.getCount()) {
                return capacity;
            }
        }
        return capacity;
    }
}
