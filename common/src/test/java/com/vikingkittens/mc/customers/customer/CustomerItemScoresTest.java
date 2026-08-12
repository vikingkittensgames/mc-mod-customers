package com.vikingkittens.mc.customers.customer;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerItemScoresTest {
    @Test
    void recordsPlayerAndOwnerlessAutomationScoresSeparately() {
        CustomerItemScores scores = new CustomerItemScores();
        UUID playerId = UUID.randomUUID();

        scores.add(playerId, 4);
        scores.add(playerId, 3);
        scores.add(null, 5);
        scores.add(null, 2);

        assertEquals(Map.of(playerId, 7), scores.playerScores());
        assertEquals(7, scores.automatedScore());
        assertEquals(14, scores.total());
    }

    @Test
    void ignoresNonpositiveScoreChanges() {
        CustomerItemScores scores = new CustomerItemScores();
        UUID playerId = UUID.randomUUID();

        scores.add(playerId, 0);
        scores.add(playerId, -1);
        scores.add(null, 0);
        scores.add(null, -1);

        assertEquals(Map.of(), scores.playerScores());
        assertEquals(0, scores.automatedScore());
        assertEquals(0, scores.total());
    }

    @Test
    void exposesAnImmutablePlayerScoreSnapshot() {
        CustomerItemScores scores = new CustomerItemScores();
        UUID playerId = UUID.randomUUID();
        scores.add(playerId, 3);

        Map<UUID, Integer> snapshot = scores.playerScores();
        scores.add(playerId, 2);

        assertEquals(Map.of(playerId, 3), snapshot);
        assertThrows(
                UnsupportedOperationException.class,
                () -> snapshot.put(UUID.randomUUID(), 1)
        );
    }

    @Test
    void clearsPlayerAndAutomationScores() {
        CustomerItemScores scores = new CustomerItemScores();
        scores.add(UUID.randomUUID(), 3);
        scores.add(null, 4);

        scores.clear();

        assertEquals(Map.of(), scores.playerScores());
        assertEquals(0, scores.automatedScore());
        assertEquals(0, scores.total());
    }
}
