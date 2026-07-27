package com.vikingkittens.mc.customers.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigTest {
    @Test
    void maxCounterDistanceDefaultsTo64() {
        assertEquals(64, Config.MAX_COUNTER_DISTANCE.getDefault());
    }

    @Test
    void maxCustomersDefaultsTo4() {
        assertEquals(4, Config.MAX_CUSTOMERS.getDefault());
    }

    @Test
    void customerGiveUpSecondsDefaultsTo120() {
        assertEquals(120, Config.CUSTOMER_GIVE_UP_SECONDS.getDefault());
    }

    @Test
    void buildCommandsAreDisabledByDefault() {
        assertEquals(false, Config.ENABLE_BUILD_COMMANDS.getDefault());
    }

    @Test
    void quickSellIsDisabledByDefault() {
        assertEquals(false, Config.ENABLE_QUICK_SELL.getDefault());
    }
}