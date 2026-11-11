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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerPickupCounterDisplayCaseTransferSecurityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void conservesItemsAcrossMultipleDisplayCasesAndTransfers() {
        CustomerPickupCounterBlockEntity counter = createCounter();
        CustomersDisplayCaseBlockEntity first = createDisplayCase(
                BlockPos.ZERO.north()
        );
        CustomersDisplayCaseBlockEntity second = createDisplayCase(
                BlockPos.ZERO.south()
        );
        first.setItem(0, new ItemStack(Items.COOKIE, 4));
        second.setItem(0, new ItemStack(Items.COOKIE, 4));
        CustomerSpawnerBlockEntity spawner = spawnerWantingCookies(5);

        CustomerPickupCounterBlockEntity.transferDisplayCaseSlot(
                List.of(counter),
                List.of(spawner),
                first,
                0
        );
        CustomerPickupCounterBlockEntity.transferDisplayCaseSlot(
                List.of(counter),
                List.of(spawner),
                second,
                0
        );
        CustomerPickupCounterBlockEntity.transferDisplayCaseSlot(
                List.of(counter),
                List.of(spawner),
                second,
                0
        );

        int remaining = first.getItem(0).getCount()
                + second.getItem(0).getCount();
        int transferred = counter.getDisplayItems().stream()
                .mapToInt(ItemStack::getCount)
                .sum();
        assertEquals(8, remaining + transferred);
        assertEquals(5, transferred);
        assertEquals(3, remaining);
    }

    private static CustomerSpawnerBlockEntity spawnerWantingCookies(
            int count
    ) {
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);
        MerchantOffers offers = new MerchantOffers();
        offers.add(new MerchantOffer(
                new ItemCost(Items.COOKIE, count),
                Optional.empty(),
                new ItemStack(Items.EMERALD),
                1,
                1,
                0.0F
        ));
        when(spawner.getActiveCustomers()).thenReturn(List.of(customer));
        when(customer.getOffers()).thenReturn(offers);
        return spawner;
    }

    @SuppressWarnings("unchecked")
    private static CustomerPickupCounterBlockEntity createCounter() {
        BlockEntityType<CustomerPickupCounterBlockEntity> type =
                mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomerPickupCounterBlockEntity(
                type,
                BlockPos.ZERO,
                state
        );
    }

    @SuppressWarnings("unchecked")
    private static CustomersDisplayCaseBlockEntity createDisplayCase(
            BlockPos pos
    ) {
        BlockEntityType<CustomersDisplayCaseBlockEntity> type =
                mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomersDisplayCaseBlockEntity(type, pos, state);
    }
}
