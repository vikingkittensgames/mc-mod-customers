package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public record BlockBreakConfirmationConfirmPayload(UUID playerId, UUID token) implements CustomersNetworkPayload {
    public static final ResourceLocation ID =
            new ResourceLocation(Customers.MODID, "block_break_confirmation_confirm");

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId());
        buffer.writeUUID(token());
    }

    public static BlockBreakConfirmationConfirmPayload read(FriendlyByteBuf buffer) {
        return new BlockBreakConfirmationConfirmPayload(buffer.readUUID(), buffer.readUUID());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
