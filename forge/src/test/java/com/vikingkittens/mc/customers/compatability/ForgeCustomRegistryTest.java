package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForgeCustomRegistryTest {
    @Test
    void createsCustomRegistryForItsRequestedKey() {
        ResourceKey<Registry<Object>> key = ResourceKey.createRegistryKey(
                ResourceLocation.parse("customers:forge_test_registry")
        );

        CustomersRegistry<Object> registry = new ForgeRegistrationHelper().createRegistry(key);

        assertEquals(key, registry.getKey());
    }
}
