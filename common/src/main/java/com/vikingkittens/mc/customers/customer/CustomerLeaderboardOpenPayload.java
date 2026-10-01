package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.CustomersNetworkPayload;

public record CustomerLeaderboardOpenPayload(
        BlockPos leaderboardPos,
        Map<CustomerLeaderboardScores.Key, Score> scores
) implements CustomersNetworkPayload {
    public record Score(float value, boolean levelPassed) {}

    public static final ResourceLocation ID = new ResourceLocation(Customers.MODID, "customer_leaderboard_open");

    public CustomerLeaderboardOpenPayload {
        scores = Map.copyOf(scores);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(leaderboardPos());
        buffer.writeMap(
                scores(),
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

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
