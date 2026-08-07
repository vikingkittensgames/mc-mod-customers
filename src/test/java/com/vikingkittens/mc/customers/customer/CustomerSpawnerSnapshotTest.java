package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CustomerSpawnerSnapshotTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void customerSnapshotRetainsEveryOfferCostItem() {
        ItemStack first = mock(ItemStack.class);
        ItemStack second = mock(ItemStack.class);
        ItemStack third = mock(ItemStack.class);
        ItemStack fourth = mock(ItemStack.class);

        CustomerSpawnerSnapshot.Customer snapshot =
                new CustomerSpawnerSnapshot.Customer(
                UUID.randomUUID(),
                CustomerSpawnerSnapshot.Customer.Type.IMPATIENT,
                List.of(first, second, third, fourth),
                0,
                0
        );

        assertEquals(4, snapshot.offerCostItems().size());
        assertSame(first, snapshot.offerCostItems().getFirst());
        assertSame(fourth, snapshot.offerCostItems().getLast());
    }

    @Test
    void customerSnapshotCopiesItsOfferList() {
        List<ItemStack> mutableItems = new ArrayList<>();
        mutableItems.add(mock(ItemStack.class));

        CustomerSpawnerSnapshot.Customer snapshot =
                new CustomerSpawnerSnapshot.Customer(
                UUID.randomUUID(),
                CustomerSpawnerSnapshot.Customer.Type.NORMAL,
                mutableItems,
                0,
                0
        );
        mutableItems.clear();

        assertEquals(1, snapshot.offerCostItems().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.offerCostItems().clear()
        );
    }

    @Test
    void spawnerSnapshotCopiesItsCustomerList() {
        List<CustomerSpawnerSnapshot.Customer> mutableCustomers =
                new ArrayList<>();
        mutableCustomers.add(new CustomerSpawnerSnapshot.Customer(
                UUID.randomUUID(),
                CustomerSpawnerSnapshot.Customer.Type.CASUAL,
                List.of(),
                0,
                0
        ));

        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                new BlockPos(10, 64, -20),
                CustomerSpawnerMode.LUNCH,
                true,
                Optional.of(UUID.randomUUID()),
                mutableCustomers
        );
        mutableCustomers.clear();

        assertEquals(1, snapshot.customers().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.customers().clear()
        );
    }

    @Test
    void supportsSpawnerModesWithoutBossEvents() {
        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                BlockPos.ZERO,
                CustomerSpawnerMode.CONTINUOUS,
                false,
                Optional.empty(),
                List.of()
        );

        assertEquals(Optional.empty(), snapshot.bossEventId());
    }
}
