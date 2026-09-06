package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

        ItemStackCUtils.onCraftedBy(stack, player, 3);

        verify(stack).onCraftedBy(player, 3);
    }
    @Test
    void getsCraftingRemainder() {
        ItemStack remainder = ItemStackCUtils.getCraftingRemainder(new ItemStack(Items.MILK_BUCKET));

        assertTrue(remainder.is(Items.BUCKET));
    }
    @Test
    void returnsEmptyStackWithoutCraftingRemainder() {
        assertTrue(ItemStackCUtils.getCraftingRemainder(new ItemStack(Items.STONE)).isEmpty());
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
