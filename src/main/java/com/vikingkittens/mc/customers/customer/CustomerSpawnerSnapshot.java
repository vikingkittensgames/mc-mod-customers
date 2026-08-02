package com.vikingkittens.mc.customers.customer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public record CustomerSpawnerSnapshot(
        BlockPos spawnerPos,
        CustomerSpawnerMode spawnerMode,
        boolean specialEnabled,
        Optional<UUID> bossEventId,
        List<Customer> customers
) {
    public CustomerSpawnerSnapshot {
        Objects.requireNonNull(spawnerPos);
        Objects.requireNonNull(spawnerMode);
        bossEventId = Objects.requireNonNull(bossEventId);
        customers = List.copyOf(customers);
    }

    public static CustomerSpawnerSnapshot create(
            BlockPos spawnerPos,
            CustomerSpawnerMode spawnerMode,
            boolean specialEnabled,
            Optional<UUID> bossEventId,
            Collection<CustomerVillagerEntity> customers
    ) {
        return new CustomerSpawnerSnapshot(
                spawnerPos,
                spawnerMode,
                specialEnabled,
                bossEventId,
                customers.stream()
                        .map(CustomerSpawnerSnapshot::createCustomer)
                        .toList()
        );
    }

    private static Customer createCustomer(
            CustomerVillagerEntity customer
    ) {
        List<ItemStack> offerCostItems = customer.getOffers().stream()
                .filter(offer -> !offer.isOutOfStock())
                .map(offer -> offer.getCostA().copy())
                .toList();
        return new Customer(
                customer.getUUID(),
                customer.getSnapshotType(),
                offerCostItems
        );
    }

    public record Customer(
            UUID customerId,
            Type type,
            List<ItemStack> offerCostItems
    ) {
        public Customer {
            Objects.requireNonNull(customerId);
            Objects.requireNonNull(type);
            offerCostItems = List.copyOf(offerCostItems);
        }

        public enum Type {
            NORMAL,
            IMPATIENT,
            CASUAL
        }
    }
}
