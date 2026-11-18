package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import com.vikingkittens.mc.customers.client.common.BlockBreakConfirmationScreen;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardOpenPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockMenu;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerPetDataPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class CustomerPayloadClientHandlers {
    private static CustomerSpawnerPetDataPayload pendingPetData;
    private CustomerPayloadClientHandlers() {}

    public static void showShiftFinished(CustomerShiftFinishedPayload payload) {
        Minecraft.getInstance().setScreen(new CustomerShiftFinishedScreen(payload));
    }

    public static void showLeaderboard(CustomerLeaderboardOpenPayload payload) {
        Minecraft.getInstance().setScreen(new CustomerLeaderboardBlockScreen(payload));
    }

    public static void showBlockBreakConfirmation(BlockBreakConfirmationPromptPayload payload) {
        Minecraft.getInstance().setScreen(new BlockBreakConfirmationScreen(payload));
    }

    public static void showCounterMarkers(CustomerCounterMarkersPayload payload) {
        CustomerCounterMarkerManager.show(payload.markers(), payload.surroundingPositions(), Util.getMillis());
    }

    public static void updateSpawnerSnapshot(CustomerSpawnerSnapshotPayload payload) {
        payload.snapshot().ifPresentOrElse(
                CustomerSpawnerSnapshotManager::replace,
                () -> CustomerSpawnerSnapshotManager.remove(payload.spawnerPos())
        );
    }

    public static void updateSpawnerPetData(CustomerSpawnerPetDataPayload payload) {
        pendingPetData = payload;
        if (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.containerMenu instanceof CustomerSpawnerBlockMenu menu) {
            applyPendingSpawnerPetData(menu);
        }
    }

    public static void applyPendingSpawnerPetData(CustomerSpawnerBlockMenu menu) {
        if (pendingPetData != null && pendingPetData.containerId() == menu.containerId) {
            menu.applyPetData(pendingPetData);
            pendingPetData = null;
        }
    }
}
