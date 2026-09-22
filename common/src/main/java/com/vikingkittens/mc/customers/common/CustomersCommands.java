package com.vikingkittens.mc.customers.common;

import dev.architectury.event.events.common.CommandRegistrationEvent;

import com.vikingkittens.mc.customers.customer.CustomerCommands;
import com.vikingkittens.mc.customers.supplier.SupplierCommands;

public final class CustomersCommands {
    private CustomersCommands() {}

    public static void initialize() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, selection) -> {
            CustomerCommands.register(dispatcher);
            SupplierCommands.register(dispatcher);
        });
    }
}
