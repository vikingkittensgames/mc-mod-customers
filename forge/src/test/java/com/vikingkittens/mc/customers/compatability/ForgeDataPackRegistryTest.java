package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.minecraftforge.registries.DataPackRegistryEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ForgeDataPackRegistryTest {
    @Test
    void registersDeclaredDataPackRegistryDuringForgeRegistryEvent() {
        @SuppressWarnings("unchecked")
        ResourceKey<Registry<String>> key = mock(ResourceKey.class);
        ForgeRegistrationHelper helper = new ForgeRegistrationHelper();
        DataPackRegistryEvent.NewRegistry event = mock(DataPackRegistryEvent.NewRegistry.class);

        helper.registerDataPackRegistry(key, Codec.STRING);
        helper.registerDataPackRegistries(event);

        verify(event).dataPackRegistry(key, Codec.STRING, Codec.STRING);
    }
}
