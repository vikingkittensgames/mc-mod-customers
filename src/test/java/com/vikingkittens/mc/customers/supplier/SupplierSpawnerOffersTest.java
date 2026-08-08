package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SupplierSpawnerOffersTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void usesEachExplicitPairAsTheOfferItemAndCost() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(Items.BEEF, 32));
        inventory.setStackInSlot(1, new ItemStack(Items.DIAMOND, 2));
        inventory.setStackInSlot(2, new ItemStack(Items.CARROT, 16));
        inventory.setStackInSlot(3, new ItemStack(Items.GOLD_INGOT, 4));

        MerchantOffers offers = SupplierSpawnerBlockEntity
                .getOffersFromInventory(RandomSource.create(), inventory);

        assertEquals(2, offers.size());
        assertSame(Items.DIAMOND, offers.get(0).getCostA().getItem());
        assertEquals(2, offers.get(0).getCostA().getCount());
        assertSame(Items.BEEF, offers.get(0).getResult().getItem());
        assertEquals(32, offers.get(0).getResult().getCount());
        assertSame(Items.GOLD_INGOT, offers.get(1).getCostA().getItem());
        assertEquals(4, offers.get(1).getCostA().getCount());
        assertSame(Items.CARROT, offers.get(1).getResult().getItem());
        assertEquals(16, offers.get(1).getResult().getCount());
    }

    @Test
    void ignoresIncompletePairsAndTheHiddenNinthColumn() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(Items.BEEF));
        inventory.setStackInSlot(3, new ItemStack(Items.EMERALD));
        inventory.setStackInSlot(8, new ItemStack(Items.DIAMOND));

        MerchantOffers offers = SupplierSpawnerBlockEntity
                .getOffersFromInventory(RandomSource.create(), inventory);

        assertEquals(0, offers.size());
    }
}
