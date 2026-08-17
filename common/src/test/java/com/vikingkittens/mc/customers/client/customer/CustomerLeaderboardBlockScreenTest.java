package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import com.vikingkittens.mc.customers.customer.CustomerLeaderboardOpenPayload;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardScores;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerLeaderboardBlockScreenTest {
    @Test
    void doesNotPauseTheGame() {
        CustomerLeaderboardBlockScreen screen = new CustomerLeaderboardBlockScreen(
                new CustomerLeaderboardOpenPayload(BlockPos.ZERO, Map.of())
        );

        assertFalse(screen.isPauseScreen());
    }

    @Test
    void verticallyCentersScoreRowElements() {
        assertEquals(4, CustomerLeaderboardBlockScreen.getRowElementY(0, 8));
        assertEquals(3, CustomerLeaderboardBlockScreen.getRowElementY(0, 9));
        assertEquals(0, CustomerLeaderboardBlockScreen.getRowElementY(0, 16));
    }

    @Test
    void includesTheLevelSectionGapInScrollableHeight() {
        assertEquals(20, CustomerLeaderboardBlockScreen.getLevelSectionHeight(0));
        assertEquals(37, CustomerLeaderboardBlockScreen.getLevelSectionHeight(1));
        assertEquals(54, CustomerLeaderboardBlockScreen.getLevelSectionHeight(2));
    }

    @Test
    void groupsScoresBySpawnerPositionAndMode() {
        BlockPos breakfastPosition = new BlockPos(10, 64, 10);
        BlockPos dinnerPosition = new BlockPos(20, 64, 20);
        List<CustomerLeaderboardBlockScreen.ScoreGroup> groups =
                CustomerLeaderboardBlockScreen.getScoreGroups(Map.of(
                        new CustomerLeaderboardScores.Key(
                                breakfastPosition,
                                CustomerSpawnerMode.BREAKFAST,
                                1,
                                new UUID(0L, 1L)
                        ),
                        new CustomerLeaderboardOpenPayload.Score(0.8F, true),
                        new CustomerLeaderboardScores.Key(
                                breakfastPosition,
                                CustomerSpawnerMode.BREAKFAST,
                                2,
                                new UUID(0L, 2L)
                        ),
                        new CustomerLeaderboardOpenPayload.Score(0.6F, false),
                        new CustomerLeaderboardScores.Key(
                                dinnerPosition,
                                CustomerSpawnerMode.DINNER,
                                1,
                                new UUID(0L, 3L)
                        ),
                        new CustomerLeaderboardOpenPayload.Score(0.4F, false)
                ));

        assertEquals(2, groups.size());
        assertEquals(breakfastPosition, groups.get(0).spawnerPosition());
        assertEquals(CustomerSpawnerMode.BREAKFAST, groups.get(0).spawnerMode());
        assertEquals(List.of(1, 2), groups.get(0).levels());
        assertTrue(groups.get(0).scoresForLevel(1).getFirst().levelPassed());
        assertEquals(dinnerPosition, groups.get(1).spawnerPosition());
        assertEquals(CustomerSpawnerMode.DINNER, groups.get(1).spawnerMode());
    }

    @Test
    void sortsLevelScoresFromHighestToLowest() {
        UUID first = new UUID(0L, 1L);
        UUID second = new UUID(0L, 2L);
        List<CustomerLeaderboardBlockScreen.ScoreGroup> groups =
                CustomerLeaderboardBlockScreen.getScoreGroups(Map.of(
                        new CustomerLeaderboardScores.Key(
                                BlockPos.ZERO,
                                CustomerSpawnerMode.LUNCH,
                                1,
                                first
                        ),
                        new CustomerLeaderboardOpenPayload.Score(0.1F, false),
                        new CustomerLeaderboardScores.Key(
                                BlockPos.ZERO,
                                CustomerSpawnerMode.LUNCH,
                                1,
                                second
                        ),
                        new CustomerLeaderboardOpenPayload.Score(0.9F, true)
                ));

        assertEquals(
                List.of(second, first),
                groups.getFirst().scoresForLevel(1).stream()
                        .map(CustomerLeaderboardBlockScreen.ScoreEntry::playerId)
                        .toList()
        );
    }

    @Test
    void includesTheSpawnerPositionOnlyForDuplicateModes() {
        List<CustomerLeaderboardBlockScreen.ScoreGroup> oneBreakfastGroup =
                CustomerLeaderboardBlockScreen.getScoreGroups(Map.of(
                        new CustomerLeaderboardScores.Key(
                                BlockPos.ZERO,
                                CustomerSpawnerMode.BREAKFAST,
                                1,
                                new UUID(0L, 1L)
                        ),
                        new CustomerLeaderboardOpenPayload.Score(1.0F, true)
                ));
        List<CustomerLeaderboardBlockScreen.ScoreGroup> twoDinnerGroups =
                CustomerLeaderboardBlockScreen.getScoreGroups(Map.of(
                        new CustomerLeaderboardScores.Key(
                                BlockPos.ZERO,
                                CustomerSpawnerMode.DINNER,
                                1,
                                new UUID(0L, 1L)
                        ),
                        new CustomerLeaderboardOpenPayload.Score(1.0F, true),
                        new CustomerLeaderboardScores.Key(
                                new BlockPos(1, 64, 1),
                                CustomerSpawnerMode.DINNER,
                                1,
                                new UUID(0L, 2L)
                        ),
                        new CustomerLeaderboardOpenPayload.Score(0.4F, false)
                ));

        assertFalse(CustomerLeaderboardBlockScreen.shouldShowSpawnerPosition(
                oneBreakfastGroup,
                oneBreakfastGroup.getFirst()
        ));
        assertTrue(CustomerLeaderboardBlockScreen.shouldShowSpawnerPosition(
                twoDinnerGroups,
                twoDinnerGroups.getFirst()
        ));
    }
}
