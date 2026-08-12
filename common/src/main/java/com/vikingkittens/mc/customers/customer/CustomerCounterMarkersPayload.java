package com.vikingkittens.mc.customers.customer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public record CustomerCounterMarkersPayload(
        List<CustomerCounterMarker> markers,
        List<BlockPos> surroundingPositions
) implements CustomPacketPayload {
    public static final Type<CustomerCounterMarkersPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_counter_markers")
    );

    public static final StreamCodec<FriendlyByteBuf, CustomerCounterMarkersPayload> STREAM_CODEC =
            StreamCodec.of(CustomerCounterMarkersPayload::write, CustomerCounterMarkersPayload::read);

    public CustomerCounterMarkersPayload {
        markers = List.copyOf(markers);
        surroundingPositions = List.copyOf(surroundingPositions);
    }

    private static void write(
            FriendlyByteBuf buffer,
            CustomerCounterMarkersPayload payload
    ) {
        buffer.writeCollection(payload.markers(), (target, marker) -> {
            target.writeBlockPos(marker.position());
            target.writeEnum(marker.spawnerMode());
        });
        buffer.writeCollection(payload.surroundingPositions(), (target, pos) -> target.writeBlockPos(pos));
    }

    private static CustomerCounterMarkersPayload read(FriendlyByteBuf buffer) {
        return new CustomerCounterMarkersPayload(
                buffer.readList(source -> new CustomerCounterMarker(
                        source.readBlockPos(),
                        source.readEnum(CustomerSpawnerMode.class)
                )),
                buffer.readList(source -> source.readBlockPos())
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
