package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.registries.DataPackRegistryEvent;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ForgeDataPackRegistryTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void registersDeclaredDataPackRegistryDuringForgeRegistryEvent() {
        ResourceKey<Registry<String>> key = ResourceKey.createRegistryKey(
                ResourceLocation.parse("customers:forge_data_pack_test_registry"));
        ForgeRegistrationHelper helper = new ForgeRegistrationHelper();
        DataPackRegistryEvent.NewRegistry event = mock(DataPackRegistryEvent.NewRegistry.class);

        helper.registerDataPackRegistry(key, Codec.STRING);
        helper.registerDataPackRegistries(event);

        verify(event).dataPackRegistry(key, Codec.STRING, Codec.STRING);
    }
}
