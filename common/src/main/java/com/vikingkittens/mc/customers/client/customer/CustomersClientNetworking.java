package com.vikingkittens.mc.customers.client.customer;

import dev.architectury.networking.NetworkManager;

import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardOpenPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class CustomersClientNetworking {
    private CustomersClientNetworking() {}

    public static void initialize() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerShiftFinishedPayload.TYPE,
                CustomerShiftFinishedPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> CustomerPayloadClientHandlers.showShiftFinished(payload)));
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerLeaderboardOpenPayload.TYPE,
                CustomerLeaderboardOpenPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> CustomerPayloadClientHandlers.showLeaderboard(payload)));
        NetworkManager.registerReceiver(NetworkManager.s2c(), BlockBreakConfirmationPromptPayload.TYPE,
                BlockBreakConfirmationPromptPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> CustomerPayloadClientHandlers.showBlockBreakConfirmation(payload)));
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerCounterMarkersPayload.TYPE,
                CustomerCounterMarkersPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> CustomerPayloadClientHandlers.showCounterMarkers(payload)));
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerSpawnerSnapshotPayload.TYPE,
                CustomerSpawnerSnapshotPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> CustomerPayloadClientHandlers.updateSpawnerSnapshot(payload)));
    }
}
