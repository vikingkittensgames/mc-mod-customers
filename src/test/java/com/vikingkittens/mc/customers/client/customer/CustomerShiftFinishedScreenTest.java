package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                .location().toString());
        assertEquals("customers:bling", CustomerShiftFinishedScreen
                .getStarSound(CustomerShiftFinishedScreen.StarState.HALF)
                .location().toString());
        assertEquals("customers:bonk", CustomerShiftFinishedScreen
                .getStarSound(CustomerShiftFinishedScreen.StarState.EMPTY)
                .location().toString());
    }
    /** Includes players who only crafted or only served in shift results. */
    @Test
    void combinesCraftedAndServedPlayerIds() {
        UUID servedOnly =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID craftedOnly =
                UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID both =
                UUID.fromString("33333333-3333-3333-3333-333333333333");

        List<UUID> playerIds =
                CustomerShiftFinishedScreen.getScoredPlayerIds(
                        Map.of(servedOnly, 4, both, 3),
                        Map.of(craftedOnly, 8, both, 7)
                );

        assertEquals(
                List.of(servedOnly, craftedOnly, both),
                playerIds
        );
    }

    /** Excludes players without a positive served or crafted contribution. */
    @Test
    void excludesPlayersWithoutAContribution() {
        UUID inactive =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID active =
                UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertEquals(
                List.of(active),
                CustomerShiftFinishedScreen.getScoredPlayerIds(
                        Map.of(inactive, 0, active, 1),
                        Map.of(inactive, 0)
                )
        );
    }

    /** Omits score icons whose count is zero. */
    @Test
    void rendersOnlyPositiveScores() {
        assertEquals(false,
                CustomerShiftFinishedScreen.shouldRenderScore(0));
        assertEquals(false,
                CustomerShiftFinishedScreen.shouldRenderScore(-1));
        assertEquals(true,
                CustomerShiftFinishedScreen.shouldRenderScore(1));
    }

    @Test
    void scalesScoreIconsToTheTextHeight() {
        assertEquals(
                9.0F / 32.0F,
                CustomerShiftFinishedScreen.getScoreIconScale(9)
        );
    }

    @Test
    void duplicatesPlayersForLayoutTesting() {
        UUID first =
                UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID second =
                UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertEquals(
                List.of(first, second, first, second, first, second),
                CustomerShiftFinishedScreen.duplicatePlayers(
                        List.of(first, second),
                        3
                )
        );
    }

    @Test
    void calculatesPlayerCardWidthFromHeadAndName() {
        assertEquals(
                56,
                CustomerShiftFinishedScreen.getPlayerCardWidth(42)
        );
    }

    @Test
    void packsAsManyPlayerCardsAsFitOnEachRow() {
        assertEquals(
                List.of(
                        List.of(60, 70),
                        List.of(80)
                ),
                CustomerShiftFinishedScreen.getPlayerCardRows(
                        List.of(60, 70, 80),
                        150,
                        4
                )
        );
    }

    @Test
    void keepsAnOversizedPlayerCardInItsOwnRow() {
        assertEquals(
                List.of(
                        List.of(250),
                        List.of(60)
                ),
                CustomerShiftFinishedScreen.getPlayerCardRows(
                        List.of(250, 60),
                        224,
                        4
                )
        );
    }

    @Test
    void calculatesPlayerCardRowWidthIncludingGaps() {
        assertEquals(
                138,
                CustomerShiftFinishedScreen.getPlayerCardRowWidth(
                        List.of(60, 70),
                        8
                )
        );
    }

    @Test
    void centersEachPlayerCardRowBetweenTheMargins() {
        assertEquals(
                128,
                CustomerShiftFinishedScreen.getCenteredPlayerCardRowX(
                        100,
                        16,
                        224,
                        200
                )
        );
    }
}
