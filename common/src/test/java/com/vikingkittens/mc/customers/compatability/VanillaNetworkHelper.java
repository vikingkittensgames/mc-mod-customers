package com.vikingkittens.mc.customers.compatability;

import net.minecraft.server.level.ServerPlayer;

import com.vikingkittens.mc.customers.common.BlockBreakConfirmationConfirmPayload;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmationPromptPayload;
import com.vikingkittens.mc.customers.customer.CustomerCounterMarkersPayload;
import com.vikingkittens.mc.customers.customer.CustomerShiftFinishedPayload;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshotPayload;

public final class VanillaNetworkHelper implements INetworkHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, BlockBreakConfirmationPromptPayload payload) {}

    @Override
    public void sendToServer(BlockBreakConfirmationConfirmPayload payload) {}

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerCounterMarkersPayload payload) {}

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerShiftFinishedPayload payload) {}

    @Override
    public void sendToPlayer(ServerPlayer player, CustomerSpawnerSnapshotPayload payload) {}
}
