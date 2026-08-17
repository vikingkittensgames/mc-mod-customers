package com.vikingkittens.mc.customers;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.mca.McaCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerSeat;
import com.vikingkittens.mc.customers.customer.CustomerSpawner;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class Customers {
    public static final String MODID = "customers";

    private Customers() {}

    public static void initialize() {
        CustomersVillagerAppearances.initialize();
        MonsterCustomersVillagerAppearanceEvents.initialize();
        McaCustomersVillagerAppearanceEvents.initialize();
        SkinCustomersVillagerAppearanceEvents.initialize();
        CustomerLeaderboard.initialize();
        CustomerPaymentBox.initialize();
        CustomerSeat.initialize();
        CustomerSpawner.initialize();
        CustomerPickupCounter.initialize();
        Customer.initialize();
        SupplierSpawner.initialize();
        Supplier.initialize();
    }
}
