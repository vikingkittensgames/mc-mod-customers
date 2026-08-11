package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public final class CustomerItemScores {
    private final Map<UUID, Integer> playerScores = new HashMap<>();
    private int automatedScore;

    public void add(@Nullable UUID playerId, int itemCount) {
        if (itemCount <= 0) {
            return;
        }
        if (playerId == null) {
            automatedScore += itemCount;
        } else {
            playerScores.merge(playerId, itemCount, Integer::sum);
        }
    }

    public Map<UUID, Integer> playerScores() {
        return Map.copyOf(playerScores);
    }

    public int automatedScore() {
        return automatedScore;
    }

    public int total() {
        return automatedScore
                + playerScores.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
    }

    public boolean hasPositiveScore() {
        return total() > 0;
    }

    public void clear() {
        playerScores.clear();
        automatedScore = 0;
    }
}
