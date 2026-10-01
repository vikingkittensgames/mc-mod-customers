package com.vikingkittens.mc.customers.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomersNetworkPayload {
    ResourceLocation id();

    void write(FriendlyByteBuf buffer);
}
