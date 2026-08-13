package com.vikingkittens.mc.customers.compatability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.CustomerPayloadClientHandlers;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class ForgeNetworkHelper implements INetworkHelper {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel channel() {
        return ChannelHolder.CHANNEL;
    }

    private static final class ChannelHolder {
        private static final SimpleChannel CHANNEL = createChannel();

        private static SimpleChannel createChannel() {
            SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(Customers.MODID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );
            channel.registerMessage(
                    0,
                    CustomerShiftFinishedPayload.class,
                    (payload, buffer) -> CustomerShiftFinishedPayload.write(buffer, payload),
                    CustomerShiftFinishedPayload::read,
                    (payload, context) -> {
                        context.get().enqueueWork(() -> CustomerPayloadClientHandlers.showShiftFinished(payload));
                        context.get().setPacketHandled(true);
                    }
            );
            channel.registerMessage(
                    1,
                    CustomerCounterMarkersPayload.class,
                    (payload, buffer) -> CustomerCounterMarkersPayload.write(buffer, payload),
                    CustomerCounterMarkersPayload::read,
                    (payload, context) -> {
                        context.get().enqueueWork(() -> CustomerPayloadClientHandlers.showCounterMarkers(payload));
                        context.get().setPacketHandled(true);
                    }
            );
            channel.registerMessage(
                    2,
                    CustomerSpawnerSnapshotPayload.class,
                    (payload, buffer) -> CustomerSpawnerSnapshotPayload.write(buffer, payload),
                    CustomerSpawnerSnapshotPayload::read,
                    (payload, context) -> {
                        context.get().enqueueWork(() -> CustomerPayloadClientHandlers.updateSpawnerSnapshot(payload));
                        context.get().setPacketHandled(true);
                    }
            );
            return channel;
        }
    }

    public static void register() {
        channel();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerCounterMarkersPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerShiftFinishedPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerSpawnerSnapshotPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
