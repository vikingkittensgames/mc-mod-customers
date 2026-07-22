package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerShiftFinishedScreenTest {
    @Test
    void fiftyPercentProducesTwoAndAHalfStars() {
        assertEquals(CustomerShiftFinishedScreen.StarState.FULL, CustomerShiftFinishedScreen.getStarState(0.5F, 0));
        assertEquals(CustomerShiftFinishedScreen.StarState.FULL, CustomerShiftFinishedScreen.getStarState(0.5F, 1));
        assertEquals(CustomerShiftFinishedScreen.StarState.HALF, CustomerShiftFinishedScreen.getStarState(0.5F, 2));
        assertEquals(CustomerShiftFinishedScreen.StarState.EMPTY, CustomerShiftFinishedScreen.getStarState(0.5F, 3));
        assertEquals(CustomerShiftFinishedScreen.StarState.EMPTY, CustomerShiftFinishedScreen.getStarState(0.5F, 4));
    }

    @Test
    void clampsScoresToStarRatingRange() {
        for (int index = 0; index < 5; index++) {
            assertEquals(CustomerShiftFinishedScreen.StarState.EMPTY, CustomerShiftFinishedScreen.getStarState(-1.0F, index));
            assertEquals(CustomerShiftFinishedScreen.StarState.FULL, CustomerShiftFinishedScreen.getStarState(2.0F, index));
        }
    }

    @Test
    void roundsScoresToNearestHalfStar() {
        assertEquals(CustomerShiftFinishedScreen.StarState.HALF, CustomerShiftFinishedScreen.getStarState(0.1F, 0));
        assertEquals(CustomerShiftFinishedScreen.StarState.FULL, CustomerShiftFinishedScreen.getStarState(0.2F, 0));
    }
}
