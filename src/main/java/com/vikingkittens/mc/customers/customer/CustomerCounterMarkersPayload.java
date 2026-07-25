package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.CustomerClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.util.List;

public record CustomerCounterMarkersPayload(
        List<CustomerCounterMarker> markers
) implements CustomPacketPayload {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<CustomerCounterMarkersPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "customer_counter_markers")
    );

    public static final StreamCodec<FriendlyByteBuf, CustomerCounterMarkersPayload> STREAM_CODEC =
            StreamCodec.of(CustomerCounterMarkersPayload::write, CustomerCounterMarkersPayload::read);

    public CustomerCounterMarkersPayload {
        markers = List.copyOf(markers);
    }

    private static void write(
            FriendlyByteBuf buffer,
            CustomerCounterMarkersPayload payload
    ) {
        buffer.writeCollection(payload.markers(), (target, marker) -> {
            target.writeBlockPos(marker.position());
            target.writeEnum(marker.spawnerMode());
        });
    }

    private static CustomerCounterMarkersPayload read(FriendlyByteBuf buffer) {
        return new CustomerCounterMarkersPayload(
                buffer.readList(source -> new CustomerCounterMarker(
                        source.readBlockPos(),
                        source.readEnum(CustomerSpawnerMode.class)
                ))
        );
    }

    public static void handle(
            CustomerCounterMarkersPayload payload,
            IPayloadContext context
    ) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerClientEvents.showCounterMarkers(payload);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
