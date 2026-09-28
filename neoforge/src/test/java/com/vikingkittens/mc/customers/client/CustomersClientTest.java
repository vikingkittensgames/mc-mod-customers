package com.vikingkittens.mc.customers.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import com.vikingkittens.mc.customers.client.advancements.ftb.CustomersFTBClient;
import com.vikingkittens.mc.customers.client.customer.CustomersClientNetworking;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class CustomersClientTest {
    @Test
    void defersClientInitializationUntilClientSetupWorkRuns() {
        FMLClientSetupEvent event = mock(FMLClientSetupEvent.class);
        ArgumentCaptor<Runnable> work = ArgumentCaptor.forClass(Runnable.class);

        try (var networking = mockStatic(CustomersClientNetworking.class);
             var registrations = mockStatic(CustomersClientRegistrations.class);
             var ftb = mockStatic(CustomersFTBClient.class)) {
            CustomersClient.onClientSetup(event);

            verify(event).enqueueWork(work.capture());
            networking.verifyNoInteractions();
            registrations.verifyNoInteractions();
            ftb.verifyNoInteractions();

            work.getValue().run();

            networking.verify(CustomersClientNetworking::initialize);
            registrations.verify(CustomersClientRegistrations::initializeRuntime);
            ftb.verify(CustomersFTBClient::initialize);
        }
    }
}
