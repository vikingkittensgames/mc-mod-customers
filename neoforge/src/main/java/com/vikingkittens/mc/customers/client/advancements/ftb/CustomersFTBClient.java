package com.vikingkittens.mc.customers.client.advancements.ftb;

import com.vikingkittens.mc.customers.advancements.ftb.CustomersFTB;

public final class CustomersFTBClient {
    private CustomersFTBClient() {}

    public static void initialize() {
        if (CustomersFTB.isEnabled()) {
            CustomersFTBTaskGuiProvider.initialize();
        }
    }
}
