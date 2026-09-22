package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterInsertionTargetTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void simulatesWantedInsertionWithoutChangingTheCounter() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack offered = new ItemStack(Items.BREAD, 10);
        ItemStack remainder = new ItemStack(Items.BREAD, 3);
        when(counter.previewCraftedStacksConnected(
                null,
                List.of(offered)
        )).thenReturn(List.of(remainder));
        CustomerPickupCounterBlockEntity.InsertionTarget target =
                new CustomerPickupCounterBlockEntity.InsertionTarget(counter);

        ItemStack result = target.insert(offered, true);

        assertEquals(3, result.getCount());
        verify(counter, never())
                .insertCraftedStacksConnectedByOwner(
                        null,
                        List.of(offered)
                );
    }

    @Test
    void insertsWantedItemsAsOwnerlessAutomation() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack offered = new ItemStack(Items.BREAD, 10);
        when(counter.insertCraftedStacksConnectedByOwner(
                null,
                List.of(offered)
        )).thenReturn(List.of(ItemStack.EMPTY));
        CustomerPickupCounterBlockEntity.InsertionTarget target =
                new CustomerPickupCounterBlockEntity.InsertionTarget(counter);

        ItemStack result = target.insert(offered, false);

        assertTrue(result.isEmpty());
        verify(counter).insertCraftedStacksConnectedByOwner(
                null,
                List.of(offered)
        );
    }

    @Test
    void batchesAutomationPreviewAndCommitThroughTheCounter() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        List<ItemStack> offered = List.of(
                new ItemStack(Items.BREAD, 3),
                new ItemStack(Items.COOKIE, 2)
        );
        List<ItemStack> remainders = List.of(
                ItemStack.EMPTY,
                new ItemStack(Items.COOKIE)
        );
        when(counter.previewCraftedStacksConnected(null, offered))
                .thenReturn(remainders);
        CustomerPickupCounterBlockEntity.InsertionTarget target =
                new CustomerPickupCounterBlockEntity.InsertionTarget(counter);

        List<ItemStack> result = target.insertAll(offered, true);

        assertEquals(remainders, result);
        verify(counter).previewCraftedStacksConnected(null, offered);
        verify(counter, never())
                .insertCraftedStacksConnectedByOwner(null, offered);
    }

    @Test
    void returnsUnwantedItemsUnchanged() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemStack offered = new ItemStack(Items.IRON_INGOT, 4);
        when(counter.insertCraftedStacksConnectedByOwner(
                null,
                List.of(offered)
        )).thenReturn(List.of(offered));
        CustomerPickupCounterBlockEntity.InsertionTarget target =
                new CustomerPickupCounterBlockEntity.InsertionTarget(counter);

        ItemStack result = target.insert(offered, false);

        assertEquals(4, result.getCount());
        assertTrue(result.is(Items.IRON_INGOT));
    }

}
