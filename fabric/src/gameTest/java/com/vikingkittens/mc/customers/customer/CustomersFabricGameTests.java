package com.vikingkittens.mc.customers.customer;

import java.util.List;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import com.vikingkittens.mc.customers.advancements.CustomersStatistics;
import com.vikingkittens.mc.customers.advancements.CustomersTriggers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.compatability.ItemInsertionTarget;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSeat;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.fabric.CustomerPickupCounterFabricStorage;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersFabricGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void registersCustomersContent(GameTestHelper helper) {
        CustomersGameTestAssertions.assertCustomersContentRegistered(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void createsTradeRemainders(GameTestHelper helper) {
        CustomersGameTestAssertions.assertTradeRemainders(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void customersCanSit(GameTestHelper helper) {
        CustomersGameTestAssertions.assertCustomersCanSit(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void commitsOnlyCompletedFabricTransactions(GameTestHelper helper) {
        RecordingInsertionTarget target = new RecordingInsertionTarget();
        CustomerPickupCounterFabricStorage storage =
                new CustomerPickupCounterFabricStorage(target);
        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(ItemVariant.of(Items.BREAD), 4, transaction);
        }
        if (target.committedItems != 0) {
            helper.fail("Aborted Fabric transaction inserted pickup items");
            return;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(ItemVariant.of(Items.BREAD), 4, transaction);
            transaction.commit();
        }
        if (target.committedItems != 4) {
            helper.fail("Committed Fabric transaction did not insert pickup items");
            return;
        }
        helper.succeed();
    }

    private static final class RecordingInsertionTarget
            implements ItemInsertionTarget {
        private int committedItems;

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return insertAll(List.of(stack), simulate).get(0);
        }

        @Override
        public List<ItemStack> insertAll(
                List<ItemStack> stacks,
                boolean simulate
        ) {
            if (!simulate) {
                committedItems += stacks.stream()
                        .mapToInt(ItemStack::getCount)
                        .sum();
            }
            return stacks.stream()
                    .map(stack -> ItemStack.EMPTY)
                    .toList();
        }

        @Override
        public boolean accepts(ItemStack stack) {
            return !stack.isEmpty();
        }
    }
}
