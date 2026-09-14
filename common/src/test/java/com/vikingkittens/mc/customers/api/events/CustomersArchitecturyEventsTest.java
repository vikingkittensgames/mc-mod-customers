package com.vikingkittens.mc.customers.api.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomersArchitecturyEventsTest {
    @Test
    void enablesBridgeOnlyWhenArchitecturyIsLoaded() {
        assertTrue(CustomersArchitecturyEvents.isEnabled("architectury"::equals));
        assertFalse(CustomersArchitecturyEvents.isEnabled(modId -> false));
    }
}
