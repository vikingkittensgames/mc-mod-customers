package com.vikingkittens.mc.customers.appearance.mca;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;

@EventBusSubscriber(modid = Customers.MODID)
public final class McaCustomersVillagerAppearanceEvents {
    private McaCustomersVillagerAppearanceEvents() {}

    @SubscribeEvent
    public static void registerAppearances(RegisterEvent event) {
        if (!McaCustomersVillagerMod.isLoaded()) {
            return;
        }
        event.register(
                CustomersVillagerAppearance.APPEARANCE_REGISTRY_KEY,
                registry -> registry.register(
                        CustomersVillagerAppearances.MCA,
                        new McaCustomersVillagerAppearance()
                )
        );
    }
}
