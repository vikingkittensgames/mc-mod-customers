package com.vikingkittens.mc.customers.compatability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.CustomerPayloadClientHandlers;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmation;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationConfirmPayload;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardOpenPayload;
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
            channel.registerMessage(
                    3,
                    BlockBreakConfirmationPromptPayload.class,
                    (payload, buffer) -> BlockBreakConfirmationPromptPayload.write(buffer, payload),
                    BlockBreakConfirmationPromptPayload::read,
                    (payload, context) -> {
                        context.get().enqueueWork(() -> CustomerPayloadClientHandlers.showBlockBreakConfirmation(payload));
                        context.get().setPacketHandled(true);
                    },
                    java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
            );
            channel.registerMessage(
                    4,
                    BlockBreakConfirmationConfirmPayload.class,
                    (payload, buffer) -> BlockBreakConfirmationConfirmPayload.write(buffer, payload),
                    BlockBreakConfirmationConfirmPayload::read,
                    (payload, context) -> {
                        context.get().enqueueWork(() -> {
                            ServerPlayer player = context.get().getSender();
                            if (player != null) {
                                BlockBreakConfirmation.confirm(player, payload.playerId(), payload.token());
                            }
                        });
                        context.get().setPacketHandled(true);
                    },
                    java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER)
            );
            channel.registerMessage(
                    5,
                    CustomerLeaderboardOpenPayload.class,
                    (payload, buffer) -> CustomerLeaderboardOpenPayload.write(buffer, payload),
                    CustomerLeaderboardOpenPayload::read,
                    (payload, context) -> {
                        context.get().enqueueWork(() -> CustomerPayloadClientHandlers.showLeaderboard(payload));
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
    public void sendToPlayer(ServerPlayer player, BlockBreakConfirmationPromptPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToServer(BlockBreakConfirmationConfirmPayload payload) {
        channel().sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerShiftFinishedPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerLeaderboardOpenPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerSpawnerSnapshotPayload payload) {
        channel().send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
