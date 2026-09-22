package com.vikingkittens.mc.customers.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomersRecipeConditionsTest {
    @Test
    void readsEachRecipeToggleFromTheConfiguration() {
        CustomersConfig config = new CustomersConfig(
                false,
                true,
                false,
                64,
                64,
                4,
                120,
                false,
                false,
                true,
                true,
                true,
                false
        );

        assertFalse(CustomersRecipeConditions.isEnabled(
                config,
                CustomersRecipeConditions.CUSTOMER_SPAWNER_BLOCK
        ));
        assertTrue(CustomersRecipeConditions.isEnabled(
                config,
                CustomersRecipeConditions.SUPPLIER_SPAWNER_BLOCK
        ));
        assertFalse(CustomersRecipeConditions.isEnabled(
                config,
                CustomersRecipeConditions.CUSTOMER_LEADERBOARD_BLOCK
        ));
    }

    @Test
    void rejectsUnknownRecipeToggles() {
        assertFalse(CustomersRecipeConditions.isEnabled(
                CustomersConfig.DEFAULT,
                "unknown"
        ));
    }
}
