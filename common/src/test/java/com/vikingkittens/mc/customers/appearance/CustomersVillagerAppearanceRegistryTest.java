package com.vikingkittens.mc.customers.appearance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistry;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CustomersVillagerAppearanceRegistryTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesLoaderNeutralAppearanceRegistryHandle() {
        assertInstanceOf(CustomersRegistry.class, CustomersVillagerAppearance.APPEARANCE_REGISTRY);
        assertEquals(
                ResourceLocation.parse("customers:villager_appearance"),
                CustomersVillagerAppearance.APPEARANCE_REGISTRY.getKey().location()
        );
    }

    @Test
    void exposesLoaderNeutralDefaultAppearanceHandle() {
        assertInstanceOf(CustomersRegistryEntry.class, CustomersVillagerAppearances.DEFAULT_APPEARANCE);
        assertEquals(
                ResourceLocation.parse("customers:default"),
                CustomersVillagerAppearances.DEFAULT_APPEARANCE.getId()
        );
    }
}
