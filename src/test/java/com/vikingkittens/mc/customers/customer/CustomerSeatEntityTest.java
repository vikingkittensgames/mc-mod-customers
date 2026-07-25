package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSeatEntityTest {
    @Test
    void recognizesSeatNames() {
        assertTrue(CustomerSeatLogic.isSeatName("oak_seat"));
        assertTrue(CustomerSeatLogic.isSeatName("dining_chair"));
        assertTrue(CustomerSeatLogic.isSeatName("bar_stool"));
        assertTrue(CustomerSeatLogic.isSeatName("park_bench"));
        assertTrue(CustomerSeatLogic.isSeatName("red_cushion"));
        assertFalse(CustomerSeatLogic.isSeatName("stone"));
    }

    @Test
    void recognizesSeatHeightWithinMiddleThird() {
        assertFalse(CustomerSeatLogic.isSeatHeight(0.25));
        assertTrue(CustomerSeatLogic.isSeatHeight(1.0 / 3.0));
        assertTrue(CustomerSeatLogic.isSeatHeight(0.5));
        assertTrue(CustomerSeatLogic.isSeatHeight(2.0 / 3.0));
        assertFalse(CustomerSeatLogic.isSeatHeight(0.75));
    }

    @Test
    void usesLargestTopSurfaceForComplexShape() {
        assertEquals(
                0.5,
                CustomerSeatLogic.getBestSeatHeight(
                        Shapes.or(
                                Shapes.box(0, 0, 0, 1, 0.5, 1),
                                Shapes.box(0, 0.5, 0.75, 1, 1, 1)
                        )
                )
        );
    }

    @Test
    void emptyShapeDefaultsToBlockCenter() {
        assertEquals(
                0.5,
                CustomerSeatLogic.getBestSeatHeight(Shapes.empty())
        );
    }

    @Test
    void emptySeatExpiresAfterTwoMinutes() {
        assertFalse(CustomerSeatLogic.shouldDiscardEmptySeat(2_399));
        assertTrue(CustomerSeatLogic.shouldDiscardEmptySeat(2_400));
    }
}
