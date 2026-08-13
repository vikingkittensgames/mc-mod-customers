package com.vikingkittens.mc.customers.compatability;

import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public interface INetworkHelper {
    void sendToPlayer(ServerPlayer player, CustomerCounterMarkersPayload payload);

    void sendToPlayer(ServerPlayer player, CustomerShiftFinishedPayload payload);

    void sendToPlayer(ServerPlayer player, CustomerSpawnerSnapshotPayload payload);
}
