package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NeoForgeItemStackHelperTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void returnsLoaderCraftingRemainder() {
        ItemStack stack = mock(ItemStack.class);
        ItemStack remainder = mock(ItemStack.class);
        when(stack.hasCraftingRemainingItem()).thenReturn(true);
        when(stack.getCraftingRemainingItem()).thenReturn(remainder);

        assertSame(remainder, new NeoForgeItemStackHelper().getCraftingRemainder(stack));
    }

    @Test
    void returnsEmptyStackWithoutCraftingRemainder() {
        assertSame(ItemStack.EMPTY, new NeoForgeItemStackHelper().getCraftingRemainder(mock(ItemStack.class)));
    }
}
