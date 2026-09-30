package com.vikingkittens.mc.customers.appearance;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

/**
 * Supplies appearances whose IDs and definitions come from sources other than
 * the static appearance registry.
 */
public interface CustomersVillagerAppearanceProvider {
    @Nullable
    CustomersVillagerAppearance get(
            Identifier appearanceId,
            RegistryAccess registryAccess
    );

    Stream<Identifier> getAvailableIds(
            RegistryAccess registryAccess
    );
}
