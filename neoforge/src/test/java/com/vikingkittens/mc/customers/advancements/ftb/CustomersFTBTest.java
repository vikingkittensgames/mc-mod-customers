package com.vikingkittens.mc.customers.advancements.ftb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomersFTBTest {
    @Test
    void enablesIntegrationOnlyWhenFtbQuestsIsLoaded() {
        assertTrue(CustomersFTB.isEnabled("ftbquests"::equals));
        assertFalse(CustomersFTB.isEnabled(modId -> false));
    }
}
