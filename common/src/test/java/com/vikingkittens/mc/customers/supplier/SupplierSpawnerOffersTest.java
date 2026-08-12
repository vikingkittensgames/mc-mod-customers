package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SupplierSpawnerOffersTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void usesEachExplicitPairAsTheOfferItemAndCost() {
        PersistedContainer inventory = new PersistedContainer(9, () -> {});
        inventory.setItem(0, new ItemStack(Items.BEEF, 32));
        inventory.setItem(1, new ItemStack(Items.DIAMOND, 2));
        inventory.setItem(2, new ItemStack(Items.CARROT, 16));
        inventory.setItem(3, new ItemStack(Items.GOLD_INGOT, 4));

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
        PersistedContainer inventory = new PersistedContainer(9, () -> {});
        inventory.setItem(0, new ItemStack(Items.BEEF));
        inventory.setItem(3, new ItemStack(Items.EMERALD));
        inventory.setItem(8, new ItemStack(Items.DIAMOND));

        MerchantOffers offers = SupplierSpawnerBlockEntity
                .getOffersFromInventory(RandomSource.create(), inventory);

        assertEquals(0, offers.size());
    }
}
