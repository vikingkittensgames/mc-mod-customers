package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterItemHandlerTest {
    @Test
    void simulatesWantedInsertionWithoutChangingTheCounter() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack offered = new ItemStack(Items.BREAD, 10);
        ItemStack remainder = new ItemStack(Items.BREAD, 3);
        when(counter.previewCraftedStackConnected(null, offered))
                .thenReturn(remainder);
        CustomerPickupCounterBlockEntity.ItemHandler handler =
                new CustomerPickupCounterBlockEntity.ItemHandler(counter);

        ItemStack result = handler.insertItem(0, offered, true);

        assertEquals(3, result.getCount());
        verify(counter, never())
                .insertCraftedStackConnectedByOwner(null, offered);
    }

    @Test
    void insertsWantedItemsAsOwnerlessAutomation() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack offered = new ItemStack(Items.BREAD, 10);
        when(counter.insertCraftedStackConnectedByOwner(null, offered))
                .thenReturn(ItemStack.EMPTY);
        CustomerPickupCounterBlockEntity.ItemHandler handler =
                new CustomerPickupCounterBlockEntity.ItemHandler(counter);

        ItemStack result = handler.insertItem(0, offered, false);

        assertTrue(result.isEmpty());
        verify(counter)
                .insertCraftedStackConnectedByOwner(null, offered);
    }

    @Test
    void returnsUnwantedItemsUnchanged() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack offered = new ItemStack(Items.IRON_INGOT, 4);
        when(counter.insertCraftedStackConnectedByOwner(null, offered))
                .thenReturn(offered);
        CustomerPickupCounterBlockEntity.ItemHandler handler =
                new CustomerPickupCounterBlockEntity.ItemHandler(counter);

        ItemStack result = handler.insertItem(0, offered, false);

        assertEquals(4, result.getCount());
        assertTrue(result.is(Items.IRON_INGOT));
    }

    @Test
    void doesNotExposePickupItemsForAutomationExtraction() {
        CustomerPickupCounterBlockEntity.ItemHandler handler =
                new CustomerPickupCounterBlockEntity.ItemHandler(
                        mock(CustomerPickupCounterBlockEntity.class)
                );

        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.extractItem(0, 64, false).isEmpty());
    }
}
