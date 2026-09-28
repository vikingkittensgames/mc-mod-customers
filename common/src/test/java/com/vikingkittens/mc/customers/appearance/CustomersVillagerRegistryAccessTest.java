package com.vikingkittens.mc.customers.appearance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomersVillagerRegistryAccessTest {
    @Test
    void usesAStableCustomMethodNameThatSurvivesProductionRemapping() {
        assertDoesNotThrow(() ->
                CustomersVillager.class.getDeclaredMethod("getCustomersRegistryAccess"));
        assertThrows(NoSuchMethodException.class, () ->
                CustomersVillager.class.getDeclaredMethod("registryAccess"));
    }
}
