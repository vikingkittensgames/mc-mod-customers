package com.vikingkittens.mc.customers.appearance;

import java.util.List;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.vikingkittens.mc.customers.Customers;

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
        List<CustomersVillagerAppearance> enabledAppearances =
                enabledAppearanceIds.stream()
                        .map(CustomersVillagerAppearance.APPEARANCE_REGISTRY::get)
                        .filter(Objects::nonNull)
                        .toList();
        CustomersVillagerAppearance selected =
                CustomersVillagerAppearanceSelector.selectApplicable(
                        enabledAppearances,
                        villager,
                        randomIndex
                );
        if (selected == null) {
            return DEFAULT;
        }
        ResourceLocation selectedId =
                CustomersVillagerAppearance.APPEARANCE_REGISTRY.getKey(selected);
        return selectedId == null ? DEFAULT : selectedId;
    }

    public static @Nullable CustomersVillagerAppearance get(
            CustomersVillager villager
    ) {
        return CustomersVillagerAppearance.APPEARANCE_REGISTRY.get(
                villager.getAppearanceId()
        );
    }
}
