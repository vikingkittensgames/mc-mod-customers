package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerPickupCounterDisplayCaseTransferTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void transfersOnlyTheWantedCountAsAutomated() {
        CustomerPickupCounterBlockEntity counter = createCounter(
                BlockPos.ZERO
        );
        CustomersDisplayCaseBlockEntity displayCase = createDisplayCase(
                BlockPos.ZERO.north()
        );
        displayCase.setItem(0, new ItemStack(Items.COOKIE, 8));
        CustomerSpawnerBlockEntity spawner = spawnerWanting(
                Items.COOKIE,
                3
        );

        boolean transferred = CustomerPickupCounterBlockEntity
                .transferDisplayCaseSlot(
                        List.of(counter),
                        List.of(spawner),
                        displayCase,
                        0
                );

        assertTrue(transferred);
        assertEquals(5, displayCase.getItem(0).getCount());
        CustomerPickupCounterBlockEntity.StoredStack stored =
                counter.removeOldestStored();
        assertTrue(stored.stack().is(Items.COOKIE));
        assertEquals(3, stored.stack().getCount());
        assertEquals(null, stored.crafterId());
        verify(spawner).scoreboardAddItemsCrafted(null, 3);
    }

    @Test
    void leavesUnwantedItemsInTheDisplayCase() {
        CustomerPickupCounterBlockEntity counter = createCounter(
                BlockPos.ZERO
        );
        CustomersDisplayCaseBlockEntity displayCase = createDisplayCase(
                BlockPos.ZERO.north()
        );
        displayCase.setItem(0, new ItemStack(Items.APPLE, 8));
        CustomerSpawnerBlockEntity spawner = spawnerWanting(
                Items.COOKIE,
                3
        );

        boolean transferred = CustomerPickupCounterBlockEntity
                .transferDisplayCaseSlot(
                        List.of(counter),
                        List.of(spawner),
                        displayCase,
                        0
                );

        assertFalse(transferred);
        assertEquals(8, displayCase.getItem(0).getCount());
        assertTrue(counter.getDisplayItems().isEmpty());
    }

    @Test
    void doesNotExtractWhenTheCounterNetworkIsFull() {
        CustomerPickupCounterBlockEntity counter = createCounter(
                BlockPos.ZERO
        );
        for (int slot = 0;
                slot < CustomerPickupCounterBlockEntity.INVENTORY_SIZE;
                slot++) {
            counter.insertStack(new ItemStack(Items.APPLE, 64));
        }
        CustomersDisplayCaseBlockEntity displayCase = createDisplayCase(
                BlockPos.ZERO.north()
        );
        displayCase.setItem(0, new ItemStack(Items.COOKIE, 8));
        CustomerSpawnerBlockEntity spawner = spawnerWanting(
                Items.COOKIE,
                3
        );

        boolean transferred = CustomerPickupCounterBlockEntity
                .transferDisplayCaseSlot(
                        List.of(counter),
                        List.of(spawner),
                        displayCase,
                        0
                );

        assertFalse(transferred);
        assertEquals(8, displayCase.getItem(0).getCount());
    }

    @Test
    void findsOnlyDisplayCasesDirectlyBesideTheCounters() {
        CustomerPickupCounterBlockEntity counter = createCounter(
                BlockPos.ZERO
        );
        CustomersDisplayCaseBlockEntity north = createDisplayCase(
                BlockPos.ZERO.north()
        );
        CustomersDisplayCaseBlockEntity east = createDisplayCase(
                BlockPos.ZERO.east()
        );
        CustomersDisplayCaseBlockEntity above = createDisplayCase(
                BlockPos.ZERO.above()
        );
        CustomersDisplayCaseBlockEntity diagonal = createDisplayCase(
                BlockPos.ZERO.north().east()
        );
        Map<BlockPos, BlockEntity> blockEntities = Map.of(
                north.getBlockPos(),
                north,
                east.getBlockPos(),
                east,
                above.getBlockPos(),
                above,
                diagonal.getBlockPos(),
                diagonal
        );
        Level level = mock(Level.class);
        when(level.getBlockEntity(any(BlockPos.class))).thenAnswer(
                invocation -> blockEntities.get(invocation.getArgument(0))
        );

        List<CustomersDisplayCaseBlockEntity> found =
                CustomerPickupCounterBlockEntity.getAdjacentDisplayCases(
                        level,
                        List.of(counter)
                );

        assertEquals(2, found.size());
        assertTrue(found.contains(north));
        assertTrue(found.contains(east));
        assertFalse(found.contains(above));
        assertFalse(found.contains(diagonal));
    }

    private static CustomerSpawnerBlockEntity spawnerWanting(
            Item item,
            int count
    ) {
        CustomerSpawnerBlockEntity spawner =
                mock(CustomerSpawnerBlockEntity.class);
        CustomerVillagerEntity customer =
                mock(CustomerVillagerEntity.class);
        MerchantOffers offers = new MerchantOffers();
        offers.add(new MerchantOffer(
                new ItemCost(item, count),
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
    private static CustomerPickupCounterBlockEntity createCounter(
            BlockPos pos
    ) {
        BlockEntityType<CustomerPickupCounterBlockEntity> type =
                mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomerPickupCounterBlockEntity(type, pos, state);
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
