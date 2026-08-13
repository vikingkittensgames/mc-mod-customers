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
        return CustomersServices.itemStacks().getCraftingRemainder(stack);
    }

    public static boolean isSameItemAndTags(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameTags(first, second);
    }

    public static boolean matchesCost(ItemStack cost, ItemStack stack) {
        return !cost.isEmpty()
                && stack.getCount() >= cost.getCount()
                && isSameItemAndTags(cost, stack);
    }
    /**
     * Creates an offer cost retaining the supplied stack's data components.
     *
     * @param stack stack defining the item and required components
     * @param count required item count
     * @return component-aware offer cost
     */
    public static ItemStack createItemCost(ItemStack stack, int count) {
        ItemStack cost = stack.copy();
        cost.setCount(count);
        return cost;
    }
}
