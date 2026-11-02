package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerItemConservationSecurityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void takingAnOfferAcrossCountersNeitherDuplicatesNorDeletesItems() {
        CustomerPickupCounterBlockEntity first = createCounter();
        CustomerPickupCounterBlockEntity second = createCounter();
        first.insertStack(new ItemStack(Items.COOKIE, 2));
        second.insertStack(new ItemStack(Items.COOKIE, 3));

        List<CustomerPickupCounterBlockEntity.StoredStack> taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStacks(
                        List.of(first, second),
                        offer(4)
                );

        assertEquals(4, count(taken));
        assertEquals(1, count(first, second));
        assertEquals(5, count(taken) + count(first, second));
    }

    @Test
    void anIncompleteOfferDoesNotConsumeAnyCounterItems() {
        CustomerPickupCounterBlockEntity first = createCounter();
        CustomerPickupCounterBlockEntity second = createCounter();
        first.insertStack(new ItemStack(Items.COOKIE, 1));
        second.insertStack(new ItemStack(Items.COOKIE, 2));

        List<CustomerPickupCounterBlockEntity.StoredStack> taken =
                CustomerPickupCounterBlockEntity.takeMatchingStoredStacks(
                        List.of(first, second),
                        offer(4)
                );

        assertTrue(taken.isEmpty());
        assertEquals(3, count(first, second));
    }

    @Test
    void revalidationReturnsExactlyTheUnwantedExcess() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        counter.insertStack(new ItemStack(Items.COOKIE, 5));
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);
        MerchantOffers offers = new MerchantOffers();
        offers.add(offer(3));
        when(spawner.getActiveCustomers()).thenReturn(List.of(customer));
        when(customer.getOffers()).thenReturn(offers);

        List<CustomerPickupCounterBlockEntity.StoredStack> returned =
                CustomerPickupCounterBlockEntity.revalidateStoredStacks(
                        List.of(counter),
                        List.of(spawner)
                );

        assertEquals(2, count(returned));
        assertEquals(3, count(counter));
        assertEquals(5, count(returned) + count(counter));
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

    private static MerchantOffer offer(int count) {
        return new MerchantOffer(
                new ItemCost(Items.COOKIE, count),
                Optional.empty(),
                new ItemStack(Items.EMERALD),
                1,
                1,
                0.0F
        );
    }

    private static int count(
            CustomerPickupCounterBlockEntity... counters
    ) {
        int count = 0;
        for (CustomerPickupCounterBlockEntity counter : counters) {
            count += counter.getDisplayItems().stream()
                    .mapToInt(ItemStack::getCount)
                    .sum();
        }
        return count;
    }

    private static int count(
            List<CustomerPickupCounterBlockEntity.StoredStack> stacks
    ) {
        return stacks.stream()
                .map(CustomerPickupCounterBlockEntity.StoredStack::stack)
                .mapToInt(ItemStack::getCount)
                .sum();
    }
}
