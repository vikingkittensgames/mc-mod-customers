package com.vikingkittens.mc.customers.appearance.mca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaCustomersVillagerModTest {
    @Test
    void detectsMcaByItsModId() {
        assertTrue(McaCustomersVillagerMod.isLoaded("mca"::equals));
    }

    @Test
    void reportsMcaMissingWhenItsModIdIsAbsent() {
        assertFalse(McaCustomersVillagerMod.isLoaded(ignored -> false));
    }
}
