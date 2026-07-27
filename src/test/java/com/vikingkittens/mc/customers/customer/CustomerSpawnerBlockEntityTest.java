package com.vikingkittens.mc.customers.customer;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Verifies customer spawner inventory controls.
 */
class CustomerSpawnerBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        LoadingModList modList = mock(LoadingModList.class);
        when(modList.getModFiles()).thenReturn(List.of());
        try (MockedStatic<LoadingModList> loadingModList =
                     mockStatic(LoadingModList.class)) {
            loadingModList.when(LoadingModList::get).thenReturn(modList);
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
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