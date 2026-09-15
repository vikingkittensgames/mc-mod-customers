package com.vikingkittens.mc.customers.advancements;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;

public final class CustomersAdvancements {
    public static final String ADVANCEMENT_ICON_NAME = "advancement_icon";

    private static final IRegistrationHelper REGISTRATIONS =
            CustomersServices.registration();

    public static final CustomersRegistryEntry<Item, Item> ADVANCEMENT_ICON =
            REGISTRATIONS.register(
                    Registries.ITEM,
                    ADVANCEMENT_ICON_NAME,
                    () -> new Item(new Item.Properties())
            );

    private CustomersAdvancements() {}

    public static void initialize() {
        CustomersFTB.initialize();
        CustomersTriggers.initialize();
        CustomersStatistics.initialize();
        CustomersAdvancementEvents.initialize();
    }
}
