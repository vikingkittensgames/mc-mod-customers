package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    /** Preserves assignment and crafter metadata through persistence. */
    @Test
    void savesAndLoadsStoredStackMetadata() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        UUID crafterId = UUID.randomUUID();
        CustomerPickupCounterBlockEntity source =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        source.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 25),
                        true,
                        crafterId
                )
        );
        CompoundTag tag = new CompoundTag();

        source.saveAdditional(tag, RegistryAccess.EMPTY);

        CustomerPickupCounterBlockEntity restored =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        restored.loadAdditional(tag, RegistryAccess.EMPTY);
        CustomerPickupCounterBlockEntity.StoredStack removed =
                restored.removeOldestStored();

        assertTrue(removed.stack().is(Items.BREAD));
        assertEquals(25, removed.stack().getCount());
        assertTrue(removed.assigned());
        assertEquals(crafterId, removed.crafterId());
    }

    /** Moves stack metadata with items as FIFO slots shift. */
    @Test
    void shiftsStoredStackMetadataWithItems() {
        UUID firstCrafter = UUID.randomUUID();
        UUID secondCrafter = UUID.randomUUID();
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.APPLE),
                        true,
                        firstCrafter
                )
        );
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 12),
                        false,
                        secondCrafter
                )
        );

        CustomerPickupCounterBlockEntity.StoredStack first =
                counter.removeOldestStored();
        CustomerPickupCounterBlockEntity.StoredStack second =
                counter.removeOldestStored();

        assertTrue(first.stack().is(Items.APPLE));
        assertTrue(first.assigned());
        assertEquals(firstCrafter, first.crafterId());
        assertTrue(second.stack().is(Items.BREAD));
        assertFalse(second.assigned());
        assertEquals(secondCrafter, second.crafterId());
    }

    /** Treats stacks from saves without metadata as legacy unassigned items. */
    @Test
    void loadsLegacyInventoryWithoutMetadata() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity source =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        source.insertStack(new ItemStack(Items.CARROT, 6));
        CompoundTag tag = new CompoundTag();
        source.saveAdditional(tag, RegistryAccess.EMPTY);
        tag.remove("stackMetadata");

        CustomerPickupCounterBlockEntity restored =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        restored.loadAdditional(tag, RegistryAccess.EMPTY);
        CustomerPickupCounterBlockEntity.StoredStack removed =
                restored.removeOldestStored();

        assertTrue(removed.stack().is(Items.CARROT));
        assertFalse(removed.assigned());
        assertEquals(null, removed.crafterId());
    }

    /** Produces one assigned stack when all offered items are assigned. */
    @Test
    void splitsFullyAssignedStack() {
        UUID crafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 20);

        List<CustomerPickupCounterBlockEntity.StoredStack> stacks =
                CustomerPickupCounterBlockEntity.splitByAssignment(
                        offered,
                        null,
                        crafterId
                );

        assertEquals(1, stacks.size());
        assertEquals(20, stacks.getFirst().stack().getCount());
        assertTrue(stacks.getFirst().assigned());
        assertEquals(crafterId, stacks.getFirst().crafterId());
    }

    @Test
    void keepsOnlyTheAssignedPortionOfAPartialStack() {
        UUID crafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 20);
        ItemStack remainder = new ItemStack(Items.BREAD, 5);

        List<CustomerPickupCounterBlockEntity.StoredStack> stacks =
                CustomerPickupCounterBlockEntity.splitByAssignment(
                        offered,
                        remainder,
                        crafterId
                );

        assertEquals(1, stacks.size());
        assertEquals(15, stacks.getFirst().stack().getCount());
        assertTrue(stacks.getFirst().assigned());
        assertEquals(crafterId, stacks.getFirst().crafterId());
    }

    @Test
    void doesNotCreateAStoredStackWithoutDemand() {
        UUID crafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.IRON_INGOT, 8);

        List<CustomerPickupCounterBlockEntity.StoredStack> stacks =
                CustomerPickupCounterBlockEntity.splitByAssignment(
                        offered,
                        offered,
                        crafterId
                );

        assertTrue(stacks.isEmpty());
    }

    /** Inserts all portions across available connected-counter slots. */
    @Test
    void insertsStoredStacksAcrossConnectedCounters() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity first =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        CustomerPickupCounterBlockEntity second =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO.east(),
                        state
                );
        for (int slot = 0; slot < 8; slot++) {
            first.insertStack(new ItemStack(Items.APPLE));
            second.insertStack(new ItemStack(Items.CARROT));
        }
        UUID crafterId = UUID.randomUUID();

        boolean inserted =
                CustomerPickupCounterBlockEntity
                        .insertStoredStacksConnected(
                                List.of(first, second),
                                List.of(
                                        new CustomerPickupCounterBlockEntity
                                                .StoredStack(
                                                        new ItemStack(
                                                                Items.BREAD,
                                                                15
                                                        ),
                                                        true,
                                                        crafterId
                                                ),
                                        new CustomerPickupCounterBlockEntity
                                                .StoredStack(
                                                        new ItemStack(
                                                                Items.BREAD,
                                                                5
                                                        ),
                                                        false,
                                                        crafterId
                                                )
                                )
                        );

        assertTrue(inserted);
        assertEquals(0, first.getFreeSlotCount());
        assertEquals(0, second.getFreeSlotCount());
        for (int slot = 0; slot < 8; slot++) {
            first.removeOldestStored();
            second.removeOldestStored();
        }
        CustomerPickupCounterBlockEntity.StoredStack assigned =
                first.removeOldestStored();
        CustomerPickupCounterBlockEntity.StoredStack unassigned =
                second.removeOldestStored();
        assertEquals(15, assigned.stack().getCount());
        assertTrue(assigned.assigned());
        assertEquals(crafterId, assigned.crafterId());
        assertEquals(5, unassigned.stack().getCount());
        assertFalse(unassigned.assigned());
        assertEquals(crafterId, unassigned.crafterId());
    }

    /** Rejects the whole operation when every required slot is unavailable. */
    @Test
    void rejectsStoredStacksWithoutPartialInsertion() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        for (int slot = 0; slot < 8; slot++) {
            counter.insertStack(new ItemStack(Items.APPLE));
        }

        boolean inserted =
                CustomerPickupCounterBlockEntity
                        .insertStoredStacksConnected(
                                List.of(counter),
                                List.of(
                                        new CustomerPickupCounterBlockEntity
                                                .StoredStack(
                                                        new ItemStack(
                                                                Items.BREAD,
                                                                15
                                                        ),
                                                        true,
                                                        UUID.randomUUID()
                                                ),
                                        new CustomerPickupCounterBlockEntity
                                                .StoredStack(
                                                        new ItemStack(
                                                                Items.BREAD,
                                                                5
                                                        ),
                                                        false,
                                                        UUID.randomUUID()
                                                )
                                )
                        );

        assertFalse(inserted);
        assertEquals(1, counter.getFreeSlotCount());
        assertEquals(8, counter.getDisplayItems().size());
    }

    /** Finds each horizontally connected counter once. */
    @Test
    void findsConnectedCounterNetworkWithoutLoops() {
        Level level = mock(Level.class);
        BlockPos firstPos = BlockPos.ZERO;
        BlockPos secondPos = firstPos.east();
        CustomerPickupCounterBlockEntity first =
                mock(CustomerPickupCounterBlockEntity.class);
        CustomerPickupCounterBlockEntity second =
                mock(CustomerPickupCounterBlockEntity.class);
        Map<BlockPos, CustomerPickupCounterBlockEntity> counters =
                Map.of(firstPos, first, secondPos, second);
        when(level.getBlockEntity(any(BlockPos.class))).thenAnswer(
                invocation -> counters.get(invocation.getArgument(0))
        );

        List<CustomerPickupCounterBlockEntity> connected =
                CustomerPickupCounterBlockEntity
                        .getConnectedCounters(level, firstPos);

        assertEquals(2, connected.size());
        assertTrue(connected.contains(first));
        assertTrue(connected.contains(second));
    }

    @Test
    void storesAssignedItemsAndReturnsTheRemainder() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 20);
        when(player.getUUID()).thenReturn(playerId);
        when(spawner.getAssignableCraftedItemCount(
                any(ItemStack.class)
        )).thenReturn(15);
        when(spawner.tryAssignCraftedItem(
                playerId,
                offered
        )).thenReturn(new ItemStack(Items.BREAD, 5));

        ItemStack result =
                CustomerPickupCounterBlockEntity.insertCraftedStack(
                        List.of(counter),
                        List.of(spawner),
                        player,
                        offered
                );

        assertEquals(5, result.getCount());
        CustomerPickupCounterBlockEntity.StoredStack assigned =
                counter.removeOldestStored();
        assertEquals(15, assigned.stack().getCount());
        assertTrue(assigned.assigned());
        assertEquals(playerId, assigned.crafterId());
        assertTrue(counter.removeOldestStored().stack().isEmpty());
    }

    @Test
    void rejectsCraftedAssignmentBeforeMutatingSpawnerState() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        for (int slot = 0;
                slot < CustomerPickupCounterBlockEntity.INVENTORY_SIZE;
                slot++) {
            counter.insertStack(new ItemStack(Items.APPLE));
        }
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        Player player = mock(Player.class);
        ItemStack offered = new ItemStack(Items.BREAD, 20);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(spawner.getAssignableCraftedItemCount(
                any(ItemStack.class)
        )).thenReturn(15);

        ItemStack result =
                CustomerPickupCounterBlockEntity.insertCraftedStack(
                        List.of(counter),
                        List.of(spawner),
                        player,
                        offered
                );

        assertEquals(20, result.getCount());
        assertTrue(result.is(Items.BREAD));
        assertEquals(
                CustomerPickupCounterBlockEntity.INVENTORY_SIZE,
                counter.getDisplayItems().size()
        );
        verify(spawner, never()).tryAssignCraftedItem(
                any(UUID.class),
                any(ItemStack.class)
        );
    }

    /** Previews demand across multiple spawners using only the remainder. */
    @Test
    void previewsCraftedDemandAcrossSpawners() {
        CustomerSpawnerBlockEntity first =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity second =
                mock(CustomerSpawnerBlockEntity.class);
        when(first.getAssignableCraftedItemCount(
                any(ItemStack.class)
        )).thenReturn(12);
        when(second.getAssignableCraftedItemCount(
                any(ItemStack.class)
        )).thenReturn(8);

        int assignable =
                CustomerPickupCounterBlockEntity
                        .getAssignableCraftedItemCount(
                                List.of(first, second),
                                new ItemStack(Items.BREAD, 20)
                        );

        assertEquals(20, assignable);
    }

    /** Uses a stored crafter UUID without requiring an online player object. */
    @Test
    void assignsReprocessedItemsToStoredCrafterUuid() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID originalCrafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 8);
        when(spawner.getAssignableCraftedItemCount(
                any(ItemStack.class)
        )).thenReturn(8);
        when(spawner.tryAssignCraftedItem(
                originalCrafterId,
                offered
        )).thenReturn(null);

        ItemStack result =
                CustomerPickupCounterBlockEntity.insertCraftedStack(
                        List.of(counter),
                        List.of(spawner),
                        originalCrafterId,
                        offered
                );

        assertTrue(result.isEmpty());
        verify(spawner).tryAssignCraftedItem(
                originalCrafterId,
                offered
        );
        CustomerPickupCounterBlockEntity.StoredStack stored =
                counter.removeOldestStored();
        assertTrue(stored.assigned());
        assertEquals(originalCrafterId, stored.crafterId());
    }

    @Test
    void rejectsCraftedStackWithoutNearbySpawners() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        CustomerPickupCounterBlockEntity counter =
                new CustomerPickupCounterBlockEntity(
                        type,
                        BlockPos.ZERO,
                        state
                );
        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUUID()).thenReturn(playerId);

        ItemStack result = counter.insertCraftedStackConnected(
                player,
                new ItemStack(Items.BREAD, 25)
        );

        assertEquals(25, result.getCount());
        assertTrue(counter.getDisplayItems().isEmpty());
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
