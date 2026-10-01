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
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerShiftFinishedPayload.ID,
                (buffer, context) -> {
                    CustomerShiftFinishedPayload payload = CustomerShiftFinishedPayload.read(buffer);
                    context.queue(() -> CustomerPayloadClientHandlers.showShiftFinished(payload));
                });
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerLeaderboardOpenPayload.ID,
                (buffer, context) -> {
                    CustomerLeaderboardOpenPayload payload = CustomerLeaderboardOpenPayload.read(buffer);
                    context.queue(() -> CustomerPayloadClientHandlers.showLeaderboard(payload));
                });
        NetworkManager.registerReceiver(NetworkManager.s2c(), BlockBreakConfirmationPromptPayload.ID,
                (buffer, context) -> {
                    BlockBreakConfirmationPromptPayload payload = BlockBreakConfirmationPromptPayload.read(buffer);
                    context.queue(() -> CustomerPayloadClientHandlers.showBlockBreakConfirmation(payload));
                });
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerCounterMarkersPayload.ID,
                (buffer, context) -> {
                    CustomerCounterMarkersPayload payload = CustomerCounterMarkersPayload.read(buffer);
                    context.queue(() -> CustomerPayloadClientHandlers.showCounterMarkers(payload));
                });
        NetworkManager.registerReceiver(NetworkManager.s2c(), CustomerSpawnerSnapshotPayload.ID,
                (buffer, context) -> {
                    CustomerSpawnerSnapshotPayload payload = CustomerSpawnerSnapshotPayload.read(buffer);
                    context.queue(() -> CustomerPayloadClientHandlers.updateSpawnerSnapshot(payload));
                });
    }
}
