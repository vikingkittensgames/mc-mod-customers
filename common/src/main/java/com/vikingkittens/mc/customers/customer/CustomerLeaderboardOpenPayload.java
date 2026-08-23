package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record CustomerLeaderboardOpenPayload(
        BlockPos leaderboardPos,
        Map<CustomerLeaderboardScores.Key, Score> scores
) {
    public record Score(float value, boolean levelPassed) {}

    public CustomerLeaderboardOpenPayload {
        scores = Map.copyOf(scores);
    }

    public static void write(FriendlyByteBuf buffer, CustomerLeaderboardOpenPayload payload) {
        buffer.writeBlockPos(payload.leaderboardPos());
        buffer.writeMap(
                payload.scores(),
                (target, key) -> {
                    target.writeBlockPos(key.spawnerPosition());
                    target.writeEnum(key.spawnerMode());
                    target.writeVarInt(key.level());
                    target.writeUUID(key.playerId());
                },
                (target, score) -> {
                    target.writeFloat(score.value());
                    target.writeBoolean(score.levelPassed());
                }
        );
    }

    public static CustomerLeaderboardOpenPayload read(FriendlyByteBuf buffer) {
        return new CustomerLeaderboardOpenPayload(
                buffer.readBlockPos(),
                buffer.readMap(
                        HashMap::new,
                        source -> new CustomerLeaderboardScores.Key(
                                source.readBlockPos(),
                                source.readEnum(CustomerSpawnerMode.class),
                                source.readVarInt(),
                                source.readUUID()
                        ),
                        source -> new Score(source.readFloat(), source.readBoolean())
                )
        );
    }

}
