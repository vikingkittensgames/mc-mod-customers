package com.vikingkittens.mc.customers.compatability;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class VanillaRegistrationHelper implements IRegistrationHelper {
    @Override
    public <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {}
}
