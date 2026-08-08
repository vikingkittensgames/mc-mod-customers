package com.vikingkittens.mc.customers.appearance.monsters;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;

@EventBusSubscriber(
        modid = Customers.MODID
)
public final class MonsterCustomersVillagerAppearanceEvents {
    private MonsterCustomersVillagerAppearanceEvents() {}

    @SubscribeEvent
    public static void registerAppearances(RegisterEvent event) {
        event.register(
                CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
                registry -> registry.register(
                        MonsterCustomersVillagerAppearance.ID,
                        new MonsterCustomersVillagerAppearance()
                )
        );
    }
}
