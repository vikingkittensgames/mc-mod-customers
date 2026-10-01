package com.vikingkittens.mc.customers.fabric;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import com.vikingkittens.mc.customers.compatability.ItemInsertionTarget;

public final class CustomerPickupCounterFabricStorage
        implements Storage<ItemVariant> {
    private static final Map<
            TransactionContext,
            Map<ItemInsertionTarget, Reservation>
    > RESERVATIONS = new IdentityHashMap<>();

    private final ItemInsertionTarget target;

    public CustomerPickupCounterFabricStorage(ItemInsertionTarget target) {
        this.target = target;
    }

    @Override
    public long insert(
            ItemVariant resource,
            long maxAmount,
            TransactionContext transaction
    ) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int requestedCount = (int) Math.min(
                maxAmount,
                resource.getItem().getMaxStackSize()
        );
        Reservation reservation = reservation(transaction);
        List<net.minecraft.world.item.ItemStack> requestedStacks =
                new ArrayList<>(reservation.stacks());
        requestedStacks.add(resource.toStack(requestedCount));
        List<net.minecraft.world.item.ItemStack> remainders =
                target.insertAll(requestedStacks, true);
        net.minecraft.world.item.ItemStack remainder =
                remainders.get(remainders.size() - 1);
        int insertedCount = requestedCount - remainder.getCount();
        if (insertedCount == 0) {
            return 0;
        }
        reservation.reserve(resource.toStack(insertedCount), transaction);
        return insertedCount;
    }

    @Override
    public long extract(
            ItemVariant resource,
            long maxAmount,
            TransactionContext transaction
    ) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return List.<StorageView<ItemVariant>>of().iterator();
    }

    private Reservation reservation(TransactionContext transaction) {
        TransactionContext outerTransaction =
                transaction.getOpenTransaction(0);
        synchronized (RESERVATIONS) {
            Map<ItemInsertionTarget, Reservation> byTarget =
                    RESERVATIONS.computeIfAbsent(
                            outerTransaction,
                            ignored -> new IdentityHashMap<>()
                    );
            return byTarget.computeIfAbsent(
                    target,
                    ignored -> new Reservation(
                            target,
                            outerTransaction
                    )
            );
        }
    }

    private static final class Reservation
            extends SnapshotParticipant<
                    List<net.minecraft.world.item.ItemStack>
            > {
        private final ItemInsertionTarget target;
        private final TransactionContext outerTransaction;
        private final List<net.minecraft.world.item.ItemStack> stacks =
                new ArrayList<>();

        private Reservation(
                ItemInsertionTarget target,
                TransactionContext outerTransaction
        ) {
            this.target = target;
            this.outerTransaction = outerTransaction;
            outerTransaction.addOuterCloseCallback(
                    result -> removeReservation(outerTransaction, target)
            );
        }

        private List<net.minecraft.world.item.ItemStack> stacks() {
            return List.copyOf(stacks);
        }

        private void reserve(
                net.minecraft.world.item.ItemStack stack,
                TransactionContext transaction
        ) {
            updateSnapshots(transaction);
            stacks.add(stack.copy());
        }

        @Override
        protected List<net.minecraft.world.item.ItemStack> createSnapshot() {
            return stacks();
        }

        @Override
        protected void readSnapshot(
                List<net.minecraft.world.item.ItemStack> snapshot
        ) {
            stacks.clear();
            stacks.addAll(snapshot);
        }

        @Override
        protected void onFinalCommit() {
            List<net.minecraft.world.item.ItemStack> remainders =
                    target.insertAll(stacks(), false);
            if (remainders.stream().anyMatch(
                    stack -> !stack.isEmpty()
            )) {
                throw new IllegalStateException(
                        "Reserved pickup counter items were unavailable"
                );
            }
        }
    }

    private static void removeReservation(
            TransactionContext transaction,
            ItemInsertionTarget target
    ) {
        synchronized (RESERVATIONS) {
            Map<ItemInsertionTarget, Reservation> byTarget =
                    RESERVATIONS.get(transaction);
            if (byTarget == null) {
                return;
            }
            byTarget.remove(target);
            if (byTarget.isEmpty()) {
                RESERVATIONS.remove(transaction);
            }
        }
    }
}
