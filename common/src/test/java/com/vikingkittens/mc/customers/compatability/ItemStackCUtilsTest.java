package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
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
        ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);

        assertTrue(ItemStackCUtils.isSameItemAndTags(water, water.copy()));
        assertFalse(ItemStackCUtils.isSameItemAndTags(water, awkward));
    }

    @Test
    void matchesComponentAwareOfferCosts() {
        ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);

        assertTrue(ItemStackCUtils.matchesCost(ItemStackCUtils.createItemCost(water, 2), water.copyWithCount(2)));
        assertFalse(ItemStackCUtils.matchesCost(ItemStackCUtils.createItemCost(water, 2), awkward));
    }
}
