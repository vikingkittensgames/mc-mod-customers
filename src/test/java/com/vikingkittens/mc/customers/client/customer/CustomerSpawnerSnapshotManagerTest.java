package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomerSpawnerSnapshotManagerTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @AfterEach
    void clearSnapshots() {
        CustomerSpawnerSnapshotManager.clear();
    }

    @Test
    void indexesSnapshotBySpawnerCustomerAndBossEvent() {
        BlockPos spawnerPos = new BlockPos(10, 64, -20);
        UUID customerId = UUID.randomUUID();
        UUID bossEventId = UUID.randomUUID();
        CustomerSpawnerSnapshot.Customer customer =
                new CustomerSpawnerSnapshot.Customer(
                customerId,
                CustomerSpawnerSnapshot.Customer.Type.NORMAL,
                List.of()
        );
        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                spawnerPos,
                CustomerSpawnerMode.DAY,
                false,
                Optional.of(bossEventId),
                List.of(customer)
        );

        CustomerSpawnerSnapshotManager.replace(snapshot);

        assertEquals(
                Optional.of(snapshot),
                CustomerSpawnerSnapshotManager.findBySpawner(spawnerPos)
        );
        assertEquals(
                Optional.of(customer),
                CustomerSpawnerSnapshotManager.findByCustomer(customerId)
        );
        assertEquals(
                Optional.of(snapshot),
                CustomerSpawnerSnapshotManager.findByBossEvent(bossEventId)
        );
    }

    @Test
    void replacingSnapshotRemovesItsObsoleteIndexes() {
        BlockPos spawnerPos = BlockPos.ZERO;
        UUID oldCustomerId = UUID.randomUUID();
        UUID oldBossEventId = UUID.randomUUID();
        CustomerSpawnerSnapshot oldSnapshot = new CustomerSpawnerSnapshot(
                spawnerPos,
                CustomerSpawnerMode.BREAKFAST,
                false,
                Optional.of(oldBossEventId),
                List.of(new CustomerSpawnerSnapshot.Customer(
                        oldCustomerId,
                        CustomerSpawnerSnapshot.Customer.Type.IMPATIENT,
                        List.of()
                ))
        );
        UUID newCustomerId = UUID.randomUUID();
        CustomerSpawnerSnapshot newSnapshot = new CustomerSpawnerSnapshot(
                spawnerPos,
                CustomerSpawnerMode.CONTINUOUS,
                true,
                Optional.empty(),
                List.of(new CustomerSpawnerSnapshot.Customer(
                        newCustomerId,
                        CustomerSpawnerSnapshot.Customer.Type.CASUAL,
                        List.of()
                ))
        );

        CustomerSpawnerSnapshotManager.replace(oldSnapshot);
        CustomerSpawnerSnapshotManager.replace(newSnapshot);

        assertEquals(
                Optional.empty(),
                CustomerSpawnerSnapshotManager.findByCustomer(oldCustomerId)
        );
        assertEquals(
                Optional.empty(),
                CustomerSpawnerSnapshotManager.findByBossEvent(oldBossEventId)
        );
        assertEquals(
                Optional.of(newSnapshot),
                CustomerSpawnerSnapshotManager.findBySpawner(spawnerPos)
        );
        assertEquals(
                Optional.of(newSnapshot.customers().getFirst()),
                CustomerSpawnerSnapshotManager.findByCustomer(newCustomerId)
        );
    }

    @Test
    void removingSpawnerClearsEveryRelatedIndex() {
        BlockPos spawnerPos = BlockPos.ZERO;
        UUID customerId = UUID.randomUUID();
        UUID bossEventId = UUID.randomUUID();
        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                spawnerPos,
                CustomerSpawnerMode.DINNER,
                true,
                Optional.of(bossEventId),
                List.of(new CustomerSpawnerSnapshot.Customer(
                        customerId,
                        CustomerSpawnerSnapshot.Customer.Type.NORMAL,
                        List.of()
                ))
        );
        CustomerSpawnerSnapshotManager.replace(snapshot);

        CustomerSpawnerSnapshotManager.remove(spawnerPos);

        assertEquals(
                Optional.empty(),
                CustomerSpawnerSnapshotManager.findBySpawner(spawnerPos)
        );
        assertEquals(
                Optional.empty(),
                CustomerSpawnerSnapshotManager.findByCustomer(customerId)
        );
        assertEquals(
                Optional.empty(),
                CustomerSpawnerSnapshotManager.findByBossEvent(bossEventId)
        );
    }
    @Test
    void returnsOnlyTheRequestedNumberOfOverheadItems() {
        UUID customerId = UUID.randomUUID();
        ItemStack first = mock(ItemStack.class);
        ItemStack second = mock(ItemStack.class);
        ItemStack third = mock(ItemStack.class);
        ItemStack fourth = mock(ItemStack.class);
        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                BlockPos.ZERO,
                CustomerSpawnerMode.DAY,
                false,
                Optional.empty(),
                List.of(new CustomerSpawnerSnapshot.Customer(
                        customerId,
                        CustomerSpawnerSnapshot.Customer.Type.NORMAL,
                        List.of(first, second, third, fourth)
                ))
        );
        CustomerSpawnerSnapshotManager.replace(snapshot);

        assertEquals(
                List.of(first, second, third),
                CustomerSpawnerSnapshotManager.findOfferCostItems(
                        customerId,
                        3
                )
        );
    }
}
