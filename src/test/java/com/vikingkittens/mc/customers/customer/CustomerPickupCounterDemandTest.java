package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterDemandTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void storesOnlyTheWantedPortionOfAPartialStack() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID crafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 20);
        when(spawner.getAssignableCraftedItemCount(any(ItemStack.class)))
                .thenReturn(15);
        when(spawner.tryAssignCraftedItem(crafterId, offered))
                .thenReturn(new ItemStack(Items.BREAD, 5));

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.insertCraftedStack(
                        List.of(counter),
                        List.of(spawner),
                        crafterId,
                        offered
                );

        assertEquals(5, remainder.getCount());
        CustomerPickupCounterBlockEntity.StoredStack stored =
                counter.removeOldestStored();
        assertEquals(15, stored.stack().getCount());
        assertTrue(stored.assigned());
        assertEquals(crafterId, stored.crafterId());
        assertTrue(counter.removeOldestStored().stack().isEmpty());
    }

    @Test
    void rejectsItemsWithoutActiveCustomerDemand() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID crafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.IRON_INGOT, 8);
        when(spawner.getAssignableCraftedItemCount(any(ItemStack.class)))
                .thenReturn(0);

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.insertCraftedStack(
                        List.of(counter),
                        List.of(spawner),
                        crafterId,
                        offered
                );

        assertEquals(8, remainder.getCount());
        assertTrue(counter.getDisplayItems().isEmpty());
        verify(spawner, never()).tryAssignCraftedItem(
                any(UUID.class),
                any(ItemStack.class)
        );
    }

    @Test
    void preservesSeparateStacksForDifferentPlayers() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID firstCrafter = UUID.randomUUID();
        UUID secondCrafter = UUID.randomUUID();
        when(spawner.getAssignableCraftedItemCount(any(ItemStack.class)))
                .thenAnswer(invocation ->
                        invocation.<ItemStack>getArgument(0).getCount()
                );
        when(spawner.tryAssignCraftedItem(
                any(UUID.class),
                any(ItemStack.class)
        )).thenReturn(null);

        CustomerPickupCounterBlockEntity.insertCraftedStack(
                List.of(counter),
                List.of(spawner),
                firstCrafter,
                new ItemStack(Items.BREAD, 5)
        );
        CustomerPickupCounterBlockEntity.insertCraftedStack(
                List.of(counter),
                List.of(spawner),
                secondCrafter,
                new ItemStack(Items.BREAD, 7)
        );

        CustomerPickupCounterBlockEntity.StoredStack first =
                counter.removeOldestStored();
        CustomerPickupCounterBlockEntity.StoredStack second =
                counter.removeOldestStored();
        assertEquals(5, first.stack().getCount());
        assertEquals(firstCrafter, first.crafterId());
        assertEquals(7, second.stack().getCount());
        assertEquals(secondCrafter, second.crafterId());
    }

    @Test
    void doesNotReserveItemsWithoutCounterCapacity() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        for (int slot = 0;
                slot < CustomerPickupCounterBlockEntity.INVENTORY_SIZE;
                slot++) {
            counter.insertStack(new ItemStack(Items.APPLE));
        }
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID crafterId = UUID.randomUUID();
        ItemStack offered = new ItemStack(Items.BREAD, 20);
        when(spawner.getAssignableCraftedItemCount(any(ItemStack.class)))
                .thenReturn(15);

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.insertCraftedStack(
                        List.of(counter),
                        List.of(spawner),
                        crafterId,
                        offered
                );

        assertEquals(20, remainder.getCount());
        verify(spawner, never()).tryAssignCraftedItem(
                any(UUID.class),
                any(ItemStack.class)
        );
    }

    @Test
    void keepsAStackThatIsStillFullyWanted() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID crafterId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 15),
                        true,
                        crafterId
                )
        );
        when(spawner.releaseCraftedItemAssignment(
                any(ItemStack.class)
        )).thenReturn(15);
        when(spawner.tryReserveCraftedItem(
                any(ItemStack.class)
        )).thenReturn(null);

        List<CustomerPickupCounterBlockEntity.StoredStack> returned =
                CustomerPickupCounterBlockEntity.revalidateStoredStacks(
                        List.of(counter),
                        List.of(spawner)
                );

        assertTrue(returned.isEmpty());
        CustomerPickupCounterBlockEntity.StoredStack stored =
                counter.removeOldestStored();
        assertEquals(15, stored.stack().getCount());
        assertEquals(crafterId, stored.crafterId());
    }

    @Test
    void returnsThePortionThatIsNoLongerWanted() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID crafterId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 15),
                        true,
                        crafterId
                )
        );
        when(spawner.releaseCraftedItemAssignment(
                any(ItemStack.class)
        )).thenReturn(15);
        when(spawner.tryReserveCraftedItem(
                any(ItemStack.class)
        )).thenReturn(new ItemStack(Items.BREAD, 10));

        List<CustomerPickupCounterBlockEntity.StoredStack> returned =
                CustomerPickupCounterBlockEntity.revalidateStoredStacks(
                        List.of(counter),
                        List.of(spawner)
                );

        assertEquals(1, returned.size());
        assertEquals(10, returned.getFirst().stack().getCount());
        assertEquals(crafterId, returned.getFirst().crafterId());
        CustomerPickupCounterBlockEntity.StoredStack stored =
                counter.removeOldestStored();
        assertEquals(5, stored.stack().getCount());
        assertEquals(crafterId, stored.crafterId());
    }

    @Test
    void returnsACompleteStackThatIsNoLongerWanted() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        UUID crafterId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.BREAD, 15),
                        true,
                        crafterId
                )
        );
        when(spawner.releaseCraftedItemAssignment(
                any(ItemStack.class)
        )).thenReturn(15);
        when(spawner.tryReserveCraftedItem(
                any(ItemStack.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        List<CustomerPickupCounterBlockEntity.StoredStack> returned =
                CustomerPickupCounterBlockEntity.revalidateStoredStacks(
                        List.of(counter),
                        List.of(spawner)
                );

        assertEquals(1, returned.size());
        assertEquals(15, returned.getFirst().stack().getCount());
        assertEquals(crafterId, returned.getFirst().crafterId());
        assertTrue(counter.getDisplayItems().isEmpty());
    }

    @Test
    void returnsLegacyStacksWithoutAPlayerOwner() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.CARROT, 6),
                        false,
                        null
                )
        );

        List<CustomerPickupCounterBlockEntity.StoredStack> returned =
                CustomerPickupCounterBlockEntity.revalidateStoredStacks(
                        List.of(counter),
                        List.of()
                );

        assertEquals(1, returned.size());
        assertEquals(6, returned.getFirst().stack().getCount());
        assertEquals(null, returned.getFirst().crafterId());
        assertTrue(counter.getDisplayItems().isEmpty());
    }
    @Test
    void revalidatesOnceEverySecond() {
        assertTrue(
                CustomerPickupCounterBlockEntity.shouldRevalidate(20)
        );
        assertTrue(
                CustomerPickupCounterBlockEntity.shouldRevalidate(40)
        );
        assertFalse(
                CustomerPickupCounterBlockEntity.shouldRevalidate(39)
        );
    }

    @Test
    void usesOnlyTheLowestConnectedCounterPositionAsLeader() {
        CustomerPickupCounterBlockEntity first =
                mock(CustomerPickupCounterBlockEntity.class);
        CustomerPickupCounterBlockEntity second =
                mock(CustomerPickupCounterBlockEntity.class);
        when(first.getBlockPos()).thenReturn(new BlockPos(4, 2, 8));
        when(second.getBlockPos()).thenReturn(new BlockPos(3, 2, 8));

        assertFalse(
                CustomerPickupCounterBlockEntity.isRevalidationLeader(
                        List.of(first, second),
                        first.getBlockPos()
                )
        );
        assertTrue(
                CustomerPickupCounterBlockEntity.isRevalidationLeader(
                        List.of(first, second),
                        second.getBlockPos()
                )
        );
    }

    @Test
    void returnsObsoleteItemsToThePlayerInventory() {
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        ItemStack returned = new ItemStack(Items.BREAD, 10);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.add(any(ItemStack.class))).thenAnswer(invocation -> {
            invocation.<ItemStack>getArgument(0).setCount(0);
            return true;
        });

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.returnToPlayer(
                        player,
                        returned
                );

        assertTrue(remainder.isEmpty());
    }

    @Test
    void returnsOnlyTheInventoryRemainderForDropping() {
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        ItemStack returned = new ItemStack(Items.BREAD, 10);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.add(any(ItemStack.class))).thenAnswer(invocation -> {
            invocation.<ItemStack>getArgument(0).shrink(6);
            return false;
        });

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.returnToPlayer(
                        player,
                        returned
                );

        assertEquals(4, remainder.getCount());
        verify(player).displayClientMessage(
                Component.translatable(
                        "messages.customers.pickup_counter.items_returned"
                ),
                true
        );
    }

    @Test
    void returnsTheCompleteStackWhenThePlayerIsOffline() {
        ItemStack returned = new ItemStack(Items.BREAD, 10);

        ItemStack remainder =
                CustomerPickupCounterBlockEntity.returnToPlayer(
                        null,
                        returned
                );

        assertEquals(10, remainder.getCount());
    }
    @Test
    void dropsReturnedItemsInTheCenterOfTheBlockAboveTheCounter() {
        Vec3 dropPosition =
                CustomerPickupCounterBlockEntity
                        .getReturnedItemDropPosition(
                                new BlockPos(4, 10, 7)
                        );

        assertEquals(4.5D, dropPosition.x);
        assertEquals(11.5D, dropPosition.y);
        assertEquals(7.5D, dropPosition.z);
    }
    @Test
    void takesAnExactOfferFromAFullStoredStack() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID crafterId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.COOKIE, 5), true, crafterId));

        CustomerPickupCounterBlockEntity.StoredStack taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStack(
                        List.of(counter), new ItemStack(Items.COOKIE, 5));

        assertEquals(5, taken.stack().getCount());
        assertEquals(crafterId, taken.crafterId());
        assertTrue(counter.getDisplayItems().isEmpty());
    }

    @Test
    void takesOnlyTheOfferCountFromALargerStoredStack() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID crafterId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.COOKIE, 12), true, crafterId));

        CustomerPickupCounterBlockEntity.StoredStack taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStack(
                        List.of(counter), new ItemStack(Items.COOKIE, 5));

        assertEquals(5, taken.stack().getCount());
        assertEquals(crafterId, taken.crafterId());
        CustomerPickupCounterBlockEntity.StoredStack remaining =
                counter.removeOldestStored();
        assertEquals(7, remaining.stack().getCount());
        assertEquals(crafterId, remaining.crafterId());
    }

    @Test
    void leavesAnInsufficientMatchingStackUntouched() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID crafterId = UUID.randomUUID();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.COOKIE, 4), true, crafterId));

        CustomerPickupCounterBlockEntity.StoredStack taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStack(
                        List.of(counter), new ItemStack(Items.COOKIE, 5));

        assertTrue(taken.stack().isEmpty());
        CustomerPickupCounterBlockEntity.StoredStack remaining =
                counter.removeOldestStored();
        assertEquals(4, remaining.stack().getCount());
        assertEquals(crafterId, remaining.crafterId());
    }

    @Test
    void skipsNonmatchingItemsAndSearchesConnectedCounters() {
        CustomerPickupCounterBlockEntity first = createCounter();
        CustomerPickupCounterBlockEntity second = createCounter();
        UUID appleCrafter = UUID.randomUUID();
        UUID cookieCrafter = UUID.randomUUID();
        first.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.APPLE, 10), true, appleCrafter));
        second.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.COOKIE, 8), true, cookieCrafter));

        CustomerPickupCounterBlockEntity.StoredStack taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStack(
                        List.of(first, second), new ItemStack(Items.COOKIE, 5));

        assertEquals(5, taken.stack().getCount());
        assertEquals(cookieCrafter, taken.crafterId());
        assertEquals(10, first.removeOldestStored().stack().getCount());
        assertEquals(3, second.removeOldestStored().stack().getCount());
    }

    @Test
    void doesNotTakeAnOwnerlessLegacyStack() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        new ItemStack(Items.COOKIE, 5), false, null));

        CustomerPickupCounterBlockEntity.StoredStack taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStack(
                        List.of(counter), new ItemStack(Items.COOKIE, 5));

        assertTrue(taken.stack().isEmpty());
        assertEquals(5, counter.removeOldestStored().stack().getCount());
    }
    /** Uses the offer predicate when selecting a stored component-bearing item. */
    @Test
    void takesStoredStackMatchingOfferComponents() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        UUID crafterId = UUID.randomUUID();
        ItemStack water =
                PotionContents.createItemStack(Items.POTION, Potions.WATER);
        ItemStack awkward =
                PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        awkward,
                        true,
                        crafterId
                )
        );
        counter.insertStoredStack(
                new CustomerPickupCounterBlockEntity.StoredStack(
                        water,
                        true,
                        crafterId
                )
        );
        ItemCost waterCost = new ItemCost(Items.POTION).withComponents(
                builder -> builder.expect(
                        DataComponents.POTION_CONTENTS,
                        water.get(DataComponents.POTION_CONTENTS)
                )
        );
        MerchantOffer offer = new MerchantOffer(
                waterCost,
                Optional.empty(),
                new ItemStack(Items.EMERALD),
                1,
                1,
                0.0F
        );

        CustomerPickupCounterBlockEntity.StoredStack taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStack(
                        List.of(counter),
                        offer
                );

        assertTrue(ItemStack.isSameItemSameComponents(water, taken.stack()));
        assertTrue(ItemStack.isSameItemSameComponents(
                awkward,
                counter.getDisplayItems().getFirst()
        ));
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
}
