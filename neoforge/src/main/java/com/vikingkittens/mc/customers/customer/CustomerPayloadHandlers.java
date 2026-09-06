package com.vikingkittens.mc.customers.customer;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vikingkittens.mc.customers.client.customer.CustomerPayloadClientHandlers;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;

public final class CustomerPayloadHandlers {
    private CustomerPayloadHandlers() {}

    public static void handleShiftFinished(CustomerShiftFinishedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CustomerPayloadClientHandlers.showShiftFinished(payload));
    }

    public static void handleLeaderboard(CustomerLeaderboardOpenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CustomerPayloadClientHandlers.showLeaderboard(payload));
    }

    public static void handleBlockBreakConfirmation(BlockBreakConfirmationPromptPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CustomerPayloadClientHandlers.showBlockBreakConfirmation(payload));
    }

    public static void handleCounterMarkers(CustomerCounterMarkersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CustomerPayloadClientHandlers.showCounterMarkers(payload));
    }

    public static void handleSpawnerSnapshot(CustomerSpawnerSnapshotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CustomerPayloadClientHandlers.updateSpawnerSnapshot(payload));
    }
}
