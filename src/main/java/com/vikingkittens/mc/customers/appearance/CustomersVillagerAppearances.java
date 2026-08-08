package com.vikingkittens.mc.customers.appearance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.vikingkittens.mc.customers.Customers;
public final class CustomersVillagerAppearances {
    public static final ResourceLocation DEFAULT =
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "default");
    public static final List<ResourceLocation> INITIAL_ENABLED =
            List.of(DEFAULT);

    private static final DeferredRegister<CustomersVillagerAppearance> APPEARANCES =
            DeferredRegister.create(
                    CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
                    Customers.MODID
            );
    private static final List<CustomersVillagerAppearanceProvider> PROVIDERS =
            new ArrayList<>();

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

    public static void registerProvider(
            CustomersVillagerAppearanceProvider provider
    ) {
        if (!PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
        }
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
        for (CustomersVillagerAppearanceProvider provider : PROVIDERS) {
            CustomersVillagerAppearance appearance =
                    provider.get(appearanceId, registryAccess);
            if (appearance != null) {
                return appearance;
            }
        }
        return null;
    }

    public static List<ResourceLocation> getAvailableAppearanceIds(
            RegistryAccess registryAccess
    ) {
        Stream<ResourceLocation> registered =
                CustomersVillagerAppearance.APPEARANCE_REGISTRY
                        .keySet()
                        .stream();
        Stream<ResourceLocation> provided = PROVIDERS.stream()
                .flatMap(provider ->
                        provider.getAvailableIds(registryAccess)
                )
                        .filter(appearanceId ->
                                !CustomersVillagerAppearance
                                        .APPEARANCE_REGISTRY
                                        .containsKey(appearanceId)
                        );
        return Stream.concat(registered, provided)
                .distinct()
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
