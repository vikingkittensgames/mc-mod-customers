package com.vikingkittens.mc.customers;

import dev.architectury.platform.forge.EventBuses;
import org.junit.jupiter.api.Test;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class CustomersForgeTest {
    @Test
    void isForgeModEntrypoint() {
        Mod annotation = CustomersForge.class.getAnnotation(Mod.class);

        assertNotNull(annotation);
        assertEquals(Customers.MODID, annotation.value());
        assertEquals(1, CustomersForge.class.getConstructors().length);
        assertEquals(1, CustomersForge.class.getConstructors()[0].getParameterCount());
        assertEquals(
                FMLJavaModLoadingContext.class,
                CustomersForge.class.getConstructors()[0].getParameterTypes()[0]
        );
    }

    @Test
    void registersForgeModEventBusWithArchitectury() {
        IEventBus modEventBus = mock(IEventBus.class);

        CustomersForge.registerArchitecturyEventBus(modEventBus);

        assertSame(modEventBus, EventBuses.getModEventBus(Customers.MODID).orElseThrow());
    }
}
