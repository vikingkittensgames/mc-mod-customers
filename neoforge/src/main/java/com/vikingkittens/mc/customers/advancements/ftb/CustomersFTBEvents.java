package com.vikingkittens.mc.customers.advancements.ftb;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;

import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.common.events.InternalEventHandler;
import com.vikingkittens.mc.customers.common.events.InternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;

public final class CustomersFTBEvents {
    private CustomersFTBEvents() {}

    public static void initialize() {
        InternalEvents.register(CustomersFTBEvents.class);
    }

    @InternalEventHandler
    public static void onItemServed(CustomerInternalEvents.ItemServed event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player == null) {
            return;
        }
        recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
    }

    @InternalEventHandler
    public static void onCustomerServed(CustomerInternalEvents.CustomerServed event) {
        if (event.playerId() == null) {
            return;
        }
        ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(event.playerId());
        if (player != null) {
            recordProgress(player, (task, teamData) -> task.recordProgress(teamData, event));
        }
    }

    @InternalEventHandler
    public static void onShiftFinished(CustomerInternalEvents.ShiftFinished event) {
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
