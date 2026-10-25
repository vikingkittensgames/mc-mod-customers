package com.vikingkittens.mc.customers.appearance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomersVillagerRegistryAccessTest {
    @Test
    void usesAStableCustomMethodNameThatSurvivesProductionRemapping() {
        assertDoesNotThrow(() ->
                CustomersVillager.class.getDeclaredMethod("getCustomersRegistryAccess"));
        assertDoesNotThrow(() ->
                CustomersVillager.class.getDeclaredMethod("getCustomersLevel"));
        assertDoesNotThrow(() ->
                CustomersVillager.class.getDeclaredMethod("getCustomersSpawnerPosition"));
        assertThrows(NoSuchMethodException.class, () ->
                CustomersVillager.class.getDeclaredMethod("registryAccess"));
    }
}
