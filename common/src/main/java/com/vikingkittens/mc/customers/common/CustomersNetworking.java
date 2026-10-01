package com.vikingkittens.mc.customers.common;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class CustomersNetworking {
    private CustomersNetworking() {}

    public static void initialize() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), BlockBreakConfirmationConfirmPayload.ID,
                (buffer, context) -> {
                    BlockBreakConfirmationConfirmPayload payload = BlockBreakConfirmationConfirmPayload.read(buffer);
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer player) {
                            BlockBreakConfirmation.confirm(player, payload.playerId(), payload.token());
                        }
                    });
                });
    }

    public static void sendToPlayer(ServerPlayer player, CustomersNetworkPayload payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        NetworkManager.sendToPlayer(player, payload.id(), buffer);
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(CustomersNetworkPayload payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        payload.write(buffer);
        NetworkManager.sendToServer(payload.id(), buffer);
    }
}
