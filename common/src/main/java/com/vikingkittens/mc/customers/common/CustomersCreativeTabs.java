package com.vikingkittens.mc.customers.common;

import java.util.Comparator;

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
import com.vikingkittens.mc.customers.customer.CustomerSpawnerItem;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerItem;

public final class CustomersCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Customers.MODID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> CUSTOMERS_TAB = TABS.register(
            "customers",
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.customers"))
                    .icon(() -> new ItemStack(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get());
                        output.accept(SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get());
                        CustomerPaymentBox.ITEMS.values().forEach(item -> output.accept(item.get()));
                        CustomerPickupCounter.ITEMS.values().forEach(item -> output.accept(item.get()));
                        output.accept(CustomerLeaderboard.ITEM.get());
                        parameters.holders().lookup(SpawnerPresets.CUSTOMER).ifPresent(registry ->
                                registry.listElements()
                                        .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                                        .forEach(holder -> output.accept(SpawnerPresets.create(
                                                holder.key().location(), holder.value(),
                                                CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get(),
                                                parameters.holders(), CustomerSpawnerItem::applyPreset))));
                        parameters.holders().lookup(SpawnerPresets.SUPPLIER).ifPresent(registry ->
                                registry.listElements()
                                        .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                                        .forEach(holder -> output.accept(SpawnerPresets.create(
                                                holder.key().location(), holder.value(),
                                                SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get(),
                                                parameters.holders(), SupplierSpawnerItem::applyPreset))));
                    }))
    );

    private CustomersCreativeTabs() {}

    public static void initialize() {
        TABS.register();
    }
}
