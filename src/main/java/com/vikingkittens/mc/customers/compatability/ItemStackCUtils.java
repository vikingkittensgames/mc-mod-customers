package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Provides version-compatible item stack lifecycle operations.
 */
public final class ItemStackCUtils {
    private ItemStackCUtils() {
    }
    public static void onCraftedBy(
            ItemStack stack,
            Player player,
            int count
    ) {
        stack.onCraftedBy(player.level(), player, count);
    }
    public static ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.hasCraftingRemainingItem()
                ? stack.getCraftingRemainingItem()
                : ItemStack.EMPTY;
    }
}
