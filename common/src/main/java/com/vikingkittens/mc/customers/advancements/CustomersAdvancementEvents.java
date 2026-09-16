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
        if (player == null) {
            return;
        }
        var itemServedStat = Stats.CUSTOM.get(CustomersStatistics.ITEM_SERVED.get(), StatFormatter.DEFAULT);
        player.awardStat(itemServedStat, 1);
        int totalItemsServed = player.getStats().getValue(itemServedStat);
        CustomersTriggers.ITEM_SERVED.get().trigger(player, event, totalItemsServed);
    }

    @InternalEventHandler
    public static void onShiftFinished(CustomerInternalEvents.ShiftFinished event) {
        var shiftFinishedStat = Stats.CUSTOM.get(CustomersStatistics.SHIFT_FINISHED.get(), StatFormatter.DEFAULT);
        List<UUID> playerIds = new ArrayList<>();
        playerIds.addAll(event.playerItemsCrafted().keySet());
        playerIds.addAll(event.playerItemsServed().keySet());
        for (UUID playerId : playerIds) {
            ServerPlayer player = event.level().getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                return;
            }
            player.awardStat(shiftFinishedStat, 1);
            int totalShiftsFinished = player.getStats().getValue(shiftFinishedStat);
            CustomersTriggers.SHIFT_FINISHED.get().trigger(player, event, totalShiftsFinished);
        }
    }
}
