package com.vikingkittens.mc.customers.common;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Customers.MODID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> CUSTOMERS_TAB = TABS.register(
            "customers",
            () -> CreativeTabRegistry.create(
                    Component.translatable("itemGroup.customers"),
                    () -> new ItemStack(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get())
            )
    );

    private CustomersCreativeTabs() {}

    public static void initialize() {
        TABS.register();
        CreativeTabRegistry.append(CUSTOMERS_TAB, CustomerSpawner.CUSTOMER_SPAWNER_ITEM);
        CreativeTabRegistry.append(CUSTOMERS_TAB, SupplierSpawner.SUPPLIER_SPAWNER_ITEM);
        CustomerPaymentBox.ITEMS.values().forEach(item ->
                CreativeTabRegistry.append(CUSTOMERS_TAB, item));
        CustomerPickupCounter.ITEMS.values().forEach(item ->
                CreativeTabRegistry.append(CUSTOMERS_TAB, item));
        CreativeTabRegistry.append(CUSTOMERS_TAB, CustomerLeaderboard.ITEM);
    }
}
