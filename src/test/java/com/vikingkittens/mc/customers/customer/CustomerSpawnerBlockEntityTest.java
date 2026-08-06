package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerSpawnerBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    /** Assigns a crafted stack across multiple active customers. */
    @Test
    void assignsCraftedItemsAcrossCustomersAndCreditsEveryItem() {
        UUID playerId = UUID.randomUUID();
        CustomerVillagerEntity first = mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity second = mock(CustomerVillagerEntity.class);
        ItemStack input = new ItemStack(Items.BREAD, 10);
        ItemStack firstRemainder = new ItemStack(Items.BREAD, 4);
        Map<UUID, Integer> craftedByPlayer = new HashMap<>();
        when(first.tryAssignCraftedOffer(input)).thenReturn(firstRemainder);
        when(second.tryAssignCraftedOffer(firstRemainder)).thenReturn(null);

        ItemStack result = CustomerSpawnerBlockEntity.tryAssignCraftedItem(
                List.of(first, second),
                craftedByPlayer,
                playerId,
                input
        );

        assertNull(result);
        assertEquals(10, craftedByPlayer.get(playerId));
    }

    /** Returns the unmatched portion and credits only assigned item units. */
    @Test
    void partiallyAssignsCraftedItems() {
        UUID playerId = UUID.randomUUID();
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        ItemStack input = new ItemStack(Items.BREAD, 10);
        ItemStack remainder = new ItemStack(Items.BREAD, 3);
        Map<UUID, Integer> craftedByPlayer = new HashMap<>();
        when(customer.tryAssignCraftedOffer(input)).thenReturn(remainder);

        ItemStack result = CustomerSpawnerBlockEntity.tryAssignCraftedItem(
                List.of(customer),
                craftedByPlayer,
                playerId,
                input
        );

        assertSame(remainder, result);
        assertEquals(7, craftedByPlayer.get(playerId));
    }

    /** Leaves the score unchanged when no customer wants the item. */
    @Test
    void doesNotCreditUnassignedCraftedItems() {
        UUID playerId = UUID.randomUUID();
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        ItemStack input = new ItemStack(Items.IRON_INGOT, 8);
        Map<UUID, Integer> craftedByPlayer = new HashMap<>();
        when(customer.tryAssignCraftedOffer(input)).thenReturn(input);

        ItemStack result = CustomerSpawnerBlockEntity.tryAssignCraftedItem(
                List.of(customer),
                craftedByPlayer,
                playerId,
                input
        );

        assertSame(input, result);
        assertFalse(craftedByPlayer.containsKey(playerId));
    }
    /** Previews assignable demand across customers without changing scores. */
    @Test
    void previewsCraftedAssignmentAcrossCustomers() {
        CustomerVillagerEntity first =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity second =
                mock(CustomerVillagerEntity.class);
        when(first.getAssignableCraftedItemCount(any(ItemStack.class)))
                .thenReturn(6);
        when(second.getAssignableCraftedItemCount(any(ItemStack.class)))
                .thenReturn(4);

        int assignable =
                CustomerSpawnerBlockEntity
                        .getAssignableCraftedItemCount(
                                List.of(first, second),
                                new ItemStack(Items.BREAD, 10)
                        );

        assertEquals(10, assignable);
    }

    /** Rebuilds reservations without requiring or changing player scores. */
    @Test
    void reservesCraftedItemsWithoutAwardingMoreCredit() {
        CustomerVillagerEntity first =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity second =
                mock(CustomerVillagerEntity.class);
        ItemStack input = new ItemStack(Items.BREAD, 10);
        ItemStack firstRemainder = new ItemStack(Items.BREAD, 4);
        when(first.tryAssignCraftedOffer(input))
                .thenReturn(firstRemainder);
        when(second.tryAssignCraftedOffer(firstRemainder))
                .thenReturn(null);

        ItemStack result =
                CustomerSpawnerBlockEntity.tryReserveCraftedItem(
                        List.of(first, second),
                        input
                );

        assertNull(result);
        verify(first).tryAssignCraftedOffer(input);
        verify(second).tryAssignCraftedOffer(firstRemainder);
    }

    @Test
    void releasesCraftedReservationsAcrossCustomers() {
        CustomerVillagerEntity first =
                mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity second =
                mock(CustomerVillagerEntity.class);
        when(first.releaseCraftedOfferAssignment(
                any(ItemStack.class)
        )).thenReturn(6);
        when(second.releaseCraftedOfferAssignment(
                any(ItemStack.class)
        )).thenReturn(4);

        int released =
                CustomerSpawnerBlockEntity
                        .releaseCraftedItemAssignment(
                                List.of(first, second),
                                new ItemStack(Items.BREAD, 10)
                        );

        assertEquals(10, released);
        verify(first).releaseCraftedOfferAssignment(
                argThat(stack -> stack.getCount() == 10)
        );
        verify(second).releaseCraftedOfferAssignment(
                argThat(stack -> stack.getCount() == 4)
        );
    }
    @Test
    void usesFirstMaximumCustomerStackAndIgnoresLaterStacks() {
        Item maximumItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(4);
        inventory.setStackInSlot(1, new ItemStack(maximumItem, 7));
        inventory.setStackInSlot(2, new ItemStack(maximumItem, 12));

        assertEquals(
                OptionalInt.of(7),
                CustomerSpawnerBlockEntity.getMaxCustomersOverrideFromInventory(
                        inventory,
                        () -> maximumItem
                )
        );
    }

    @Test
    void hasNoOverrideWithoutMaximumCustomerItem() {
        Item ordinaryItem = createItem();
        Item maximumItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(4);
        inventory.setStackInSlot(0, new ItemStack(ordinaryItem, 3));

        assertTrue(
                CustomerSpawnerBlockEntity.getMaxCustomersOverrideFromInventory(
                        inventory,
                        () -> maximumItem
                ).isEmpty()
        );
    }

    @Test
    void createsNoOffersFromEmptyInventory() {
        MerchantOffers offers = getOffers(
                RandomSource.create(1L),
                new ItemStackHandler(9),
                createItem(),
                createItem()
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void createsNoOffersFromPaymentAndMaximumItems() {
        Item paymentItem = createItem();
        Item maximumItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(paymentItem, 4));
        inventory.setStackInSlot(1, new ItemStack(maximumItem, 7));

        assertTrue(getOffers(
                RandomSource.create(1L),
                inventory,
                paymentItem,
                maximumItem
        ).isEmpty());
    }

    @Test
    void createsOfferWithDefaultPaymentAndSingleItemCount() {
        Item paymentItem = createItem();
        Item maximumItem = createItem();
        Item wantedItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(wantedItem));

        MerchantOffer offer = getOffers(
                RandomSource.create(1L),
                inventory,
                paymentItem,
                maximumItem
        ).getFirst();

        assertSame(wantedItem, offer.getItemCostA().item().value());
        assertEquals(1, offer.getItemCostA().count());
        assertSame(paymentItem, offer.getResult().getItem());
        assertEquals(1, offer.getResult().getCount());
    }

    @Test
    void appliesRowPaymentToEveryRequestedItem() {
        Item paymentItem = createItem();
        Item maximumItem = createItem();
        Item wantedItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(wantedItem, 5));
        inventory.setStackInSlot(1, new ItemStack(paymentItem, 2));
        RandomSource random = mock(RandomSource.class);
        when(random.nextIntBetweenInclusive(1, 5)).thenReturn(3);

        MerchantOffer offer = getOffers(
                random,
                inventory,
                paymentItem,
                maximumItem
        ).getFirst();

        assertEquals(3, offer.getItemCostA().count());
        assertEquals(6, offer.getResult().getCount());
    }

    @Test
    void ignoresMaximumItemWhileKeepingOrdinaryRowItem() {
        Item paymentItem = createItem();
        Item maximumItem = createItem();
        Item wantedItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(maximumItem, 7));
        inventory.setStackInSlot(1, new ItemStack(wantedItem));

        MerchantOffers offers = getOffers(
                RandomSource.create(1L),
                inventory,
                paymentItem,
                maximumItem
        );

        assertEquals(1, offers.size());
        assertSame(
                wantedItem,
                offers.getFirst().getItemCostA().item().value()
        );
    }

    @Test
    void choosesOnlyOneCandidateFromEachSelectedRow() {
        Item paymentItem = createItem();
        Item maximumItem = createItem();
        Item firstItem = createItem();
        Item secondItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(9);
        inventory.setStackInSlot(0, new ItemStack(firstItem));
        inventory.setStackInSlot(1, new ItemStack(secondItem));
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(2)).thenReturn(1);

        MerchantOffers offers = getOffers(
                random,
                inventory,
                paymentItem,
                maximumItem
        );

        assertEquals(1, offers.size());
        assertSame(
                secondItem,
                offers.getFirst().getItemCostA().item().value()
        );
    }

    @Test
    void createsOfferForEachSelectedPopulatedRow() {
        Item paymentItem = createItem();
        Item maximumItem = createItem();
        ItemStackHandler inventory = new ItemStackHandler(18);
        inventory.setStackInSlot(0, new ItemStack(createItem()));
        inventory.setStackInSlot(9, new ItemStack(createItem()));
        RandomSource random = mock(RandomSource.class);
        when(random.nextIntBetweenInclusive(1, 2)).thenReturn(2);
        when(random.nextInt(anyInt())).thenReturn(0);

        MerchantOffers offers = getOffers(
                random,
                inventory,
                paymentItem,
                maximumItem
        );

        assertEquals(2, offers.size());
    }

    @Test
    void firstCustomerReservesCounterAndIsAtFrontOfLine() {
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        BlockPos position = new BlockPos(10, 20, 30);
        UUID customerId = UUID.randomUUID();

        UUID result = CustomerSpawnerBlockEntity.tryReserveTargetCounterPosition(
                reservations,
                position,
                customerId,
                ignored -> true
        );

        assertEquals(customerId, result);
        assertEquals(List.of(customerId), reservations.get(position));
    }

    @Test
    void addsNewCustomersToBeginningAndReturnsLastCustomer() {
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        BlockPos position = new BlockPos(10, 20, 30);
        UUID firstCustomerId = UUID.randomUUID();
        UUID secondCustomerId = UUID.randomUUID();
        reservations.put(position, new ArrayList<>(List.of(firstCustomerId)));

        UUID result = CustomerSpawnerBlockEntity.tryReserveTargetCounterPosition(
                reservations,
                position,
                secondCustomerId,
                ignored -> true
        );

        assertEquals(firstCustomerId, result);
        assertEquals(
                List.of(secondCustomerId, firstCustomerId),
                reservations.get(position)
        );
    }

    @Test
    void reservingSameCustomerDoesNotDuplicateIt() {
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        BlockPos position = new BlockPos(10, 20, 30);
        UUID customerId = UUID.randomUUID();
        reservations.put(position, new ArrayList<>(List.of(customerId)));

        CustomerSpawnerBlockEntity.tryReserveTargetCounterPosition(
                reservations,
                position,
                customerId,
                ignored -> true
        );

        assertEquals(List.of(customerId), reservations.get(position));
    }

    @Test
    void removesInvalidCustomersBeforeAddingNewCustomer() {
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        BlockPos position = new BlockPos(10, 20, 30);
        UUID invalidCustomerId = UUID.randomUUID();
        UUID activeCustomerId = UUID.randomUUID();
        UUID newCustomerId = UUID.randomUUID();
        reservations.put(
                position,
                new ArrayList<>(List.of(activeCustomerId, invalidCustomerId))
        );

        UUID result = CustomerSpawnerBlockEntity.tryReserveTargetCounterPosition(
                reservations,
                position,
                newCustomerId,
                id -> !id.equals(invalidCustomerId)
        );

        assertEquals(activeCustomerId, result);
        assertEquals(
                List.of(newCustomerId, activeCustomerId),
                reservations.get(position)
        );
    }

    @Test
    void returnsCustomerBeingFollowed() {
        BlockPos position = new BlockPos(10, 20, 30);
        UUID newestCustomerId = UUID.randomUUID();
        UUID middleCustomerId = UUID.randomUUID();
        UUID counterCustomerId = UUID.randomUUID();
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        reservations.put(
                position,
                new ArrayList<>(List.of(
                        newestCustomerId,
                        middleCustomerId,
                        counterCustomerId
                ))
        );

        assertEquals(
                middleCustomerId,
                CustomerSpawnerBlockEntity
                        .getReservedTargetCounterPositionFollowingCustomerId(
                                reservations,
                                position,
                                newestCustomerId,
                                ignored -> true
                        )
        );
        assertEquals(
                counterCustomerId,
                CustomerSpawnerBlockEntity
                        .getReservedTargetCounterPositionFollowingCustomerId(
                                reservations,
                                position,
                                middleCustomerId,
                                ignored -> true
                        )
        );
    }

    @Test
    void followingCustomerLookupRemovesInactiveReservations() {
        BlockPos position = new BlockPos(10, 20, 30);
        UUID queuedCustomerId = UUID.randomUUID();
        UUID inactiveCustomerId = UUID.randomUUID();
        UUID activeCustomerId = UUID.randomUUID();
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        reservations.put(
                position,
                new ArrayList<>(List.of(
                        queuedCustomerId,
                        inactiveCustomerId,
                        activeCustomerId
                ))
        );

        UUID followingCustomerId = CustomerSpawnerBlockEntity
                .getReservedTargetCounterPositionFollowingCustomerId(
                        reservations,
                        position,
                        queuedCustomerId,
                        id -> !id.equals(inactiveCustomerId)
                );

        assertEquals(activeCustomerId, followingCustomerId);
        assertEquals(
                List.of(queuedCustomerId, activeCustomerId),
                reservations.get(position)
        );
    }

    @Test
    void lastCustomerHasNoCustomerToFollow() {
        BlockPos position = new BlockPos(10, 20, 30);
        UUID customerId = UUID.randomUUID();
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        reservations.put(position, new ArrayList<>(List.of(customerId)));

        assertEquals(
                null,
                CustomerSpawnerBlockEntity
                        .getReservedTargetCounterPositionFollowingCustomerId(
                                reservations,
                                position,
                                customerId,
                                ignored -> true
                        )
        );
    }

    @Test
    void roundTripsReservedTargetCounterPositionQueues() {
        Map<BlockPos, List<UUID>> reservations = Map.of(
                new BlockPos(10, 20, 30),
                List.of(UUID.randomUUID(), UUID.randomUUID()),
                new BlockPos(-5, 70, 12),
                List.of(UUID.randomUUID())
        );

        CompoundTag tag = new CompoundTag();
        DataWriter writer = PersistenceCUtils.writer(tag);
        CustomerSpawnerBlockEntity.saveReservedTargetCounterPositions(writer, reservations);
        DataReader reader = PersistenceCUtils.reader(tag);
        Map<BlockPos, List<UUID>> loaded =
                CustomerSpawnerBlockEntity.loadReservedTargetCounterPositions(reader);

        assertEquals(reservations, loaded);
    }

    @Test
    void identifiesActiveCustomerById() {
        ServerLevel level = mock(ServerLevel.class);
        UUID customerId = UUID.randomUUID();
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        when(level.getEntity(customerId)).thenReturn(customer);
        when(customer.isAlive()).thenReturn(true);
        when(customer.getState()).thenReturn(CustomerState.BUYING);

        assertTrue(CustomerVillagerEntity.isActiveCustomer(level, customerId));
    }

    @Test
    void rejectsDoneCustomerById() {
        ServerLevel level = mock(ServerLevel.class);
        UUID customerId = UUID.randomUUID();
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        when(level.getEntity(customerId)).thenReturn(customer);
        when(customer.isAlive()).thenReturn(true);
        when(customer.getState()).thenReturn(CustomerState.DONE);

        assertFalse(CustomerVillagerEntity.isActiveCustomer(level, customerId));
    }
    @Test
    void findsOnlyActiveCustomersForClientSnapshots() {
        ServerLevel level = mock(ServerLevel.class);
        UUID activeId = UUID.randomUUID();
        UUID doneId = UUID.randomUUID();
        CustomerVillagerEntity active = mock(CustomerVillagerEntity.class);
        CustomerVillagerEntity done = mock(CustomerVillagerEntity.class);
        when(level.getEntity(activeId)).thenReturn(active);
        when(level.getEntity(doneId)).thenReturn(done);
        when(active.isAlive()).thenReturn(true);
        when(active.getState()).thenReturn(CustomerState.BUYING);
        when(done.isAlive()).thenReturn(true);
        when(done.getState()).thenReturn(CustomerState.DONE);

        List<CustomerVillagerEntity> customers =
                CustomerSpawnerBlockEntity.getActiveCustomers(
                        level,
                        Set.of(activeId, doneId)
                );

        assertEquals(List.of(active), customers);
    }

    @Test
    void findsOnlyPlayersWithinSpawnerViewRange() {
        BlockPos spawnerPos = BlockPos.ZERO;
        UUID nearbyId = UUID.randomUUID();
        UUID boundaryId = UUID.randomUUID();
        UUID distantId = UUID.randomUUID();
        ServerPlayer nearby = mock(ServerPlayer.class);
        ServerPlayer boundary = mock(ServerPlayer.class);
        ServerPlayer distant = mock(ServerPlayer.class);
        when(nearby.getUUID()).thenReturn(nearbyId);
        when(nearby.blockPosition()).thenReturn(new BlockPos(10, 0, 0));
        when(boundary.getUUID()).thenReturn(boundaryId);
        when(boundary.blockPosition()).thenReturn(new BlockPos(64, 0, 0));
        when(distant.getUUID()).thenReturn(distantId);
        when(distant.blockPosition()).thenReturn(new BlockPos(65, 0, 0));

        assertEquals(
                Set.of(nearbyId, boundaryId),
                CustomerSpawnerBlockEntity.getPlayerIdsInRange(
                        spawnerPos,
                        List.of(nearby, boundary, distant),
                        64.0D
                )
        );
    }
    @Test
    void identifiesPlayersEnteringAndLeavingSpawnerViewRange() {
        UUID leavingId = UUID.randomUUID();
        UUID retainedId = UUID.randomUUID();
        UUID enteringId = UUID.randomUUID();

        CustomerSpawnerBlockEntity.PlayerRangeChanges changes =
                CustomerSpawnerBlockEntity.getPlayerRangeChanges(
                        Set.of(leavingId, retainedId),
                        Set.of(retainedId, enteringId)
                );

        assertEquals(Set.of(enteringId), changes.entering());
        assertEquals(Set.of(leavingId), changes.leaving());
    }
    @Test
    void addsTrackedPlayersWhenABossBarIsCreatedAfterRangeTracking() {
        UUID trackedPlayer = UUID.randomUUID();

        Set<UUID> playersToAdd =
                CustomerSpawnerBlockEntity.getPlayerIdsToAddToBossBar(
                        Set.of(),
                        Set.of(trackedPlayer)
                );

        assertEquals(Set.of(trackedPlayer), playersToAdd);
    }

    @Test
    void doesNotReaddPlayersAlreadyAttachedToTheBossBar() {
        UUID attachedPlayer = UUID.randomUUID();

        Set<UUID> playersToAdd =
                CustomerSpawnerBlockEntity.getPlayerIdsToAddToBossBar(
                        Set.of(attachedPlayer),
                        Set.of(attachedPlayer)
                );

        assertEquals(Set.of(), playersToAdd);
    }

    private static MerchantOffers getOffers(
            RandomSource random,
            ItemStackHandler inventory,
            Item paymentItem,
            Item maximumItem
    ) {
        return CustomerSpawnerBlockEntity.getOffersFromInventory(
                random,
                inventory,
                () -> paymentItem,
                () -> maximumItem
        );
    }

    @SuppressWarnings("unchecked")
    private static Item createItem() {
        Item item = mock(Item.class);
        Holder.Reference<Item> holder = mock(Holder.Reference.class);
        when(item.asItem()).thenReturn(item);
        when(item.components()).thenReturn(DataComponentMap.EMPTY);
        when(item.builtInRegistryHolder()).thenReturn(holder);
        when(holder.value()).thenReturn(item);
        return item;
    }
}
