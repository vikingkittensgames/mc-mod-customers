package com.vikingkittens.mc.customers.customer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies customer shift result payload serialization and snapshot behavior. */
class CustomerShiftFinishedPayloadTest {
    /** Verifies every shift result field survives a network round trip. */
    @Test
    void roundTripsAllShiftResults() {
        UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CustomerShiftFinishedPayload original = new CustomerShiftFinishedPayload(
                CustomerSpawnerMode.LUNCH, 0.75F, 12, 8, 3, Map.of(playerId, 14)
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        CustomerShiftFinishedPayload.STREAM_CODEC.encode(buffer, original);
        CustomerShiftFinishedPayload decoded = CustomerShiftFinishedPayload.STREAM_CODEC.decode(buffer);

        assertEquals(original, decoded);
    }
    /** Verifies the payload retains a stable immutable snapshot of player results. */
    @Test
    void copiesPlayerResults() {
        UUID playerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Map<UUID, Integer> mutableResults = new HashMap<>();
        mutableResults.put(playerId, 5);
        CustomerShiftFinishedPayload payload = new CustomerShiftFinishedPayload(
                CustomerSpawnerMode.DINNER, 0.5F, 10, 5, 2, mutableResults
        );

        mutableResults.put(playerId, 9);

        assertEquals(5, payload.numItemsServedByPlayer().get(playerId));
        assertThrows(UnsupportedOperationException.class,
                () -> payload.numItemsServedByPlayer().put(playerId, 10));
    }
}
