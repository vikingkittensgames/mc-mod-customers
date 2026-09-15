package com.vikingkittens.mc.customers.advancements;

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
        var itemServedStat =
                Stats.CUSTOM.get(CustomersStatistics.ITEM_SERVED.get(), StatFormatter.DEFAULT);
        player.awardStat(itemServedStat, 1);
        int totalItemsServed = player.getStats().getValue(itemServedStat);
        CustomersTriggers.ITEM_SERVED.get().trigger(player, event, totalItemsServed);
    }
}
