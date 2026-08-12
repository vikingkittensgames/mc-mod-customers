package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class ItemStackCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    void appliesCraftedItemBehavior() {
        ItemStack stack = mock(ItemStack.class);
        Player player = mock(Player.class);
        Level level = mock(Level.class);
        when(player.level()).thenReturn(level);

        ItemStackCUtils.onCraftedBy(stack, player, 3);

        verify(stack).onCraftedBy(level, player, 3);
    }
    @Test
    void getsCraftingRemainder() {
        ItemStack stack = mock(ItemStack.class);
        ItemStack remainder = mock(ItemStack.class);
        when(stack.hasCraftingRemainingItem()).thenReturn(true);
        when(stack.getCraftingRemainingItem()).thenReturn(remainder);

        assertSame(remainder, ItemStackCUtils.getCraftingRemainder(stack));
    }
    @Test
    void returnsEmptyStackWithoutCraftingRemainder() {
        ItemStack stack = mock(ItemStack.class);

        assertSame(ItemStack.EMPTY, ItemStackCUtils.getCraftingRemainder(stack));
    }
    /** Preserves stack components when creating an offer cost. */
    @Test
    void createsItemCostWithStackComponents() {
        ItemStack water =
                PotionContents.createItemStack(Items.POTION, Potions.WATER);
        ItemStack awkward =
                PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);

        ItemCost cost = ItemStackCUtils.createItemCost(water, 1);

        assertTrue(cost.test(water));
        assertFalse(cost.test(awkward));
    }
}
