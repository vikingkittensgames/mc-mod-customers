package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSpawnerBlockMenuTest {
    @Test
    void shiftsOnlyTheCostColumnEightPixelsRight() {
        assertEquals(8, CustomerSpawnerBlockMenu.getContainerSlotX(0));
        assertEquals(134, CustomerSpawnerBlockMenu.getContainerSlotX(7));
        assertEquals(156, CustomerSpawnerBlockMenu.getContainerSlotX(8));
    }

    @Test
    void validatesMaxCustomerText() {
        assertTrue(CustomerSpawnerBlockMenu.isValidMaxCustomersText(""));
        assertTrue(CustomerSpawnerBlockMenu.isValidMaxCustomersText("1"));
        assertTrue(CustomerSpawnerBlockMenu.isValidMaxCustomersText("99"));
        assertFalse(CustomerSpawnerBlockMenu.isValidMaxCustomersText("0"));
        assertFalse(CustomerSpawnerBlockMenu.isValidMaxCustomersText("100"));
        assertFalse(CustomerSpawnerBlockMenu.isValidMaxCustomersText("abc"));
    }
}
