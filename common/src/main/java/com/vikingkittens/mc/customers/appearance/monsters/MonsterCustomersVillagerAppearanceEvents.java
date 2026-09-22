package com.vikingkittens.mc.customers.appearance.monsters;

import dev.architectury.registry.registries.RegistrySupplier;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceRegistries;

public final class MonsterCustomersVillagerAppearanceEvents {
    public static final RegistrySupplier<MonsterCustomersVillagerAppearance> APPEARANCE =
            CustomersVillagerAppearanceRegistries.appearances().register(
            MonsterCustomersVillagerAppearance.ID,
            MonsterCustomersVillagerAppearance::new
    );

    private MonsterCustomersVillagerAppearanceEvents() {}

    public static void initialize() {}
}
