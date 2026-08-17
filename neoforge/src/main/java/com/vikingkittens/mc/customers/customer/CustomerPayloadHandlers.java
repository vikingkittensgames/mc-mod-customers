package com.vikingkittens.mc.customers.customer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vikingkittens.mc.customers.client.customer.CustomerPayloadClientHandlers;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;

public final class CustomerPayloadHandlers {
    private CustomerPayloadHandlers() {}

    public static void handleShiftFinished(CustomerShiftFinishedPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerPayloadClientHandlers.showShiftFinished(payload);
        }
    }

    public static void handleLeaderboard(CustomerLeaderboardOpenPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerPayloadClientHandlers.showLeaderboard(payload);
        }
    }

    public static void handleBlockBreakConfirmation(BlockBreakConfirmationPromptPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerPayloadClientHandlers.showBlockBreakConfirmation(payload);
        }
    }

    public static void handleCounterMarkers(CustomerCounterMarkersPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerPayloadClientHandlers.showCounterMarkers(payload);
        }
    }

    public static void handleSpawnerSnapshot(CustomerSpawnerSnapshotPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerPayloadClientHandlers.updateSpawnerSnapshot(payload);
        }
    }
}
