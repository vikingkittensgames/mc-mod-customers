package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraftforge.items.IItemHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForgeItemInsertionTargetTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesTheCommonInsertionTargetAsAVirtualForgeInputSlot() {
        ItemInsertionTarget target = mock(ItemInsertionTarget.class);
        ItemStack inserted = new ItemStack(Items.EMERALD, 4);
        ItemStack insertRemainder = new ItemStack(Items.EMERALD);
        when(target.insert(inserted, true)).thenReturn(insertRemainder);
        when(target.accepts(inserted)).thenReturn(true);
        IItemHandler adapter = new ForgeItemInsertionTarget(target);

        assertEquals(1, adapter.getSlots());
        assertTrue(adapter.getStackInSlot(0).isEmpty());
        assertSame(insertRemainder, adapter.insertItem(0, inserted, true));
        assertTrue(adapter.extractItem(0, 2, false).isEmpty());
        assertEquals(64, adapter.getSlotLimit(0));
        assertTrue(adapter.isItemValid(0, inserted));
    }

    @Test
    void rejectsAnySlotOtherThanTheSingleVirtualInput() {
        IItemHandler adapter = new ForgeItemInsertionTarget(mock(ItemInsertionTarget.class));

        assertThrows(IndexOutOfBoundsException.class, () -> adapter.getStackInSlot(1));
    }
}
