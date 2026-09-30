package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        when(target.insert(any(ItemStack.class), eq(true)))
                .thenReturn(insertRemainder);
        when(target.insert(any(ItemStack.class), eq(false)))
                .thenReturn(ItemStack.EMPTY);
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
            transaction.commit();
        }

        ArgumentCaptor<ItemStack> committed =
                ArgumentCaptor.forClass(ItemStack.class);
        verify(target).insert(committed.capture(), eq(false));
        assertTrue(committed.getValue().is(Items.EMERALD));
        assertEquals(3, committed.getValue().getCount());
    }

    @Test
    void doesNotInsertWhenTheNeoForgeTransactionIsAborted() {
        ItemInsertionTarget target = mock(ItemInsertionTarget.class);
        when(target.accepts(any(ItemStack.class))).thenReturn(true);
        when(target.insert(any(ItemStack.class), eq(true)))
                .thenReturn(ItemStack.EMPTY);
        ResourceHandler<ItemResource> adapter =
                new NeoForgeItemInsertionTarget(target);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(
                    4,
                    adapter.insert(
                            0,
                            ItemResource.of(Items.EMERALD),
                            4,
                            transaction
                    )
            );
        }

        verify(target, never()).insert(any(ItemStack.class), eq(false));
    }

    @Test
    void rejectsAnySlotOtherThanTheSingleVirtualInput() {
        ResourceHandler<ItemResource> adapter =
                new NeoForgeItemInsertionTarget(mock(ItemInsertionTarget.class));

        org.junit.jupiter.api.Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> adapter.getResource(1)
        );
    }
}
