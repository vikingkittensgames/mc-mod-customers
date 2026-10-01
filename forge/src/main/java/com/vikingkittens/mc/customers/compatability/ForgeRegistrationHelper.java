package com.vikingkittens.mc.customers.compatability;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;

public final class ForgeRegistrationHelper implements IRegistrationHelper {
    private final List<Consumer<DataPackRegistryEvent.NewRegistry>> dataPackRegistries =
            new ArrayList<>();

    @Override
    public synchronized <T> void registerDataPackRegistry(
            ResourceKey<Registry<T>> registryKey,
            Codec<T> codec
    ) {
        dataPackRegistries.add(
                event -> event.dataPackRegistry(
                        registryKey,
                        codec,
                        codec
                )
        );
    }

    public void bind(IEventBus eventBus) {
        eventBus.addListener(this::registerDataPackRegistries);
    }

    void registerDataPackRegistries(
            DataPackRegistryEvent.NewRegistry event
    ) {
        dataPackRegistries.forEach(
                registration -> registration.accept(event)
        );
    }
}
