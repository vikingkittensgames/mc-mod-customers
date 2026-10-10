package com.vikingkittens.mc.customers.client.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppearanceListScrollTest {
    @Test
    void limitsScrollingToTheAppearanceContent() {
        assertEquals(0, AppearanceListScroll.maximumOffset(5, 20, 120));
        assertEquals(80, AppearanceListScroll.maximumOffset(10, 20, 120));
    }

    @Test
    void clampsWheelScrollingAtBothEnds() {
        assertEquals(20, AppearanceListScroll.afterWheel(0, -1.0D, 10, 20, 120));
        assertEquals(80, AppearanceListScroll.afterWheel(80, -1.0D, 10, 20, 120));
        assertEquals(0, AppearanceListScroll.afterWheel(0, 1.0D, 10, 20, 120));
    }
}
