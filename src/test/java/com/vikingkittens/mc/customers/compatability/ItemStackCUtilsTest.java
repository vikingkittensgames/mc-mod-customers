package com.vikingkittens.mc.customers.compatability;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
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
        ItemStack stack = mock(ItemStack.class);
        ItemStack remainder = mock(ItemStack.class);
        when(stack.getCraftingRemainder()).thenReturn(remainder);

        assertSame(remainder, ItemStackCUtils.getCraftingRemainder(stack));
    }
}
