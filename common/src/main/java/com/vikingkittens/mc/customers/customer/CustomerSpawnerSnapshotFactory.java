package com.vikingkittens.mc.customers.customer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public final class CustomerSpawnerSnapshotFactory {
    private CustomerSpawnerSnapshotFactory() {}

    public static CustomerSpawnerSnapshot create(
            BlockPos spawnerPos,
            CustomerSpawnerMode spawnerMode,
            Optional<UUID> bossEventId,
            Collection<CustomerVillagerEntity> customers) {
        return new CustomerSpawnerSnapshot(
                spawnerPos,
                spawnerMode,
                bossEventId,
                customers.stream().map(CustomerSpawnerSnapshotFactory::createCustomer).toList());
    }

    private static CustomerSpawnerSnapshot.Customer createCustomer(CustomerVillagerEntity customer) {
        List<ItemStack> offerCostItems = customer.getOffers().stream()
                .filter(offer -> !offer.isOutOfStock())
                .map(offer -> offer.getCostA().copy())
                .toList();
        long giveUpTicks = customer.getGiveUpTicks();
        if (customer.getState() != CustomerState.BUYING && customer.getState() != CustomerState.GIVING_UP) {
            giveUpTicks = 0;
        }
        return new CustomerSpawnerSnapshot.Customer(
                customer.getUUID(),
                customer.getSnapshotType(),
                offerCostItems,
                customer.getTicksSinceTrade(),
                giveUpTicks);
    }
}
