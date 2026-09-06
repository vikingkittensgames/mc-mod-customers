package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NeoForgeItemInsertionTargetTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesTheCommonInsertionTargetAsAVirtualNeoForgeInputSlot() {
        ItemInsertionTarget target = mock(ItemInsertionTarget.class);
        ItemStack inserted = new ItemStack(Items.EMERALD, 4);
        ItemStack insertRemainder = new ItemStack(Items.EMERALD);
        when(target.insert(any(ItemStack.class), eq(false))).thenReturn(insertRemainder);
        when(target.accepts(any(ItemStack.class))).thenReturn(true);
        ResourceHandler<ItemResource> adapter = new NeoForgeItemInsertionTarget(target);
        ItemResource resource = ItemResource.of(inserted);

        assertEquals(1, adapter.size());
        assertTrue(adapter.getResource(0).isEmpty());
        assertEquals(64, adapter.getCapacityAsLong(0, resource));
        assertTrue(adapter.isValid(0, resource));
        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(3, adapter.insert(0, resource, 4, transaction));
            assertEquals(0, adapter.extract(0, resource, 2, transaction));
        }
    }

    @Test
    void rejectsAnySlotOtherThanTheSingleVirtualInput() {
        ResourceHandler<ItemResource> adapter = new NeoForgeItemInsertionTarget(mock(ItemInsertionTarget.class));

        org.junit.jupiter.api.Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> adapter.getResource(1)
        );
    }
}
