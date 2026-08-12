package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPayloadClientHandlersTest {
    @AfterEach
    void clearSnapshots() {
        CustomerSpawnerSnapshotManager.clear();
    }

    @Test
    void replacesAndRemovesSpawnerSnapshot() {
        BlockPos position = new BlockPos(10, 64, 20);
        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                position,
                CustomerSpawnerMode.DINNER,
                Optional.empty(),
                List.of()
        );

        CustomerPayloadClientHandlers.updateSpawnerSnapshot(
                new CustomerSpawnerSnapshotPayload(position, Optional.of(snapshot))
        );

        assertEquals(Optional.of(snapshot), CustomerSpawnerSnapshotManager.findBySpawner(position));

        CustomerPayloadClientHandlers.updateSpawnerSnapshot(
                new CustomerSpawnerSnapshotPayload(position, Optional.empty())
        );

        assertTrue(CustomerSpawnerSnapshotManager.findBySpawner(position).isEmpty());
    }
}
