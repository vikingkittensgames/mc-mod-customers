package com.vikingkittens.mc.customers.customer;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void savesAndLoadsItsFifoInventory() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity source =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        source.insertStack(new ItemStack(Items.APPLE));
        source.insertStack(new ItemStack(Items.BREAD));
        CompoundTag tag = new CompoundTag();

        source.saveAdditional(tag, RegistryAccess.EMPTY);

        CustomerPickupCounterBlockEntity restored =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        restored.loadAdditional(tag, RegistryAccess.EMPTY);

        assertTrue(restored.removeOldest().is(Items.APPLE));
        assertTrue(restored.removeOldest().is(Items.BREAD));
    }

    @Test
    void synchronizesInventoryChangesToClients() {
        Level level = mock(Level.class);
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        counter.setLevel(level);

        counter.insertStack(new ItemStack(Items.APPLE));

        verify(level).sendBlockUpdated(
                BlockPos.ZERO,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
        assertNotNull(counter.getUpdatePacket());
        assertTrue(
                counter.getUpdateTag(RegistryAccess.EMPTY)
                        .contains("inventory")
        );
    }
    @Test
    void removesItemsInInsertionOrder() {
        ItemStackHandler inventory = new ItemStackHandler(9);

        ItemStack breadRemainder =
                CustomerPickupCounterBlockEntity.insertStack(
                        inventory,
                        new ItemStack(Items.BREAD, 25)
                );
        ItemStack appleRemainder =
                CustomerPickupCounterBlockEntity.insertStack(
                        inventory,
                        new ItemStack(Items.APPLE)
                );

        assertTrue(breadRemainder.isEmpty());
        assertTrue(appleRemainder.isEmpty());
        ItemStack removed =
                CustomerPickupCounterBlockEntity.removeOldest(inventory);
        assertTrue(removed.is(Items.BREAD));
        assertEquals(25, removed.getCount());
        assertTrue(
                CustomerPickupCounterBlockEntity.removeOldest(inventory)
                        .is(Items.APPLE)
        );
        assertTrue(
                CustomerPickupCounterBlockEntity.removeOldest(inventory)
                        .isEmpty()
        );
    }

    @Test
    void shiftsRemainingItemsTowardTheFront() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        CustomerPickupCounterBlockEntity.insertStack(
                inventory,
                new ItemStack(Items.APPLE)
        );
        CustomerPickupCounterBlockEntity.insertStack(
                inventory,
                new ItemStack(Items.BREAD)
        );
        CustomerPickupCounterBlockEntity.insertStack(
                inventory,
                new ItemStack(Items.CARROT)
        );

        CustomerPickupCounterBlockEntity.removeOldest(inventory);

        assertTrue(inventory.getStackInSlot(0).is(Items.BREAD));
        assertTrue(inventory.getStackInSlot(1).is(Items.CARROT));
        assertTrue(inventory.getStackInSlot(2).isEmpty());
    }

    @Test
    void holdsAtMostNineItemStacks() {
        ItemStackHandler inventory = new ItemStackHandler(9);

        for (int index = 0; index < 9; index++) {
            assertTrue(
                    CustomerPickupCounterBlockEntity.insertStack(
                            inventory,
                            new ItemStack(Items.CARROT)
                    ).isEmpty()
            );
        }

        ItemStack rejected =
                CustomerPickupCounterBlockEntity.insertStack(
                        inventory,
                        new ItemStack(Items.APPLE)
                );

        assertEquals(
                9,
                CustomerPickupCounterBlockEntity
                        .getDisplayItems(inventory)
                        .size()
        );
        assertTrue(rejected.is(Items.APPLE));
        assertEquals(1, rejected.getCount());
    }

    @Test
    void exposesDisplayItemsInFifoOrder() {
        ItemStackHandler inventory = new ItemStackHandler(9);
        CustomerPickupCounterBlockEntity.insertStack(
                inventory,
                new ItemStack(Items.APPLE)
        );
        CustomerPickupCounterBlockEntity.insertStack(
                inventory,
                new ItemStack(Items.BREAD)
        );

        assertTrue(
                CustomerPickupCounterBlockEntity
                        .getDisplayItems(inventory)
                        .get(0)
                        .is(Items.APPLE)
        );
        assertTrue(
                CustomerPickupCounterBlockEntity
                        .getDisplayItems(inventory)
                        .get(1)
                        .is(Items.BREAD)
        );
    }

    @Test
    void insertsIntoTheFirstAvailableConnectedCounter() {
        Level level = mock(Level.class);
        BlockPos firstPos = BlockPos.ZERO;
        BlockPos secondPos = firstPos.east();
        BlockPos thirdPos = secondPos.east();
        CustomerPickupCounterBlockEntity first =
                mock(CustomerPickupCounterBlockEntity.class);
        CustomerPickupCounterBlockEntity second =
                mock(CustomerPickupCounterBlockEntity.class);
        CustomerPickupCounterBlockEntity third =
                mock(CustomerPickupCounterBlockEntity.class);
        Map<BlockPos, CustomerPickupCounterBlockEntity> counters = Map.of(
                firstPos,
                first,
                secondPos,
                second,
                thirdPos,
                third
        );
        when(level.getBlockEntity(any(BlockPos.class))).thenAnswer(
                invocation -> counters.get(invocation.getArgument(0))
        );
        when(first.insertStack(any(ItemStack.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
        );
        when(second.insertStack(any(ItemStack.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
        );
        when(third.insertStack(any(ItemStack.class)))
                .thenReturn(ItemStack.EMPTY);

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.insertStackConnected(
                        level,
                        firstPos,
                        new ItemStack(Items.APPLE)
                );

        assertTrue(remainder.isEmpty());
        verify(first, times(1)).insertStack(any(ItemStack.class));
        verify(second, times(1)).insertStack(any(ItemStack.class));
        verify(third, times(1)).insertStack(any(ItemStack.class));
    }

    @Test
    void removesFromTheFirstNonemptyConnectedCounter() {
        Level level = mock(Level.class);
        BlockPos firstPos = BlockPos.ZERO;
        BlockPos secondPos = firstPos.east();
        BlockPos thirdPos = secondPos.east();
        CustomerPickupCounterBlockEntity first =
                mock(CustomerPickupCounterBlockEntity.class);
        CustomerPickupCounterBlockEntity second =
                mock(CustomerPickupCounterBlockEntity.class);
        CustomerPickupCounterBlockEntity third =
                mock(CustomerPickupCounterBlockEntity.class);
        Map<BlockPos, CustomerPickupCounterBlockEntity> counters = Map.of(
                firstPos,
                first,
                secondPos,
                second,
                thirdPos,
                third
        );
        when(level.getBlockEntity(any(BlockPos.class))).thenAnswer(
                invocation -> counters.get(invocation.getArgument(0))
        );
        when(first.removeOldest()).thenReturn(ItemStack.EMPTY);
        when(second.removeOldest()).thenReturn(ItemStack.EMPTY);
        when(third.removeOldest()).thenReturn(
                new ItemStack(Items.BREAD)
        );

        ItemStack removed =
                CustomerPickupCounterBlockEntity.removeOldestConnected(
                        level,
                        firstPos
                );

        assertTrue(removed.is(Items.BREAD));
        verify(first, times(1)).removeOldest();
        verify(second, times(1)).removeOldest();
        verify(third, times(1)).removeOldest();
    }
}
