package com.vikingkittens.mc.customers.appearance.skins;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceProvider;

public final class SkinPackCustomersVillagerAppearanceProvider
        implements CustomersVillagerAppearanceProvider {
    public static final SkinPackCustomersVillagerAppearanceProvider INSTANCE =
            new SkinPackCustomersVillagerAppearanceProvider();

    private SkinPackCustomersVillagerAppearanceProvider() {
    }

    @Override
    public @Nullable CustomersVillagerAppearance get(
            ResourceLocation appearanceId,
            RegistryAccess registryAccess
    ) {
        Registry<SkinPackCustomersVillagerDefinition> skinPacks =
                registryAccess.registry(
                        SkinCustomersVillagerRegistries.SKIN_PACKS
                ).orElse(null);
        if (skinPacks == null) {
            return null;
        }
        SkinPackCustomersVillagerDefinition skinPack =
                skinPacks.get(appearanceId);
        if (skinPack == null || registryAccess.registry(
                SkinCustomersVillagerRegistries.SKINS
        ).isEmpty()) {
            return null;
        }
        return new SkinPackCustomersVillagerAppearance(
                registryAccess,
                skinPack
        );
    }

    @Override
    public Stream<ResourceLocation> getAvailableIds(
            RegistryAccess registryAccess
    ) {
        return registryAccess.registry(
                        SkinCustomersVillagerRegistries.SKIN_PACKS
                )
                .stream()
                .flatMap(registry -> registry.keySet().stream());
    }
}
