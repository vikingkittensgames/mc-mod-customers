package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.registries.NewRegistryEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NeoForgeCustomRegistryTest {
    @Test
    void createsCustomRegistryForItsRequestedKey() {
        ResourceKey<Registry<Object>> key = ResourceKey.createRegistryKey(
                ResourceLocation.parse("customers:neoforge_test_registry")
        );

        CustomersRegistry<Object> registry = new NeoForgeRegistrationHelper().createRegistry(key);

        assertEquals(key, registry.getKey());
    }

    @Test
    void registersCustomRegistryDuringNeoForgeRegistryEvent() {
        ResourceKey<Registry<Object>> key = ResourceKey.createRegistryKey(
                ResourceLocation.parse("customers:neoforge_event_test_registry")
        );
        NeoForgeRegistrationHelper helper = new NeoForgeRegistrationHelper();
        CustomersRegistry<Object> registry = helper.createRegistry(key);
        NewRegistryEvent event = mock(NewRegistryEvent.class);

        helper.registerCustomRegistries(event);

        verify(event).register(registry.get());
    }
}
