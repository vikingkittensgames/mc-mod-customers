package com.vikingkittens.mc.customers;

import com.vikingkittens.mc.customers.advancements.CustomersAdvancements;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.common.CustomersCommands;
import com.vikingkittens.mc.customers.common.CustomersCreativeTabs;
import com.vikingkittens.mc.customers.common.CustomersEntityAttributes;
import com.vikingkittens.mc.customers.common.CustomersNetworking;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSeat;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.customer.CustomersCustomerEvents;
import com.vikingkittens.mc.customers.economy.CustomersEconomyEvents;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class Customers {
    public static final String MODID = "customers";

    private Customers() {}

    public static void initialize() {
        CustomersCommands.initialize();
        CustomersEconomyEvents.initialize();
        CustomersNetworking.initialize();
        CustomersAdvancements.initialize();
        CustomersVillagerAppearances.initialize();
        MonsterCustomersVillagerAppearanceEvents.initialize();
        SkinCustomersVillagerAppearanceEvents.initialize();
        CustomerLeaderboard.initialize();
        CustomerPaymentBox.initialize();
        CustomerSeat.initialize();
        CustomerSpawner.initialize();
        CustomerPickupCounter.initialize();
        Customer.initialize();
        CustomersCustomerEvents.initialize();
        SupplierSpawner.initialize();
        Supplier.initialize();
        CustomersCreativeTabs.initialize();
        CustomersEntityAttributes.initialize();
    }
}
