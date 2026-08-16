package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public record BlockBreakConfirmationConfirmPayload(UUID playerId, UUID token) {
    public static void write(FriendlyByteBuf buffer, BlockBreakConfirmationConfirmPayload payload) {
        buffer.writeUUID(payload.playerId());
        buffer.writeUUID(payload.token());
    }

    public static BlockBreakConfirmationConfirmPayload read(FriendlyByteBuf buffer) {
        return new BlockBreakConfirmationConfirmPayload(buffer.readUUID(), buffer.readUUID());
    }
}
