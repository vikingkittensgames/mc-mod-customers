package com.vikingkittens.mc.customers.common;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardOpenPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class CustomersNetworking {
    private CustomersNetworking() {}

    public static void initialize() {
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(CustomerShiftFinishedPayload.TYPE, CustomerShiftFinishedPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(CustomerLeaderboardOpenPayload.TYPE, CustomerLeaderboardOpenPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(BlockBreakConfirmationPromptPayload.TYPE,
                    BlockBreakConfirmationPromptPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(CustomerCounterMarkersPayload.TYPE,
                    CustomerCounterMarkersPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(CustomerSpawnerSnapshotPayload.TYPE,
                    CustomerSpawnerSnapshotPayload.STREAM_CODEC);
        }
        NetworkManager.registerReceiver(NetworkManager.c2s(), BlockBreakConfirmationConfirmPayload.TYPE,
                BlockBreakConfirmationConfirmPayload.STREAM_CODEC, (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer player) {
                        BlockBreakConfirmation.confirm(player, payload.playerId(), payload.token());
                    }
                }));
    }
}
