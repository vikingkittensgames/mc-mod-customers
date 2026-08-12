package com.vikingkittens.mc.customers.config;

import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.compatability.IConfigHelper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecipeEnabledConditionTest {
    @Test
    void usesCustomerSpawnerRecipeSetting() {
        IConfigHelper config = mock(IConfigHelper.class);
        when(config.customerSpawnerRecipeEnabled()).thenReturn(true);

        assertTrue(new RecipeEnabledCondition(RecipeEnabledCondition.CUSTOMER_SPAWNER_BLOCK).test(config));
    }

    @Test
    void usesSupplierSpawnerRecipeSetting() {
        IConfigHelper config = mock(IConfigHelper.class);
        when(config.supplierSpawnerRecipeEnabled()).thenReturn(false);

        assertFalse(new RecipeEnabledCondition(RecipeEnabledCondition.SUPPLIER_SPAWNER_BLOCK).test(config));
    }

    @Test
    void rejectsUnknownRecipeSetting() {
        assertFalse(new RecipeEnabledCondition("unknown").test(mock(IConfigHelper.class)));
    }
}
