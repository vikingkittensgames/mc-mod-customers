package com.vikingkittens.mc.customers.customer;

import java.util.Map;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerLeaderboardOpenPayloadTest {
    @Test
    void roundTripsScoresAndLevelCompletion() {
        CustomerLeaderboardScores.Key key = new CustomerLeaderboardScores.Key(
                new BlockPos(1, 64, 2),
                CustomerSpawnerMode.DINNER,
                2,
                UUID.randomUUID()
        );
        CustomerLeaderboardOpenPayload original = new CustomerLeaderboardOpenPayload(
                BlockPos.ZERO,
                Map.of(key, new CustomerLeaderboardOpenPayload.Score(0.8F, true))
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        CustomerLeaderboardOpenPayload.write(buffer, original);

        assertEquals(original, CustomerLeaderboardOpenPayload.read(buffer));
    }
}
