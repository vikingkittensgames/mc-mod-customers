package com.vikingkittens.mc.customers.advancements.ftb;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

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
        ServerQuestFile.getInstance().ifPresent(file ->
                file.getTeamData(player).ifPresent(teamData ->
                        file.getAllTasks().stream()
                                .filter(CustomersFTBTasks.CustomersTask.class::isInstance)
                                .map(CustomersFTBTasks.CustomersTask.class::cast)
                                .forEach(task -> task.recordProgress(teamData, event))
                )
        );
    }
}
