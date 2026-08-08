package com.vikingkittens.mc.customers.appearance;

import java.util.Comparator;
import java.util.List;
import java.util.function.IntUnaryOperator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerRegistries;
import com.vikingkittens.mc.customers.appearance.skins.SkinPackCustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.skins.SkinPackCustomersVillagerDefinition;

public final class CustomersVillagerAppearances {
    public static final ResourceLocation DEFAULT =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "default");
    public static final ResourceLocation MONSTERS =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "monsters");
    public static final List<ResourceLocation> INITIAL_ENABLED =
            List.of(DEFAULT);

    private static final DeferredRegister<CustomersVillagerAppearance> APPEARANCES =
            DeferredRegister.create(
                    CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
                    Customers.MODID
            );

    public static final DeferredHolder<
                    CustomersVillagerAppearance,
                    CustomersVillagerAppearance>
            DEFAULT_APPEARANCE = APPEARANCES.register(
                    DEFAULT.getPath(),
                    DefaultCustomersVillagerAppearance::new
            );
    private CustomersVillagerAppearances() {}

    public static void register(IEventBus modEventBus) {
        APPEARANCES.register(modEventBus);
    }

    public static ResourceLocation select(
            List<ResourceLocation> enabledAppearanceIds,
            CustomersVillager villager,
            IntUnaryOperator randomIndex
    ) {
        ResourceLocation selectedId =
                CustomersVillagerAppearanceSelector.selectApplicableId(
                        enabledAppearanceIds,
                        appearanceId -> get(
                                appearanceId,
                                villager.registryAccess()
                        ),
                        villager,
                        randomIndex
                );
        return selectedId == null ? DEFAULT : selectedId;
    }

    public static @Nullable CustomersVillagerAppearance get(
            CustomersVillager villager
    ) {
        return get(
                villager.getAppearanceId(),
                villager.registryAccess()
        );
    }

    public static @Nullable CustomersVillagerAppearance get(
            ResourceLocation appearanceId,
            RegistryAccess registryAccess
    ) {
        CustomersVillagerAppearance registered =
                CustomersVillagerAppearance.APPEARANCE_REGISTRY.get(
                        appearanceId
                );
        if (registered != null || registryAccess == null) {
            return registered;
        }
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

    public static List<ResourceLocation> getAvailableAppearanceIds(
            RegistryAccess registryAccess
    ) {
        java.util.stream.Stream<ResourceLocation> registered =
                CustomersVillagerAppearance.APPEARANCE_REGISTRY
                        .keySet()
                        .stream();
        java.util.stream.Stream<ResourceLocation> skinPacks =
                registryAccess.registry(
                                SkinCustomersVillagerRegistries.SKIN_PACKS
                        )
                        .stream()
                        .flatMap(registry -> registry.keySet().stream())
                        .filter(appearanceId ->
                                !CustomersVillagerAppearance
                                        .APPEARANCE_REGISTRY
                                        .containsKey(appearanceId)
                        );
        return java.util.stream.Stream.concat(registered, skinPacks)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    public static Component getName(
            ResourceLocation appearanceId,
            RegistryAccess registryAccess
    ) {
        CustomersVillagerAppearance appearance =
                get(appearanceId, registryAccess);
        return appearance == null
                ? Component.literal(appearanceId.toString())
                : appearance.getName();
    }
}
