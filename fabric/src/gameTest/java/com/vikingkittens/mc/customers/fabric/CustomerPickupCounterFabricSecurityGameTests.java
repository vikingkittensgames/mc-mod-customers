package com.vikingkittens.mc.customers.fabric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.vikingkittens.mc.customers.compatability.ItemInsertionTarget;

public final class CustomerPickupCounterFabricSecurityGameTests
        implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void overlappingTransactionsCannotReserveTheSameDemand(
            GameTestHelper helper
    ) {
        CapacityInsertionTarget target =
                new CapacityInsertionTarget(4);
        CustomerPickupCounterFabricStorage storage =
                new CustomerPickupCounterFabricStorage(target);

        try (
                Transaction first = Transaction.openOuter();
                Transaction second = Transaction.openOuter()
        ) {
            if (storage.insert(
                    ItemVariant.of(Items.BREAD),
                    4,
                    first
            ) != 4) {
                helper.fail("First transaction did not reserve demand");
                return;
            }
            if (storage.insert(
                    ItemVariant.of(Items.BREAD),
                    4,
                    second
            ) != 0) {
                helper.fail("Second transaction overbooked demand");
                return;
            }
            second.commit();
            first.commit();
        }

        if (target.committedItems() != 4) {
            helper.fail("Transactions committed duplicate items");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void abortingATransactionReleasesItsReservation(
            GameTestHelper helper
    ) {
        CapacityInsertionTarget target =
                new CapacityInsertionTarget(4);
        CustomerPickupCounterFabricStorage storage =
                new CustomerPickupCounterFabricStorage(target);

        try (Transaction transaction = Transaction.openOuter()) {
            if (storage.insert(
                    ItemVariant.of(Items.BREAD),
                    4,
                    transaction
            ) != 4) {
                helper.fail("Initial transaction did not reserve demand");
                return;
            }
        }
        try (Transaction transaction = Transaction.openOuter()) {
            if (storage.insert(
                    ItemVariant.of(Items.BREAD),
                    4,
                    transaction
            ) != 4) {
                helper.fail("Aborted transaction retained its reservation");
                return;
            }
            transaction.commit();
        }

        if (target.committedItems() != 4) {
            helper.fail("Replacement transaction did not commit");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void differentCounterTargetsCannotReserveSharedDemand(
            GameTestHelper helper
    ) {
        AtomicInteger committedItems = new AtomicInteger();
        CustomerPickupCounterFabricStorage firstStorage =
                new CustomerPickupCounterFabricStorage(
                        new CapacityInsertionTarget(4, committedItems)
                );
        CustomerPickupCounterFabricStorage secondStorage =
                new CustomerPickupCounterFabricStorage(
                        new CapacityInsertionTarget(4, committedItems)
                );

        try (Transaction transaction = Transaction.openOuter()) {
            if (firstStorage.insert(
                    ItemVariant.of(Items.BREAD),
                    4,
                    transaction
            ) != 4) {
                helper.fail("First counter did not reserve shared demand");
                return;
            }
            if (secondStorage.insert(
                    ItemVariant.of(Items.BREAD),
                    4,
                    transaction
            ) != 0) {
                helper.fail("Second counter overbooked shared demand");
                return;
            }
            transaction.commit();
        }

        if (committedItems.get() != 4) {
            helper.fail("Connected counters committed duplicate items");
            return;
        }
        helper.succeed();
    }

    private static final class CapacityInsertionTarget
            implements ItemInsertionTarget {
        private final int capacity;
        private final AtomicInteger committedItems;

        private CapacityInsertionTarget(int capacity) {
            this(capacity, new AtomicInteger());
        }

        private CapacityInsertionTarget(
                int capacity,
                AtomicInteger committedItems
        ) {
            this.capacity = capacity;
            this.committedItems = committedItems;
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return insertAll(List.of(stack), simulate).getFirst();
        }

        @Override
        public List<ItemStack> insertAll(
                List<ItemStack> stacks,
                boolean simulate
        ) {
            int available = capacity - committedItems.get();
            int inserted = 0;
            List<ItemStack> remainders =
                    new ArrayList<>(stacks.size());
            for (ItemStack stack : stacks) {
                int accepted = Math.min(available, stack.getCount());
                available -= accepted;
                inserted += accepted;
                remainders.add(
                        accepted == stack.getCount()
                                ? ItemStack.EMPTY
                                : stack.copyWithCount(
                                        stack.getCount() - accepted
                                )
                );
            }
            if (!simulate) {
                committedItems.addAndGet(inserted);
            }
            return List.copyOf(remainders);
        }

        private int committedItems() {
            return committedItems.get();
        }

        @Override
        public boolean accepts(ItemStack stack) {
            return !stack.isEmpty();
        }
    }
}
