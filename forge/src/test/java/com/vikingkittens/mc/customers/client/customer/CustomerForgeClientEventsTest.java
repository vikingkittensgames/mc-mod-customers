package com.vikingkittens.mc.customers.client.customer;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
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

    @Test
    void registersGlassModelsByRawResourceId() {
        ModelEvent.RegisterAdditional event =
                mock(ModelEvent.RegisterAdditional.class);
        ArgumentCaptor<ResourceLocation> models =
                ArgumentCaptor.forClass(ResourceLocation.class);

        CustomerForgeClientEvents.registerAdditionalModels(event);

        verify(event, times(4)).register(models.capture());
        assertEquals(
                CustomersDisplayCaseGlassRenderer.modelIds(),
                models.getAllValues()
        );
        models.getAllValues().forEach(model ->
                assertEquals(ResourceLocation.class, model.getClass())
        );
    }

    @Test
    void resolvesGlassModelsByRawResourceId() {
        ResourceLocation modelId = new ResourceLocation(
                "customers",
                "block/display_case_glass_single"
        );
        BakedModel expected = mock(BakedModel.class);
        AtomicReference<ResourceLocation> requestedModel =
                new AtomicReference<>();

        BakedModel actual = CustomerForgeClientEvents.resolveAdditionalModel(
                id -> {
                    requestedModel.set(id);
                    return expected;
                },
                modelId
        );

        assertSame(expected, actual);
        assertSame(modelId, requestedModel.get());
    }
}
