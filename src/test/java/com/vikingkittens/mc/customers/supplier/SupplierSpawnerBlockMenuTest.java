package com.vikingkittens.mc.customers.supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupplierSpawnerBlockMenuTest {
    @Test
    void placesFourPairsWithFourPixelsBetweenPairs() {
        assertEquals(8, SupplierSpawnerBlockMenu.getContainerSlotX(0));
        assertEquals(26, SupplierSpawnerBlockMenu.getContainerSlotX(1));
        assertEquals(48, SupplierSpawnerBlockMenu.getContainerSlotX(2));
        assertEquals(66, SupplierSpawnerBlockMenu.getContainerSlotX(3));
        assertEquals(88, SupplierSpawnerBlockMenu.getContainerSlotX(4));
        assertEquals(106, SupplierSpawnerBlockMenu.getContainerSlotX(5));
        assertEquals(128, SupplierSpawnerBlockMenu.getContainerSlotX(6));
        assertEquals(146, SupplierSpawnerBlockMenu.getContainerSlotX(7));
    }

    @Test
    void mapsVisibleSlotsAroundEachHiddenNinthColumn() {
        assertEquals(0, SupplierSpawnerBlockMenu.getContainerSlotIndex(0, 0));
        assertEquals(7, SupplierSpawnerBlockMenu.getContainerSlotIndex(0, 7));
        assertEquals(9, SupplierSpawnerBlockMenu.getContainerSlotIndex(1, 0));
        assertEquals(16, SupplierSpawnerBlockMenu.getContainerSlotIndex(1, 7));
        assertEquals(52, SupplierSpawnerBlockMenu.getContainerSlotIndex(5, 7));
    }
}
