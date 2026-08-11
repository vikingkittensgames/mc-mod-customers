package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterBlockEntityTest {
    @Test
    void playerOwnedItemsUseAllCustomerDemand() {
        assertEquals(
                CustomerPickupCounterBlockEntity.CustomerScope.ALL,
                CustomerPickupCounterBlockEntity.customerScope(
                        UUID.randomUUID()
                )
        );
    }

    @Test
    void automatedItemsUseOnlyThisCounterBlockDemand() {
        assertEquals(
                CustomerPickupCounterBlockEntity
                        .CustomerScope.COUNTER_BLOCK,
                CustomerPickupCounterBlockEntity.customerScope(null)
        );
    }

    @Test
    void insertsOnlyLiveRemainingDemandAndCreditsItsSpawner() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID existingOwner = UUID.randomUUID();
        UUID incomingOwner = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.COOKIE, 3),
                        true,
                        existingOwner
                )
        );
        CustomerSpawnerBlockEntity firstSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity secondSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerVillagerEntity firstCustomer =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity secondCustomer =
                mock(CustomerVillagerEntity.class);
        MerchantOffers firstOffers = new MerchantOffers();
        MerchantOffers secondOffers = new MerchantOffers();
        firstOffers.add(offer(Items.COOKIE, 2));
        secondOffers.add(offer(Items.COOKIE, 3));
        when(firstSpawner.getActiveCustomers())
                .thenReturn(List.of(firstCustomer));
        when(secondSpawner.getActiveCustomers())
                .thenReturn(List.of(secondCustomer));
        when(firstCustomer.getOffers()).thenReturn(firstOffers);
        when(secondCustomer.getOffers()).thenReturn(secondOffers);

        ItemStack remainder =
                CustomerPickupCounterBlockEntity
                        .insertLiveDemandStack(
                                List.of(counter),
                                List.of(
                                        firstSpawner,
                                        secondSpawner
                                ),
                                incomingOwner,
                                new ItemStack(Items.COOKIE, 4)
                        );

        assertEquals(2, remainder.getCount());
        CustomerPickupCounterBlockEntity.StoredStack existing =
                counter.removeOldestStored();
        CustomerPickupCounterBlockEntity.StoredStack inserted =
                counter.removeOldestStored();
        assertEquals(3, existing.stack().getCount());
        assertEquals(existingOwner, existing.crafterId());
        assertEquals(2, inserted.stack().getCount());
        assertEquals(incomingOwner, inserted.crafterId());
        verify(firstSpawner, never())
                .scoreboardAddItemsCrafted(
                        any(UUID.class),
                        anyInt()
                );
        verify(secondSpawner)
                .scoreboardAddItemsCrafted(incomingOwner, 2);
        verify(firstSpawner, never()).tryAssignCraftedItem(
                any(UUID.class),
                any(ItemStack.class)
        );
        verify(secondSpawner, never()).tryAssignCraftedItem(
                any(UUID.class),
                any(ItemStack.class)
        );
    }

    @Test
    void allocatesOnlyRemainingDemandToAnIncomingStack() {
        CustomerSpawnerBlockEntity firstSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity secondSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        List<CustomerPickupCounterBlockEntity.CustomerOffer> offers =
                List.of(
                        new CustomerPickupCounterBlockEntity.CustomerOffer(
                                firstSpawner,
                                offer(Items.COOKIE, 2)
                        ),
                        new CustomerPickupCounterBlockEntity.CustomerOffer(
                                secondSpawner,
                                offer(Items.COOKIE, 3)
                        )
                );

        CustomerPickupCounterBlockEntity.IncomingAllocation allocation =
                CustomerPickupCounterBlockEntity
                        .allocateIncoming(
                                offers,
                                List.of(
                                        new ItemStack(Items.COOKIE, 3)
                                ),
                                new ItemStack(Items.COOKIE, 4)
                        );

        assertEquals(2, allocation.acceptedCount());
        assertEquals(
                Map.of(secondSpawner, 2),
                allocation.acceptedBySpawner()
        );
    }

    @Test
    void rejectsIncomingItemsWhenExistingInventoryFillsDemand() {
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        List<CustomerPickupCounterBlockEntity.CustomerOffer> offers =
                List.of(
                        new CustomerPickupCounterBlockEntity.CustomerOffer(
                                spawner,
                                offer(Items.COOKIE, 3)
                        )
                );

        CustomerPickupCounterBlockEntity.IncomingAllocation allocation =
                CustomerPickupCounterBlockEntity
                        .allocateIncoming(
                                offers,
                                List.of(
                                        new ItemStack(Items.COOKIE, 3)
                                ),
                                new ItemStack(Items.COOKIE, 4)
                        );

        assertEquals(0, allocation.acceptedCount());
        assertTrue(allocation.acceptedBySpawner().isEmpty());
    }

    @Test
    void collectsOffersWithTheirOriginatingSpawner() {
        CustomerSpawnerBlockEntity firstSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity secondSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerVillagerEntity firstCustomer =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity secondCustomer =
                mock(CustomerVillagerEntity.class);
        MerchantOffer firstOffer = mock(MerchantOffer.class);
        MerchantOffer secondOffer = mock(MerchantOffer.class);
        MerchantOffers firstOffers = new MerchantOffers();
        MerchantOffers secondOffers = new MerchantOffers();
        firstOffers.add(firstOffer);
        secondOffers.add(secondOffer);
        when(firstSpawner.getActiveCustomers())
                .thenReturn(List.of(firstCustomer));
        when(secondSpawner.getActiveCustomers())
                .thenReturn(List.of(secondCustomer));
        when(firstCustomer.getOffers()).thenReturn(firstOffers);
        when(secondCustomer.getOffers()).thenReturn(secondOffers);

        List<CustomerPickupCounterBlockEntity.CustomerOffer>
                offers =
                        CustomerPickupCounterBlockEntity
                                .findCustomerOffers(
                                        List.of(
                                                firstSpawner,
                                                secondSpawner
                                        )
                                );

        assertEquals(2, offers.size());
        assertEquals(firstSpawner, offers.getFirst().spawner());
        assertEquals(firstOffer, offers.getFirst().offer());
        assertEquals(secondSpawner, offers.getLast().spawner());
        assertEquals(secondOffer, offers.getLast().offer());
    }

    @Test
    void combinesDistinctActiveCustomersFromScopedSpawners() {
        CustomerSpawnerBlockEntity firstSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity secondSpawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerVillagerEntity first =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity shared =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity second =
                mock(CustomerVillagerEntity.class);
        when(firstSpawner.getActiveCustomers())
                .thenReturn(List.of(first, shared));
        when(secondSpawner.getActiveCustomers())
                .thenReturn(List.of(shared, second));

        assertEquals(
                List.of(first, shared, second),
                CustomerPickupCounterBlockEntity
                        .findActiveCustomers(
                                List.of(
                                        firstSpawner,
                                        secondSpawner
                                )
                        )
        );
    }

    @Test
    void allCustomerScopeKeepsEveryDiscoveredSpawner() {
        Level level = mock(Level.class);
        Block counterBlock = mock(Block.class);
        CustomerSpawnerBlockEntity first =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity second =
                mock(CustomerSpawnerBlockEntity.class);

        assertEquals(
                List.of(first, second),
                CustomerPickupCounterBlockEntity
                        .filterCustomerSpawners(
                                level,
                                List.of(first, second),
                                counterBlock,
                                CustomerPickupCounterBlockEntity
                                        .CustomerScope.ALL
                        )
        );
    }

    @Test
    void counterBlockScopeKeepsOnlyMatchingSpawnerCounters() {
        Level level = mock(Level.class);
        Block counterBlock = mock(Block.class);
        Block otherBlock = mock(Block.class);
        CustomerSpawnerBlockEntity matching =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity different =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerSpawnerBlockEntity missing =
                mock(CustomerSpawnerBlockEntity.class);
        BlockPos matchingPos = new BlockPos(1, 2, 3);
        BlockPos differentPos = new BlockPos(4, 5, 6);
        BlockPos missingPos = new BlockPos(7, 8, 9);
        BlockState matchingState = mock(BlockState.class);
        BlockState differentState = mock(BlockState.class);
        when(matching.getBlockPos()).thenReturn(matchingPos);
        when(different.getBlockPos()).thenReturn(differentPos);
        when(missing.getBlockPos()).thenReturn(missingPos);
        when(level.getBlockState(matchingPos.above()))
                .thenReturn(matchingState);
        when(level.getBlockState(differentPos.above()))
                .thenReturn(differentState);
        when(matchingState.getBlock()).thenReturn(counterBlock);
        when(differentState.getBlock()).thenReturn(otherBlock);

        assertEquals(
                List.of(matching),
                CustomerPickupCounterBlockEntity
                        .filterCustomerSpawners(
                                level,
                                List.of(matching, different, missing),
                                counterBlock,
                                CustomerPickupCounterBlockEntity
                                        .CustomerScope.COUNTER_BLOCK
                        )
        );
    }

    private static CustomerPickupCounterBlockEntity createCounter() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomerPickupCounterBlockEntity(
                type,
                BlockPos.ZERO,
                state
        );
    }

    private static MerchantOffer offer(Item item, int count) {
        return new MerchantOffer(
                new ItemCost(item, count),
                Optional.empty(),
                new ItemStack(Items.EMERALD),
                1,
                1,
                0.0F
        );
    }

    @Test
    void mergesAssignedStacksWithTheSameOwner() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID ownerId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 20),
                        true,
                        ownerId
                )
        );

        ItemStack remainder = counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 12),
                        true,
                        ownerId
                )
        );

        assertTrue(remainder.isEmpty());
        assertEquals(1, counter.getDisplayItems().size());
        assertEquals(32, counter.getDisplayItems().getFirst().getCount());
    }

    @Test
    void mergesAssignedOwnerlessStacks() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 20),
                        true,
                        null
                )
        );

        ItemStack remainder = counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 12),
                        true,
                        null
                )
        );

        assertTrue(remainder.isEmpty());
        assertEquals(1, counter.getDisplayItems().size());
        assertEquals(32, counter.getDisplayItems().getFirst().getCount());
    }

    @Test
    void mergesStacksWithoutLegacyAssignmentMetadata() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID ownerId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 20),
                        false,
                        ownerId
                )
        );

        ItemStack remainder = counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 12),
                        false,
                        ownerId
                )
        );

        assertTrue(remainder.isEmpty());
        assertEquals(1, counter.getDisplayItems().size());
        assertEquals(32, counter.getDisplayItems().getFirst().getCount());
    }

    @Test
    void keepsStacksWithDifferentOwnersSeparate() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 20),
                        true,
                        UUID.randomUUID()
                )
        );

        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 12),
                        true,
                        UUID.randomUUID()
                )
        );

        assertEquals(2, counter.getDisplayItems().size());
    }

    @Test
    void capacityIncludesSpaceInMatchingMetadataStacks() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 60),
                        true,
                        null
                )
        );
        for (int slot = 1; slot < 9; slot++) {
            counter.insertStoredStack(
                    new CustomerPickupCounterBlockEntity.StoredStack(
                            new ItemStack(Items.APPLE),
                            true,
                            null
                    )
            );
        }

        assertTrue(CustomerPickupCounterBlockEntity.hasCapacity(
                List.of(counter),
                List.of(
                        new CustomerPickupCounterBlockEntity.StoredStack(
                                new ItemStack(Items.BREAD, 4),
                                true,
                                null
                        )
                )
        ));
    }

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
    void savesAndLoadsCrafterMetadataWithoutAssignmentState() {
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

        assertFalse(tag.toString().contains("assigned"));

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
        assertEquals(firstCrafter, first.crafterId());
        assertTrue(second.stack().is(Items.BREAD));
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
            first.insertStack(new ItemStack(Items.APPLE, 64));
            second.insertStack(new ItemStack(Items.CARROT, 64));
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
        assertEquals(1, second.getFreeSlotCount());
        for (int slot = 0; slot < 8; slot++) {
            first.removeOldestStored();
            second.removeOldestStored();
        }
        CustomerPickupCounterBlockEntity.StoredStack stored =
                first.removeOldestStored();
        assertEquals(20, stored.stack().getCount());
        assertEquals(crafterId, stored.crafterId());
        assertTrue(second.removeOldestStored().stack().isEmpty());
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
            counter.insertStack(new ItemStack(Items.APPLE, 64));
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
            counter.insertStack(new ItemStack(Items.APPLE, 64));
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
    void findsSpawnerReferencedByNearbyCustomer() {
        Level level = mock(Level.class);
        CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        BlockPos spawnerPos = new BlockPos(40, 0, 0);
        when(customer.getSpawnerPos()).thenReturn(spawnerPos);
        when(level.getBlockEntity(spawnerPos)).thenReturn(spawner);

        List<CustomerSpawnerBlockEntity> spawners =
                CustomerPickupCounterBlockEntity.findCustomerSpawners(
                        level,
                        List.of(),
                        List.of(customer)
                );

        assertEquals(List.of(spawner), spawners);
    }

    @Test
    void findsEachCustomerSpawnerOnlyOnce() {
        Level level = mock(Level.class);
        CustomerVillagerEntity firstCustomer =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity secondCustomer =
                mock(CustomerVillagerEntity.class);
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        BlockPos spawnerPos = BlockPos.ZERO;
        when(firstCustomer.getSpawnerPos()).thenReturn(spawnerPos);
        when(secondCustomer.getSpawnerPos()).thenReturn(spawnerPos);
        when(level.getBlockEntity(spawnerPos)).thenReturn(spawner);

        List<CustomerSpawnerBlockEntity> spawners =
                CustomerPickupCounterBlockEntity.findCustomerSpawners(
                        level,
                        List.of(spawnerPos),
                        List.of(firstCustomer, secondCustomer)
                );

        assertEquals(List.of(spawner), spawners);
        verify(level, times(1)).getBlockEntity(spawnerPos);
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
