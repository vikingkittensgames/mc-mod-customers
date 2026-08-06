package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

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
        stack.onCraftedBy(player, count);
    }
    public static ItemStack getCraftingRemainder(ItemStack stack) {
        return stack.getCraftingRemainder();
    }
    /**
     * Creates an offer cost retaining the supplied stack's data components.
     *
     * @param stack stack defining the item and required components
     * @param count required item count
     * @return component-aware offer cost
     */
    public static ItemCost createItemCost(ItemStack stack, int count) {
        return new ItemCost(
                stack.getItem().builtInRegistryHolder(),
                count,
                DataComponentExactPredicate.allOf(stack.getComponents())
        );
    }
}
