package com.vikingkittens.mc.customers.customer;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class CustomersCustomerEvents {
    private CustomersCustomerEvents() {}

    public static void initialize() {
        InteractionEvent.INTERACT_ENTITY.register((player, target, hand) ->
                onCustomerInteract(player, target, hand, CustomerInteractions::tryQuickSell));
    }

    static EventResult onCustomerInteract(
            Player player,
            Entity target,
            InteractionHand hand,
            CustomerInteraction interaction
    ) {
        return interaction.tryQuickSell(player, hand, target)
                ? EventResult.interruptTrue()
                : EventResult.pass();
    }

    @FunctionalInterface
    interface CustomerInteraction {
        boolean tryQuickSell(Player player, InteractionHand hand, Entity target);
    }
}
