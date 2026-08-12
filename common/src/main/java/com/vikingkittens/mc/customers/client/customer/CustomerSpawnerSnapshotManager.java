package com.vikingkittens.mc.customers.client.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot;

public final class CustomerSpawnerSnapshotManager {
    private static final Map<BlockPos, CustomerSpawnerSnapshot> BY_SPAWNER =
            new HashMap<>();
    private static final Map<UUID, CustomerSpawnerSnapshot.Customer> BY_CUSTOMER =
            new HashMap<>();
    private static final Map<UUID, CustomerSpawnerSnapshot> BY_BOSS_EVENT =
            new HashMap<>();

    private CustomerSpawnerSnapshotManager() {
    }

    public static void replace(CustomerSpawnerSnapshot snapshot) {
        remove(snapshot.spawnerPos());
        BY_SPAWNER.put(snapshot.spawnerPos(), snapshot);
        for (CustomerSpawnerSnapshot.Customer customer : snapshot.customers()) {
            BY_CUSTOMER.put(customer.customerId(), customer);
        }
        snapshot.bossEventId().ifPresent(
                bossEventId -> BY_BOSS_EVENT.put(bossEventId, snapshot)
        );
    }

    public static void remove(BlockPos spawnerPos) {
        CustomerSpawnerSnapshot removed = BY_SPAWNER.remove(spawnerPos);
        if (removed == null) {
            return;
        }
        for (CustomerSpawnerSnapshot.Customer customer : removed.customers()) {
            BY_CUSTOMER.remove(customer.customerId());
        }
        removed.bossEventId().ifPresent(BY_BOSS_EVENT::remove);
    }

    public static Optional<CustomerSpawnerSnapshot> findBySpawner(
            BlockPos spawnerPos
    ) {
        return Optional.ofNullable(BY_SPAWNER.get(spawnerPos));
    }

    public static Optional<CustomerSpawnerSnapshot.Customer> findByCustomer(
            UUID customerId
    ) {
        return Optional.ofNullable(BY_CUSTOMER.get(customerId));
    }

    public static List<ItemStack> findOfferCostItems(
            UUID customerId,
            int maximumItems
    ) {
        if (maximumItems <= 0) {
            return List.of();
        }
        return findByCustomer(customerId)
                .map(customer -> customer.offerCostItems()
                        .stream()
                        .limit(maximumItems)
                        .toList())
                .orElseGet(List::of);
    }
    public static Optional<CustomerSpawnerSnapshot> findByBossEvent(
            UUID bossEventId
    ) {
        return Optional.ofNullable(BY_BOSS_EVENT.get(bossEventId));
    }

    public static void clear() {
        BY_SPAWNER.clear();
        BY_CUSTOMER.clear();
        BY_BOSS_EVENT.clear();
    }
}
