package com.vikingkittens.mc.customers.compatability;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.CustomerPayloadClientHandlers;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class ForgeNetworkHelper implements INetworkHelper {
    public static void register() {
        ChannelHolder.CHANNEL.getName();
    }

    private static <T> StreamCodec<RegistryFriendlyByteBuf, T> playCodec(
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec
    ) {
        return StreamCodec.of(codec::encode, codec::decode);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ChannelHolder.CHANNEL.send(payload, player.connection.getConnection());
    }

    private static final class ChannelHolder {
        private static final Channel<CustomPacketPayload> CHANNEL = ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(Customers.MODID, "main"))
                .networkProtocolVersion(1)
                .payloadChannel()
                .play()
                .clientbound()
                .addMain(
                        CustomerShiftFinishedPayload.TYPE,
                        playCodec(CustomerShiftFinishedPayload.STREAM_CODEC),
                        (payload, context) -> CustomerPayloadClientHandlers.showShiftFinished(payload)
                )
                .addMain(
                        CustomerCounterMarkersPayload.TYPE,
                        playCodec(CustomerCounterMarkersPayload.STREAM_CODEC),
                        (payload, context) -> CustomerPayloadClientHandlers.showCounterMarkers(payload)
                )
                .addMain(
                        CustomerSpawnerSnapshotPayload.TYPE,
                        playCodec(CustomerSpawnerSnapshotPayload.STREAM_CODEC),
                        (payload, context) -> CustomerPayloadClientHandlers.updateSpawnerSnapshot(payload)
                )
                .build();

        private ChannelHolder() {}
    }
}
