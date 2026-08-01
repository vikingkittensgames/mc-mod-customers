package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerSpawnerBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
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

        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        DataWriter writer = PersistenceCUtils.writer(output);
        CustomerSpawnerBlockEntity.saveReservedTargetCounterPositions(writer, reservations);
        var input = TagValueInput.create(
                ProblemReporter.DISCARDING,
                HolderLookup.Provider.create(Stream.empty()),
                output.buildResult()
        );
        DataReader reader = PersistenceCUtils.reader(input);
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