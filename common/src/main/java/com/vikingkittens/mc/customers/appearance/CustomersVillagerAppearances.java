package com.vikingkittens.mc.customers.appearance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.RegistryCUtils;

public final class CustomersVillagerAppearances {
    public static final Identifier DEFAULT =
            Identifier.fromNamespaceAndPath(Customers.MODID, "default");
    public static final List<Identifier> INITIAL_ENABLED =
            List.of(DEFAULT);

    private static final List<CustomersVillagerAppearanceProvider> PROVIDERS =
            new ArrayList<>();

    public static final CustomersRegistryEntry<
            CustomersVillagerAppearance,
            DefaultCustomersVillagerAppearance
    > DEFAULT_APPEARANCE = CustomersServices.registration().register(
            CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
            DEFAULT.getPath(),
            DefaultCustomersVillagerAppearance::new
    );

    private CustomersVillagerAppearances() {}

    public static void initialize() {}

    public static void registerProvider(
            CustomersVillagerAppearanceProvider provider
    ) {
        if (!PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
        }
    }

    public static Identifier select(
            List<Identifier> enabledAppearanceIds,
            CustomersVillager villager,
            IntUnaryOperator randomIndex
    ) {
        Identifier selectedId =
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
            Identifier appearanceId,
        RegistryAccess registryAccess
    ) {
        CustomersVillagerAppearance registered = RegistryCUtils.getValue(
                CustomersVillagerAppearance.APPEARANCE_REGISTRY.get(),
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

    public static List<Identifier> getAvailableAppearanceIds(
            RegistryAccess registryAccess
    ) {
        Stream<Identifier> registered =
                CustomersVillagerAppearance.APPEARANCE_REGISTRY.get()
                        .keySet()
                        .stream();
        Stream<Identifier> provided = PROVIDERS.stream()
                .flatMap(provider ->
                        provider.getAvailableIds(registryAccess)
                )
                        .filter(appearanceId ->
                                !CustomersVillagerAppearance
                                        .APPEARANCE_REGISTRY
                                        .get()
                                        .containsKey(appearanceId)
                        );
        return Stream.concat(registered, provided)
                .distinct()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();
    }

    public static Component getName(
            Identifier appearanceId,
            RegistryAccess registryAccess
    ) {
        CustomersVillagerAppearance appearance =
                get(appearanceId, registryAccess);
        return appearance == null
                ? Component.literal(appearanceId.toString())
                : appearance.getName();
    }
}
