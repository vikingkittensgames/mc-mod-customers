package com.vikingkittens.mc.customers.appearance;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

/**
 * Supplies appearances whose IDs and definitions come from sources other than
 * the static appearance registry.
 */
public interface CustomersVillagerAppearanceProvider {
    @Nullable
    CustomersVillagerAppearance get(
            ResourceLocation appearanceId,
            RegistryAccess registryAccess
    );

    Stream<ResourceLocation> getAvailableIds(
            RegistryAccess registryAccess
    );
}
