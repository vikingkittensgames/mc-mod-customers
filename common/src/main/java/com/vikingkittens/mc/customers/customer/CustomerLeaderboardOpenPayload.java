package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public record CustomerLeaderboardOpenPayload(
        BlockPos leaderboardPos,
        Map<CustomerLeaderboardScores.Key, Score> scores
) implements CustomPacketPayload {
    public record Score(float value, boolean levelPassed) {}

    public static final Type<CustomerLeaderboardOpenPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_leaderboard_open")
    );
    public static final StreamCodec<FriendlyByteBuf, CustomerLeaderboardOpenPayload> STREAM_CODEC =
            StreamCodec.of(CustomerLeaderboardOpenPayload::write, CustomerLeaderboardOpenPayload::read);

    public CustomerLeaderboardOpenPayload {
        scores = Map.copyOf(scores);
    }

    private static void write(FriendlyByteBuf buffer, CustomerLeaderboardOpenPayload payload) {
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

    private static CustomerLeaderboardOpenPayload read(FriendlyByteBuf buffer) {
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
