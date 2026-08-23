package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

public final class CustomerLeaderboardScores {
    private static final String TAG_LEVEL = "level";
    private static final String TAG_MODE = "mode";
    private static final String TAG_PLAYER_ID = "playerId";
    private static final String TAG_SCORE = "score";
    private static final String TAG_SCORES = "scores";
    private static final String TAG_SPAWNER_POSITION = "spawnerPosition";

    private final Map<Key, Float> scores = new HashMap<>();

    public boolean add(Key key, float score) {
        float clampedScore = Mth.clamp(score, 0.0F, 1.0F);
        Float existingScore = scores.get(key);
        if (existingScore != null && existingScore >= clampedScore) {
            return false;
        }
        scores.put(key, clampedScore);
        return true;
    }

    public float get(Key key) {
        return scores.getOrDefault(key, -1.0F);
    }

    public boolean isEmpty() {
        return scores.isEmpty();
    }

    public Map<Key, Float> scores() {
        return Map.copyOf(scores);
    }

    public void write(DataWriter output) {
        scores.forEach((key, score) -> {
            DataWriter scoreOutput = output.addChild(TAG_SCORES);
            scoreOutput.putBlockPos(TAG_SPAWNER_POSITION, key.spawnerPosition());
            scoreOutput.putString(TAG_MODE, key.spawnerMode().getSerializedName());
            scoreOutput.putInt(TAG_LEVEL, key.level());
            scoreOutput.putUuid(TAG_PLAYER_ID, key.playerId());
            scoreOutput.putFloat(TAG_SCORE, score);
        });
    }

    public void read(DataReader input) {
        scores.clear();
        for (DataReader scoreInput : input.getChildren(TAG_SCORES)) {
            scoreInput.getBlockPos(TAG_SPAWNER_POSITION).ifPresent(spawnerPosition ->
                    scoreInput.getString(TAG_MODE)
                            .flatMap(CustomerLeaderboardScores::getSpawnerMode)
                            .ifPresent(spawnerMode ->
                                    scoreInput.getUuid(TAG_PLAYER_ID).ifPresent(playerId -> add(
                                            new Key(
                                                    spawnerPosition,
                                                    spawnerMode,
                                                    scoreInput.getInt(TAG_LEVEL).orElse(1),
                                                    playerId
                                            ),
                                            scoreInput.getFloat(TAG_SCORE).orElse(0.0F)
                                    ))
                            )
            );
        }
    }

    private static java.util.Optional<CustomerSpawnerMode> getSpawnerMode(String value) {
        return java.util.Arrays.stream(CustomerSpawnerMode.values())
                .filter(spawnerMode -> spawnerMode.getSerializedName().equals(value))
                .findFirst();
    }

    public record Key(
            BlockPos spawnerPosition,
            CustomerSpawnerMode spawnerMode,
            int level,
            UUID playerId
    ) {
        public Key {
            spawnerPosition = spawnerPosition.immutable();
            if (level < 1 || level > CustomerSpawnerBlockEntity.MAX_LEVELS) {
                throw new IllegalArgumentException("Customer leaderboard level must be between 1 and 8");
            }
        }
    }
}
