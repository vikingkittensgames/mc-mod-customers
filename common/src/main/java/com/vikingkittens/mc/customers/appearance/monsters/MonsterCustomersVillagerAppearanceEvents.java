package com.vikingkittens.mc.customers.appearance.monsters;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class MonsterCustomersVillagerAppearanceEvents {
    public static final CustomersRegistryEntry<
            CustomersVillagerAppearance,
            MonsterCustomersVillagerAppearance
    > APPEARANCE = CustomersServices.registration().register(
            CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
            MonsterCustomersVillagerAppearance.ID.getPath(),
            MonsterCustomersVillagerAppearance::new
    );

    private MonsterCustomersVillagerAppearanceEvents() {}

    public static void initialize() {}
}
