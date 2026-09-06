package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeItemStackHelperTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void returnsLoaderCraftingRemainder() {
        ItemStack remainder = new NeoForgeItemStackHelper().getCraftingRemainder(new ItemStack(Items.MILK_BUCKET));

        assertTrue(remainder.is(Items.BUCKET));
    }

    @Test
    void returnsEmptyStackWithoutCraftingRemainder() {
        assertTrue(new NeoForgeItemStackHelper().getCraftingRemainder(new ItemStack(Items.STONE)).isEmpty());
    }
}
