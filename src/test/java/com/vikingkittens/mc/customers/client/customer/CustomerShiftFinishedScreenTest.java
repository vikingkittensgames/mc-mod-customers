package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void animatesStarsOneAtATimeFromLeftToRight() {
        assertEquals(0.0F, CustomerShiftFinishedScreen.getStarScale(0L, 0));
        assertTrue(CustomerShiftFinishedScreen.getStarScale(250L, 0) > 1.0F);
        assertEquals(1.0F, CustomerShiftFinishedScreen.getStarScale(500L, 0));

        assertEquals(0.0F, CustomerShiftFinishedScreen.getStarScale(499L, 1));
        assertEquals(0.0F, CustomerShiftFinishedScreen.getStarScale(500L, 1));
        assertTrue(CustomerShiftFinishedScreen.getStarScale(750L, 1) > 1.0F);
        assertEquals(1.0F, CustomerShiftFinishedScreen.getStarScale(1000L, 1));

        assertEquals(0.0F, CustomerShiftFinishedScreen.getStarScale(1999L, 4));
        assertEquals(1.0F, CustomerShiftFinishedScreen.getStarScale(2500L, 4));
    }

    @Test
    void elasticEasingStartsAtZeroBouncesAndSettlesAtOne() {
        assertEquals(0.0F, CustomerShiftFinishedScreen.easeOutElastic(0.0F));
        assertTrue(CustomerShiftFinishedScreen.easeOutElastic(0.5F) > 1.0F);
        assertEquals(1.0F, CustomerShiftFinishedScreen.easeOutElastic(1.0F));
    }

    @Test
    void playsFilledStarSoundWhenItsAnimationStarts() {
        assertEquals(false, CustomerShiftFinishedScreen.shouldPlayStarSound(
                499L, 1, false));
        assertEquals(true, CustomerShiftFinishedScreen.shouldPlayStarSound(
                500L, 1, false));
    }

    @Test
    void playsEachStarSoundOnlyOnceWhenItsAnimationStarts() {
        assertEquals(true, CustomerShiftFinishedScreen.shouldPlayStarSound(
                1000L, 2, false));
        assertEquals(false, CustomerShiftFinishedScreen.shouldPlayStarSound(
                1000L, 2, true));
    }

    @Test
    void selectsBlingForFilledStarsAndBonkForEmptyStars() {
        assertEquals("customers:bling", CustomerShiftFinishedScreen
                .getStarSound(CustomerShiftFinishedScreen.StarState.FULL)
                .getLocation().toString());
        assertEquals("customers:bling", CustomerShiftFinishedScreen
                .getStarSound(CustomerShiftFinishedScreen.StarState.HALF)
                .getLocation().toString());
        assertEquals("customers:bonk", CustomerShiftFinishedScreen
                .getStarSound(CustomerShiftFinishedScreen.StarState.EMPTY)
                .getLocation().toString());
    }
}
