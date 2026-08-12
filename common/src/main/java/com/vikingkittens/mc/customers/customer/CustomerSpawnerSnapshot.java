package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public record CustomerSpawnerSnapshot(
        BlockPos spawnerPos,
        CustomerSpawnerMode spawnerMode,
        Optional<UUID> bossEventId,
        List<Customer> customers
) {
    public CustomerSpawnerSnapshot {
        Objects.requireNonNull(spawnerPos);
        Objects.requireNonNull(spawnerMode);
        bossEventId = Objects.requireNonNull(bossEventId);
        customers = List.copyOf(customers);
    }

    public record Customer(
            UUID customerId,
            Type type,
            List<ItemStack> offerCostItems,
            long ticksSinceTrade,
            long giveUpTicks
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
