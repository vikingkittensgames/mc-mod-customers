package com.vikingkittens.mc.customers.appearance.skins;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceProvider;
import com.vikingkittens.mc.customers.compatability.RegistryCUtils;

public final class SkinPackCustomersVillagerAppearanceProvider
        implements CustomersVillagerAppearanceProvider {
    public static final SkinPackCustomersVillagerAppearanceProvider INSTANCE =
            new SkinPackCustomersVillagerAppearanceProvider();

    private SkinPackCustomersVillagerAppearanceProvider() {
    }

    @Override
    public @Nullable CustomersVillagerAppearance get(
            Identifier appearanceId,
            RegistryAccess registryAccess
    ) {
        Registry<SkinPackCustomersVillagerDefinition> skinPacks =
                RegistryCUtils.lookup(
                        registryAccess,
                        SkinCustomersVillagerRegistries.SKIN_PACKS
                ).orElse(null);
        if (skinPacks == null) {
            return null;
        }
        SkinPackCustomersVillagerDefinition skinPack =
                RegistryCUtils.getValue(skinPacks, appearanceId);
        if (skinPack == null || RegistryCUtils.lookup(
                registryAccess,
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
    public Stream<Identifier> getAvailableIds(
            RegistryAccess registryAccess
    ) {
        return RegistryCUtils.lookup(
                        registryAccess,
                        SkinCustomersVillagerRegistries.SKIN_PACKS
                )
                .stream()
                .flatMap(registry -> registry.keySet().stream());
    }
}
