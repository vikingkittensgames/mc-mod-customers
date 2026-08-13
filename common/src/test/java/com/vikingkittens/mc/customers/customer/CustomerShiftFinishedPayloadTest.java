package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.network.FriendlyByteBuf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies customer shift result payload serialization and snapshot behavior. */
class CustomerShiftFinishedPayloadTest {
    /** Verifies every shift result field survives a network round trip. */
    @Test
    void roundTripsAllShiftResults() {
        UUID playerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CustomerShiftFinishedPayload original = new CustomerShiftFinishedPayload(
                CustomerSpawnerMode.LUNCH,
                0.75F,
                12,
                8,
                3,
                Map.of(playerId, 14),
                Map.of(playerId, 21),
                6,
                9
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        CustomerShiftFinishedPayload.write(buffer, original);
        CustomerShiftFinishedPayload decoded = CustomerShiftFinishedPayload.read(buffer);

        assertEquals(original, decoded);
    }
    /** Verifies the payload retains a stable immutable snapshot of player results. */
    @Test
    void copiesPlayerResults() {
        UUID playerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Map<UUID, Integer> mutableResults = new HashMap<>();
        mutableResults.put(playerId, 5);
        CustomerShiftFinishedPayload payload = new CustomerShiftFinishedPayload(
                CustomerSpawnerMode.DINNER,
                0.5F,
                10,
                5,
                2,
                mutableResults,
                mutableResults,
                0,
                0
        );

        mutableResults.put(playerId, 9);

        assertEquals(5, payload.numItemsServedByPlayer().get(playerId));
        assertThrows(UnsupportedOperationException.class,
                () -> payload.numItemsServedByPlayer().put(playerId, 10));
        assertEquals(5, payload.numItemsCraftedByPlayer().get(playerId));
        assertThrows(UnsupportedOperationException.class,
                () -> payload.numItemsCraftedByPlayer().put(playerId, 10));
    }
    /** Calculates total served and crafted item units from player results. */
    @Test
    void calculatesItemTotals() {
        CustomerShiftFinishedPayload payload =
                new CustomerShiftFinishedPayload(
                        CustomerSpawnerMode.BREAKFAST,
                        1.0F,
                        3,
                        3,
                        0,
                        Map.of(
                                UUID.randomUUID(), 8,
                                UUID.randomUUID(), 5
                        ),
                        Map.of(
                                UUID.randomUUID(), 12,
                                UUID.randomUUID(), 7
                        ),
                        4,
                        6
                );

        assertEquals(17, payload.totalItemsServed());
        assertEquals(25, payload.totalItemsCrafted());
        assertEquals(4, payload.numItemsServedAutomated());
        assertEquals(6, payload.numItemsCraftedAutomated());
    }}
