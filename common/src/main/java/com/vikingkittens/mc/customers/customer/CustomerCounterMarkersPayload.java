package com.vikingkittens.mc.customers.customer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.CustomersNetworkPayload;

public record CustomerCounterMarkersPayload(
        List<CustomerCounterMarker> markers,
        List<BlockPos> surroundingPositions
) implements CustomersNetworkPayload {
    public static final ResourceLocation ID = new ResourceLocation(Customers.MODID, "customer_counter_markers");

    public CustomerCounterMarkersPayload {
        markers = List.copyOf(markers);
        surroundingPositions = List.copyOf(surroundingPositions);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(markers(), (target, marker) -> {
            target.writeBlockPos(marker.position());
            target.writeEnum(marker.spawnerMode());
        });
        buffer.writeCollection(surroundingPositions(), (target, pos) -> target.writeBlockPos(pos));
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

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
