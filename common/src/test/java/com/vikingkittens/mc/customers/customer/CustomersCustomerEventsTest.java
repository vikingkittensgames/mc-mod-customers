package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersCustomerEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void interruptsInteractionWhenQuickSellCompletes() {
        Player player = mock(Player.class);
        Entity target = mock(Entity.class);

        assertTrue(CustomersCustomerEvents.onCustomerInteract(
                player,
                target,
                InteractionHand.MAIN_HAND,
                (actualPlayer, hand, actualTarget) -> {
                    assertSame(player, actualPlayer);
                    assertSame(InteractionHand.MAIN_HAND, hand);
                    assertSame(target, actualTarget);
                    return true;
                }
        ).isTrue());
    }

    @Test
    void passesInteractionWhenQuickSellDoesNotComplete() {
        assertFalse(CustomersCustomerEvents.onCustomerInteract(
                mock(Player.class),
                mock(Entity.class),
                InteractionHand.MAIN_HAND,
                (player, hand, target) -> false
        ).interruptsFurtherEvaluation());
    }
}
