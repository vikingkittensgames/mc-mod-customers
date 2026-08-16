package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public record BlockBreakConfirmationPromptPayload(UUID playerId, UUID token, String titleKey, String messageKey)
        implements CustomPacketPayload {
    public static final Type<BlockBreakConfirmationPromptPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "block_break_confirmation_prompt"));
    public static final StreamCodec<FriendlyByteBuf, BlockBreakConfirmationPromptPayload> STREAM_CODEC =
            StreamCodec.of(BlockBreakConfirmationPromptPayload::write, BlockBreakConfirmationPromptPayload::read);

    private static void write(FriendlyByteBuf buffer, BlockBreakConfirmationPromptPayload payload) {
        buffer.writeUUID(payload.playerId());
        buffer.writeUUID(payload.token());
        buffer.writeUtf(payload.titleKey());
        buffer.writeUtf(payload.messageKey());
    }

    private static BlockBreakConfirmationPromptPayload read(FriendlyByteBuf buffer) {
        return new BlockBreakConfirmationPromptPayload(buffer.readUUID(), buffer.readUUID(), buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
