package com.vikingkittens.mc.customers.advancements.ftb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;

import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.customer.CustomerEvents;
import com.vikingkittens.mc.customers.supplier.SupplierEvents;

public final class CustomersFTBEvents {
    private static boolean initialized;

    private CustomersFTBEvents() {}

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        CustomerEvents.ITEM_SERVED.register(CustomersFTBEvents::onItemServed);
        CustomerEvents.CUSTOMER_SERVED.register(CustomersFTBEvents::onCustomerServed);
        CustomerEvents.SHIFT_FINISHED.register(CustomersFTBEvents::onShiftFinished);
        CustomerEvents.LEADERBOARD_CHANGED.register(CustomersFTBEvents::onLeaderboardChanged);
        SupplierEvents.SUPPLIES_PURCHASED.register(CustomersFTBEvents::onSuppliesPurchased);
        CustomerEvents.CUSTOMER_SPAWNER_CONFIG_CHANGED.register(CustomersFTBEvents::onCustomerSpawnerChanged);
        CustomerEvents.COUNTER_BLOCK_PLACED.register(CustomersFTBEvents::onCounterBlockPlaced);
        SupplierEvents.SUPPLIER_SPAWNER_CONFIG_CHANGED.register(CustomersFTBEvents::onSupplierSpawnerChanged);
    }

    public static void onItemServed(CustomerEvents.ItemServed event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player == null) {
            return;
        }
        recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
    }

    public static void onCustomerServed(CustomerEvents.CustomerServed event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
        }
    }

    public static void onShiftFinished(CustomerEvents.ShiftFinished event) {
        Set<UUID> playerIds = new HashSet<>(event.playerItemsCrafted().keySet());
        playerIds.addAll(event.playerItemsServed().keySet());

        ServerQuestFile.getInstance().ifPresent(file -> {
            Set<UUID> progressedTeams = new HashSet<>();
            for (UUID playerId : playerIds) {
                ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                file.getTeamData(player)
                        .filter(teamData -> progressedTeams.add(teamData.getTeamId()))
                        .ifPresent(teamData -> recordProgress(
                                file,
                                teamData,
                                (task, data) -> task.recordProgress(data, event)
                        ));
            }
        });
    }

    public static void onLeaderboardChanged(CustomerEvents.LeaderboardChanged event) {
        ServerQuestFile.getInstance().ifPresent(file -> {
            Map<TeamData, Set<UUID>> affectedPlayersByTeam = new HashMap<>();
            for (UUID playerId : event.affectedPlayerIds()) {
                ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                file.getTeamData(player)
                        .ifPresent(teamData -> affectedPlayersByTeam
                                .computeIfAbsent(teamData, ignored -> new HashSet<>())
                                .add(playerId));
            }
            affectedPlayersByTeam.forEach((teamData, playerIds) -> recordProgress(
                    file,
                    teamData,
                    (task, data) -> task.recordProgress(data, event, playerIds)
            ));
        });
    }

    public static void onCustomerSpawnerChanged(CustomerEvents.CustomerSpawnerConfigChanged event) {
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
        }
    }

    public static void onSupplierSpawnerChanged(SupplierEvents.SupplierSpawnerConfigChanged event) {
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
        }
    }

    public static void onSuppliesPurchased(SupplierEvents.SuppliesPurchased event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
        }
    }

    public static void onCounterBlockPlaced(CustomerEvents.CounterBlockPlaced event) {
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
        }
    }

    private static void recordProgress(
            ServerPlayer player,
            BiConsumer<CustomersFTBTasks.CustomersTask, TeamData> recorder
    ) {
        ServerQuestFile.getInstance().ifPresent(file ->
                file.getTeamData(player).ifPresent(teamData -> recordProgress(file, teamData, recorder))
        );
    }

    private static void recordProgress(
            ServerQuestFile file,
            TeamData teamData,
            BiConsumer<CustomersFTBTasks.CustomersTask, TeamData> recorder
    ) {
        file.getAllTasks().stream()
                .filter(CustomersFTBTasks.CustomersTask.class::isInstance)
                .map(CustomersFTBTasks.CustomersTask.class::cast)
                .forEach(task -> recorder.accept(task, teamData));
    }
}
