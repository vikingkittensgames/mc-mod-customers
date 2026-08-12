package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class VanillaRegistrationHelperTest {
    @Test
    void acceptsDataPackRegistryDeclarationsWithoutALoaderLifecycle() {
        ResourceKey<Registry<String>> registryKey = ResourceKey.createRegistryKey(
                ResourceLocation.parse("customers:vanilla_data_pack_test_registry"));

        assertDoesNotThrow(() -> new VanillaRegistrationHelper().registerDataPackRegistry(registryKey, Codec.STRING));
    }

    @Test
    void createsRegistryAndRegistersValuesImmediately() {
        ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(
                ResourceLocation.parse("customers:vanilla_test_registry")
        );
        ResourceLocation valueId = ResourceLocation.parse("customers:value");
        VanillaRegistrationHelper helper = new VanillaRegistrationHelper();

        CustomersRegistry<Object> registry = helper.createRegistry(registryKey);
        CustomersRegistryEntry<Object, Object> entry = helper.register(registryKey, valueId.getPath(), Object::new);

        assertSame(entry.get(), registry.get().get(valueId));
    }
}
