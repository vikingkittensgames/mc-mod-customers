package com.vikingkittens.mc.customers.compatability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForgePlatformHelperTest {
    @Test
    void loadsForgeConfigProvider() {
        assertEquals(
                ForgeConfigHelper.class,
                CustomersServices.config().getClass()
        );
    }

    @Test
    void loadsForgeRegistrationProvider() {
        assertEquals(
                ForgeRegistrationHelper.class,
                CustomersServices.registration().getClass()
        );
    }

    @Test
    void loadsForgePlatformProvider() {
        assertEquals(
                ForgePlatformHelper.class,
                CustomersServices.platform().getClass()
        );
    }
}
