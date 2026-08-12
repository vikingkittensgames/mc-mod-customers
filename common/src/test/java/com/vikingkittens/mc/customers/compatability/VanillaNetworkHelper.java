package com.vikingkittens.mc.customers.compatability;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class VanillaNetworkHelper implements INetworkHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {}
}
