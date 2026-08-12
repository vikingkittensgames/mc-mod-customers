package com.vikingkittens.mc.customers.customer;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerScoreboardVisibilityTest {
    private static final UUID PLAYER_ID = UUID.randomUUID();

    @Test
    void hidesScoreboardWhenNoPlayerCraftedOrServedItems() {
        assertFalse(CustomerSpawnerBlockEntity.shouldShowFinalScore(
                Map.of(),
                Map.of(),
                0,
                0
        ));
        assertFalse(CustomerSpawnerBlockEntity.shouldShowFinalScore(
                Map.of(PLAYER_ID, 0),
                Map.of(PLAYER_ID, 0),
                0,
                0
        ));
    }

    @Test
    void showsScoreboardWhenAPlayerServedAnItem() {
        assertTrue(CustomerSpawnerBlockEntity.shouldShowFinalScore(
                Map.of(PLAYER_ID, 1),
                Map.of(),
                0,
                0
        ));
    }

    @Test
    void showsScoreboardWhenAPlayerCraftedAnItem() {
        assertTrue(CustomerSpawnerBlockEntity.shouldShowFinalScore(
                Map.of(),
                Map.of(PLAYER_ID, 1),
                0,
                0
        ));
    }

    @Test
    void showsScoreboardWhenAutomationServedAnItem() {
        assertTrue(CustomerSpawnerBlockEntity.shouldShowFinalScore(
                Map.of(),
                Map.of(),
                1,
                0
        ));
    }

    @Test
    void showsScoreboardWhenAutomationCraftedAnItem() {
        assertTrue(CustomerSpawnerBlockEntity.shouldShowFinalScore(
                Map.of(),
                Map.of(),
                0,
                1
        ));
    }
}
