package com.vikingkittens.mc.customers.customer;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerLeaderboardScoresTest {
    @Test
    void storesScoresBySpawnerModeLevelAndPlayer() {
        CustomerLeaderboardScores scores = new CustomerLeaderboardScores();
        CustomerLeaderboardScores.Key key = new CustomerLeaderboardScores.Key(
                new BlockPos(10, 64, -5),
                CustomerSpawnerMode.BREAKFAST,
                2,
                new UUID(0L, 1L)
        );

        scores.add(key, 0.5F);

        assertEquals(0.5F, scores.get(key));
    }

    @Test
    void replacesThePreviousScoreForTheSameKeyOnlyWhenHigher() {
        CustomerLeaderboardScores scores = new CustomerLeaderboardScores();
        CustomerLeaderboardScores.Key key = new CustomerLeaderboardScores.Key(
                new BlockPos(10, 64, -5),
                CustomerSpawnerMode.LUNCH,
                1,
                new UUID(0L, 2L)
        );

        scores.add(key, 0.2F);
        scores.add(key, 0.9F);
        scores.add(key, 0.5F);

        assertEquals(0.9F, scores.get(key));
    }

    @Test
    void clampsScoresToTheFiveStarRange() {
        CustomerLeaderboardScores scores = new CustomerLeaderboardScores();
        CustomerLeaderboardScores.Key lowKey = new CustomerLeaderboardScores.Key(
                BlockPos.ZERO,
                CustomerSpawnerMode.DINNER,
                1,
                new UUID(0L, 3L)
        );
        CustomerLeaderboardScores.Key highKey = new CustomerLeaderboardScores.Key(
                BlockPos.ZERO,
                CustomerSpawnerMode.DINNER,
                1,
                new UUID(0L, 4L)
        );

        scores.add(lowKey, -1.0F);
        scores.add(highKey, 2.0F);

        assertEquals(0.0F, scores.get(lowKey));
        assertEquals(1.0F, scores.get(highKey));
    }

    @Test
    void returnsNegativeOneWhenNoScoreExistsForTheKey() {
        CustomerLeaderboardScores scores = new CustomerLeaderboardScores();

        assertEquals(-1.0F, scores.get(new CustomerLeaderboardScores.Key(
                BlockPos.ZERO,
                CustomerSpawnerMode.DINNER,
                1,
                new UUID(0L, 5L)
        )));
    }
}
