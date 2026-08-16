package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public record BlockBreakConfirmationPromptPayload(UUID playerId, UUID token, String titleKey, String messageKey) {
    public static void write(FriendlyByteBuf buffer, BlockBreakConfirmationPromptPayload payload) {
        buffer.writeUUID(payload.playerId());
        buffer.writeUUID(payload.token());
        buffer.writeUtf(payload.titleKey());
        buffer.writeUtf(payload.messageKey());
    }

    public static BlockBreakConfirmationPromptPayload read(FriendlyByteBuf buffer) {
        return new BlockBreakConfirmationPromptPayload(buffer.readUUID(), buffer.readUUID(), buffer.readUtf(), buffer.readUtf());
    }
}
