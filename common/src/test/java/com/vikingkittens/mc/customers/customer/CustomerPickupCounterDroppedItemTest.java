package com.vikingkittens.mc.customers.customer;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterDroppedItemTest {
    @Test
    void itemCollectionRunsImmediatelyThenEveryEightTicks() {
        assertTrue(CustomerPickupCounterBlockEntity
                .shouldCollectItems(100L, Long.MIN_VALUE));
        assertFalse(CustomerPickupCounterBlockEntity
                .shouldCollectItems(107L, 100L));
        assertTrue(CustomerPickupCounterBlockEntity
                .shouldCollectItems(108L, 100L));
    }

    @Test
    void usesTheDroppingPlayerAsTheStackOwner() {
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUUID()).thenReturn(playerId);

        assertEquals(
                playerId,
                CustomerPickupCounterBlockEntity
                        .getDroppingPlayerId(player)
        );
    }

    @Test
    void treatsNonPlayerAndMissingOwnersAsAutomation() {
        assertNull(CustomerPickupCounterBlockEntity
                .getDroppingPlayerId(mock(Entity.class)));
        assertNull(CustomerPickupCounterBlockEntity
                .getDroppingPlayerId(null));
    }

    @Test
    void removesAConsumedDroppedStack() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemEntity droppedItem = mock(ItemEntity.class);
        ItemStack offered = new ItemStack(Items.BREAD, 6);
        when(droppedItem.getItem()).thenReturn(offered);
        when(counter.insertCraftedStackConnectedByOwner(null, offered))
                .thenReturn(ItemStack.EMPTY);

        CustomerPickupCounterBlockEntity.acceptDroppedItem(
                counter,
                droppedItem
        );

        verify(droppedItem).discard();
    }

    @Test
    void leavesTheUnacceptedRemainderInTheWorld() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemEntity droppedItem = mock(ItemEntity.class);
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 10);
        ItemStack remainder = new ItemStack(Items.BREAD, 4);
        when(player.getUUID()).thenReturn(playerId);
        when(droppedItem.getOwner()).thenReturn(player);
        when(droppedItem.getItem()).thenReturn(offered);
        when(counter.insertCraftedStackConnectedByOwner(
                playerId,
                offered
        )).thenReturn(remainder);

        CustomerPickupCounterBlockEntity.acceptDroppedItem(
                counter,
                droppedItem
        );

        verify(droppedItem).setItem(remainder);
        assertEquals(4, remainder.getCount());
    }

    @Test
    void doesNotReplaceACompletelyUnacceptedStack() {
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        ItemEntity droppedItem = mock(ItemEntity.class);
        ItemStack offered = new ItemStack(Items.IRON_INGOT, 3);
        when(droppedItem.getItem()).thenReturn(offered);
        when(counter.insertCraftedStackConnectedByOwner(null, offered))
                .thenReturn(offered);

        CustomerPickupCounterBlockEntity.acceptDroppedItem(
                counter,
                droppedItem
        );

        verify(droppedItem, org.mockito.Mockito.never())
                .setItem(org.mockito.ArgumentMatchers.any());
        assertTrue(droppedItem.getItem().is(Items.IRON_INGOT));
    }
}
