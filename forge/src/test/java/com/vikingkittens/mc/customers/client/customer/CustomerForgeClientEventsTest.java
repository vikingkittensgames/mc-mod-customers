package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class CustomerForgeClientEventsTest {
    @Test
    void defersClientSetupWork() {
        FMLClientSetupEvent event = mock(FMLClientSetupEvent.class);
        ArgumentCaptor<Runnable> work = ArgumentCaptor.forClass(Runnable.class);

        CustomerForgeClientEvents.clientSetup(event);

        verify(event).enqueueWork(work.capture());
    }

    @Test
    void registersClientNetworking() {
        try (var networking = mockStatic(CustomersClientNetworking.class)) {
            CustomerForgeClientEvents.initializeClientNetworking();

            networking.verify(CustomersClientNetworking::initialize);
        }
    }
}
