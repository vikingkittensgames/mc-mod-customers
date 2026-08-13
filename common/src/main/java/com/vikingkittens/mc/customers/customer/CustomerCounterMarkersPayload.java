package com.vikingkittens.mc.customers.customer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record CustomerCounterMarkersPayload(
        List<CustomerCounterMarker> markers,
        List<BlockPos> surroundingPositions
) {

    public CustomerCounterMarkersPayload {
        markers = List.copyOf(markers);
        surroundingPositions = List.copyOf(surroundingPositions);
    }

    public static void write(
            FriendlyByteBuf buffer,
            CustomerCounterMarkersPayload payload
    ) {
        buffer.writeCollection(payload.markers(), (target, marker) -> {
            target.writeBlockPos(marker.position());
            target.writeEnum(marker.spawnerMode());
        });
        buffer.writeCollection(payload.surroundingPositions(), (target, pos) -> target.writeBlockPos(pos));
    }

    public static CustomerCounterMarkersPayload read(FriendlyByteBuf buffer) {
        return new CustomerCounterMarkersPayload(
                buffer.readList(source -> new CustomerCounterMarker(
                        source.readBlockPos(),
                        source.readEnum(CustomerSpawnerMode.class)
                )),
                buffer.readList(source -> source.readBlockPos())
        );
    }

}
