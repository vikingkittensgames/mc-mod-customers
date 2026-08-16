package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public record BlockBreakConfirmationConfirmPayload(UUID playerId, UUID token) implements CustomPacketPayload {
    public static final Type<BlockBreakConfirmationConfirmPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "block_break_confirmation_confirm"));
    public static final StreamCodec<FriendlyByteBuf, BlockBreakConfirmationConfirmPayload> STREAM_CODEC =
            StreamCodec.of(BlockBreakConfirmationConfirmPayload::write, BlockBreakConfirmationConfirmPayload::read);

    private static void write(FriendlyByteBuf buffer, BlockBreakConfirmationConfirmPayload payload) {
        buffer.writeUUID(payload.playerId());
        buffer.writeUUID(payload.token());
    }

    private static BlockBreakConfirmationConfirmPayload read(FriendlyByteBuf buffer) {
        return new BlockBreakConfirmationConfirmPayload(buffer.readUUID(), buffer.readUUID());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
