package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerCounterMarkersPayloadTest {
    @Test
    void roundTripsCounterMarkers() {
        CustomerCounterMarkersPayload original = new CustomerCounterMarkersPayload(List.of(
                new CustomerCounterMarker(
                        new BlockPos(100, 60, 150),
                        CustomerSpawnerMode.BREAKFAST
                ),
                new CustomerCounterMarker(
                        new BlockPos(-20, 70, 30),
                        CustomerSpawnerMode.NIGHT
                )
        ), List.of(new BlockPos(1, 2, 3)));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        CustomerCounterMarkersPayload.STREAM_CODEC.encode(buffer, original);
        CustomerCounterMarkersPayload decoded =
                CustomerCounterMarkersPayload.STREAM_CODEC.decode(buffer);

        assertEquals(original, decoded);
    }

    @Test
    void copiesMarkerList() {
        List<CustomerCounterMarker> mutableMarkers = new ArrayList<>();
        mutableMarkers.add(new CustomerCounterMarker(
                BlockPos.ZERO,
                CustomerSpawnerMode.CONTINUOUS
        ));

        CustomerCounterMarkersPayload payload =
                new CustomerCounterMarkersPayload(mutableMarkers, List.of());
        mutableMarkers.clear();

        assertEquals(1, payload.markers().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> payload.markers().clear()
        );
    }
}
