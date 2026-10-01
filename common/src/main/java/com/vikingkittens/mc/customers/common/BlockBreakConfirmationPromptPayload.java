package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public record BlockBreakConfirmationPromptPayload(UUID playerId, UUID token, String titleKey, String messageKey)
        implements CustomersNetworkPayload {
    public static final ResourceLocation ID =
            new ResourceLocation(Customers.MODID, "block_break_confirmation_prompt");

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerId());
        buffer.writeUUID(token());
        buffer.writeUtf(titleKey());
        buffer.writeUtf(messageKey());
    }

    public static BlockBreakConfirmationPromptPayload read(FriendlyByteBuf buffer) {
        return new BlockBreakConfirmationPromptPayload(buffer.readUUID(), buffer.readUUID(), buffer.readUtf(), buffer.readUtf());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
