package com.vikingkittens.mc.customers.compatability;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface IRegistrationHelper {
    <T> void registerDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec);
}
