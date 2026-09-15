package com.vikingkittens.mc.customers.advancements;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CustomersAdvancementsRegistrationTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void registersTheLoaderNeutralAdvancementIconItem() {
        assertInstanceOf(
                CustomersRegistryEntry.class,
                CustomersAdvancements.ADVANCEMENT_ICON
        );
        assertEquals(
                ResourceLocation.parse("customers:advancement_icon"),
                CustomersAdvancements.ADVANCEMENT_ICON.getId()
        );
    }
}
