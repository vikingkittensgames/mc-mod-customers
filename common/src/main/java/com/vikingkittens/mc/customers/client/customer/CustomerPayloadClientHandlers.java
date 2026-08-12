package com.vikingkittens.mc.customers.client.customer;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class CustomerPayloadClientHandlers {
    private CustomerPayloadClientHandlers() {}

    public static void showShiftFinished(CustomerShiftFinishedPayload payload) {
        Minecraft.getInstance().setScreen(new CustomerShiftFinishedScreen(payload));
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
}
