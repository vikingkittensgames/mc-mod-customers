package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStackCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void comparesItemsAndTheirComponents() {
        ItemStack water = potion(Potions.WATER);
        ItemStack awkward = potion(Potions.AWKWARD);

        assertTrue(ItemStackCUtils.isSameItemAndTags(water, water.copy()));
        assertFalse(ItemStackCUtils.isSameItemAndTags(water, awkward));
    }

    @Test
    void matchesComponentAwareOfferCosts() {
        ItemStack water = potion(Potions.WATER);
        ItemStack awkward = potion(Potions.AWKWARD);
        ItemStack twoWaterPotions = water.copy();
        twoWaterPotions.setCount(2);
        ItemStack oneWaterPotion = water.copy();

        assertTrue(ItemStackCUtils.matchesCost(ItemStackCUtils.createItemCost(water, 2), twoWaterPotions));
        assertTrue(ItemStackCUtils.matchesCost(ItemStackCUtils.createItemCost(water, 2), oneWaterPotion));
        assertFalse(ItemStackCUtils.matchesCost(ItemStackCUtils.createItemCost(water, 2), awkward));
    }

    private static ItemStack potion(net.minecraft.world.item.alchemy.Potion potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        PotionUtils.setPotion(stack, potion);
        return stack;
    }
}
