package com.vikingkittens.mc.customers.advancements;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.RegistrationCUtils;

public final class CustomersAdvancements {
    public static final String ADVANCEMENT_ICON_NAME = "advancement_icon";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Customers.MODID, Registries.ITEM);

    public static final RegistrySupplier<Item> ADVANCEMENT_ICON = RegistrationCUtils.registerItem(
                    ITEMS,
                    ADVANCEMENT_ICON_NAME,
                    key -> new Item(new Item.Properties().setId(key))
            );

    private CustomersAdvancements() {}

    public static void initialize() {
        ITEMS.register();
        CustomersTriggers.initialize();
        CustomersStatistics.initialize();
        CustomersAdvancementEvents.initialize();
    }
}
