package com.vikingkittens.mc.customers.appearance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import dev.architectury.registry.registries.RegistrySupplier;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.Customers;

public final class CustomersVillagerAppearances {
    public static final Identifier DEFAULT =
            Identifier.fromNamespaceAndPath(Customers.MODID, "default");
    public static final List<Identifier> INITIAL_ENABLED =
            List.of(DEFAULT);

    private static final List<CustomersVillagerAppearanceProvider> PROVIDERS =
            new ArrayList<>();

    public static RegistrySupplier<DefaultCustomersVillagerAppearance> DEFAULT_APPEARANCE;

    private CustomersVillagerAppearances() {}

    public static void initialize() {
        DEFAULT_APPEARANCE = CustomersVillagerAppearanceRegistries.appearances().register(
                DEFAULT,
                DefaultCustomersVillagerAppearance::new
        );
    }

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
                                villager.getCustomersRegistryAccess()
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
                villager.getCustomersRegistryAccess()
        );
    }

    public static @Nullable CustomersVillagerAppearance get(
            Identifier appearanceId,
        RegistryAccess registryAccess
    ) {
        CustomersVillagerAppearance registered = CustomersVillagerAppearanceRegistries.appearances().get(appearanceId);
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
        Stream<Identifier> registered = CustomersVillagerAppearanceRegistries.appearances().getIds().stream();
        Stream<Identifier> provided = PROVIDERS.stream()
                .flatMap(provider ->
                        provider.getAvailableIds(registryAccess)
                )
                        .filter(appearanceId ->
                        !CustomersVillagerAppearanceRegistries.appearances().contains(appearanceId)
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
