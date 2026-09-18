package com.vikingkittens.mc.customers.advancements;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import com.vikingkittens.mc.customers.common.events.InternalEventHandler;
import com.vikingkittens.mc.customers.common.events.InternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.supplier.SupplierInternalEvents;

public final class CustomersAdvancementEvents {
    private CustomersAdvancementEvents() {}

    public static void initialize() {
        InternalEvents.register(CustomersAdvancementEvents.class);
    }

    @InternalEventHandler
    public static void onItemServed(CustomerInternalEvents.ItemServed event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            var itemServedStat = Stats.CUSTOM.get(CustomersStatistics.ITEM_SERVED.get(), StatFormatter.DEFAULT);
            var petItemServedStat =
                    Stats.CUSTOM.get(CustomersStatistics.PET_ITEM_SERVED.get(), StatFormatter.DEFAULT);
            player.awardStat(itemServedStat, 1);
            if (event.isPetItem()) {
                player.awardStat(petItemServedStat, 1);
            }
            int totalItemsServed = player.getStats().getValue(itemServedStat);
            int totalPetItemsServed = player.getStats().getValue(petItemServedStat);

            CustomersTriggers.ITEM_SERVED.get().trigger(
                    player,
                    event,
                    totalItemsServed,
                    totalPetItemsServed
            );
        }
    }

    @InternalEventHandler
    public static void onCustomerServed(CustomerInternalEvents.CustomerServed event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            var customerServedStat = Stats.CUSTOM.get(CustomersStatistics.CUSTOMER_SERVED.get(), StatFormatter.DEFAULT);
            var customerCasualServedStat = Stats.CUSTOM.get(CustomersStatistics.CUSTOMER_CASUAL_SERVED.get(), StatFormatter.DEFAULT);
            var customerNormalServedStat = Stats.CUSTOM.get(CustomersStatistics.CUSTOMER_NORMAL_SERVED.get(), StatFormatter.DEFAULT);
            var customerImpatientServedStat = Stats.CUSTOM.get(CustomersStatistics.CUSTOMER_IMPATIENT_SERVED.get(), StatFormatter.DEFAULT);
            player.awardStat(customerServedStat, 1);
            switch (event.customerProfession().toString()) {
                case "customers:customer_casual":
                    player.awardStat(customerCasualServedStat, 1);
                    break;
                case "customers:customer":
                    player.awardStat(customerNormalServedStat, 1);
                    break;
                case "customers:customer_impatient":
                    player.awardStat(customerImpatientServedStat, 1);
                    break;
            }
            int totalCustomersServed = player.getStats().getValue(customerServedStat);
            int totalCustomersCasualServed = player.getStats().getValue(customerCasualServedStat);
            int totalCustomersNormalServed = player.getStats().getValue(customerNormalServedStat);
            int totalCustomersImpatientServed = player.getStats().getValue(customerImpatientServedStat);
            CustomersTriggers.CUSTOMER_SERVED.get().trigger(
                    player,
                    event,
                    totalCustomersServed,
                    totalCustomersCasualServed,
                    totalCustomersNormalServed,
                    totalCustomersImpatientServed
            );
        }
    }

    @InternalEventHandler
    public static void onShiftFinished(CustomerInternalEvents.ShiftFinished event) {
        var shiftFinishedStat = Stats.CUSTOM.get(CustomersStatistics.SHIFT_FINISHED.get(), StatFormatter.DEFAULT);
        List<UUID> playerIds = new ArrayList<>();
        playerIds.addAll(event.playerItemsCrafted().keySet());
        playerIds.addAll(event.playerItemsServed().keySet());
        for (UUID playerId : playerIds) {
            ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                player.awardStat(shiftFinishedStat, 1);
                int totalShiftsFinished = player.getStats().getValue(shiftFinishedStat);

                CustomersTriggers.SHIFT_FINISHED.get().trigger(player, event, totalShiftsFinished);
            }
        }
    }

    @InternalEventHandler
    public static void onLeaderboardChanged(CustomerInternalEvents.LeaderboardChanged event) {
        for (UUID playerId : event.affectedPlayerIds()) {
            ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                CustomersTriggers.LEADERBOARD_CHANGED.get().trigger(player, event);
            }
        }
    }

    @InternalEventHandler
    public static void onCustomerSpawnerChanged(CustomerInternalEvents.CustomerSpawnerConfigChanged event) {
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            CustomersTriggers.CUSTOMER_SPAWNER_CHANGED.get().trigger(player, event);
        }
    }

    @InternalEventHandler
    public static void onSupplierSpawnerChanged(SupplierInternalEvents.SupplierSpawnerConfigChanged event) {
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            CustomersTriggers.SUPPLIER_SPAWNER_CHANGED.get().trigger(player, event);
        }
    }

    @InternalEventHandler
    public static void onCounterBlockPlaced(CustomerInternalEvents.CounterBlockPlaced event) {
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            CustomersTriggers.COUNTER_PLACED.get().trigger(player, event);
        }
    }
}
