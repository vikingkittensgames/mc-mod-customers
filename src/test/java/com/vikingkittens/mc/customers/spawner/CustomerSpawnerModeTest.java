package com.vikingkittens.mc.customers.spawner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSpawnerModeTest {
    private static final float EPSILON = 0.0001F;

    @ParameterizedTest
    @MethodSource("progressVisibilityCases")
    void shouldShowProgressOnlyForTimedModes(CustomerSpawnerMode mode, boolean expected) {
        assertEquals(expected, CustomerSpawnerMode.shouldShowProgress(mode));
    }

    @ParameterizedTest
    @MethodSource("spawnCases")
    void shouldSpawnMatchesConfiguredTimeWindows(CustomerSpawnerMode mode, int minuteOfDay, boolean expected) {
        assertEquals(expected, CustomerSpawnerMode.shouldSpawn(mode, ticksSinceMidnightAtMinute(minuteOfDay)));
    }

    @ParameterizedTest
    @MethodSource("progressCases")
    void generateProgressReturnsLinearProgressWithinActiveWindow(CustomerSpawnerMode mode, int minuteOfDay, float expected) {
        assertEquals(expected, CustomerSpawnerMode.generateProgress(mode, ticksSinceMidnightAtMinute(minuteOfDay)), EPSILON);
    }

    @Test
    void continuousAndManualAlwaysSpawnAndNeverShowProgress() {
        for (int minuteOfDay : new int[] {0, clockMinute(5, 0), clockMinute(12, 0), clockMinute(23, 59)}) {
            assertTrue(CustomerSpawnerMode.shouldSpawn(CustomerSpawnerMode.CONTINUOUS, ticksSinceMidnightAtMinute(minuteOfDay)));
            assertTrue(CustomerSpawnerMode.shouldSpawn(CustomerSpawnerMode.MANUAL, ticksSinceMidnightAtMinute(minuteOfDay)));
            assertEquals(0.0F, CustomerSpawnerMode.generateProgress(CustomerSpawnerMode.CONTINUOUS, ticksSinceMidnightAtMinute(minuteOfDay)), EPSILON);
            assertEquals(0.0F, CustomerSpawnerMode.generateProgress(CustomerSpawnerMode.MANUAL, ticksSinceMidnightAtMinute(minuteOfDay)), EPSILON);
        }
    }

    @Test
    void modeTimeUsesTicksSinceMidnight() {
        assertEquals(0L, ticksSinceMidnightAtMinute(clockMinute(0, 0)));
        assertEquals(6000L, ticksSinceMidnightAtMinute(clockMinute(6, 0)));
        assertEquals(12000L, ticksSinceMidnightAtMinute(clockMinute(12, 0)));
        assertEquals(18000L, ticksSinceMidnightAtMinute(clockMinute(18, 0)));
    }

    @Test
    void lunchEndsBeforeSunsetWhenUsingTicksSinceMidnight() {
        assertTrue(CustomerSpawnerMode.shouldSpawn(CustomerSpawnerMode.LUNCH, ticksSinceMidnightAtMinute(clockMinute(15, 29))));
        assertFalse(CustomerSpawnerMode.shouldSpawn(CustomerSpawnerMode.LUNCH, ticksSinceMidnightAtMinute(clockMinute(15, 30))));
        assertFalse(CustomerSpawnerMode.shouldSpawn(CustomerSpawnerMode.LUNCH, ticksSinceMidnightAtMinute(clockMinute(18, 0))));
    }

    private static Stream<Arguments> progressVisibilityCases() {
        return Stream.of(
                Arguments.of(CustomerSpawnerMode.CONTINUOUS, false),
                Arguments.of(CustomerSpawnerMode.MANUAL, false),
                Arguments.of(CustomerSpawnerMode.DAY, true),
                Arguments.of(CustomerSpawnerMode.NIGHT, true),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, true),
                Arguments.of(CustomerSpawnerMode.LUNCH, true),
                Arguments.of(CustomerSpawnerMode.DINNER, true)
        );
    }

    private static Stream<Arguments> spawnCases() {
        return Stream.of(
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(4, 59), false),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(5, 0), true),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(12, 0), true),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(18, 59), true),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(19, 0), false),

                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(4, 59), true),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(5, 0), false),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(16, 59), false),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(17, 0), true),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(23, 59), true),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(0, 0), true),

                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(5, 29), false),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(5, 30), true),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(8, 0), true),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(10, 29), true),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(10, 30), false),

                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(11, 29), false),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(11, 30), true),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(13, 30), true),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(15, 29), true),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(15, 30), false),

                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(16, 29), false),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(16, 30), true),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(18, 45), true),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(20, 59), true),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(21, 0), false)
        );
    }

    private static Stream<Arguments> progressCases() {
        return Stream.of(
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(4, 59), 0.0F),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(5, 0), 0.0F),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(12, 0), 0.5F),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(18, 59), 839.0F / 840.0F),
                Arguments.of(CustomerSpawnerMode.DAY, clockMinute(19, 0), 0.0F),

                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(16, 59), 0.0F),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(17, 0), 0.0F),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(23, 0), 0.5F),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(4, 59), 719.0F / 720.0F),
                Arguments.of(CustomerSpawnerMode.NIGHT, clockMinute(5, 0), 0.0F),

                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(5, 29), 0.0F),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(5, 30), 0.0F),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(8, 0), 0.5F),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(10, 29), 299.0F / 300.0F),
                Arguments.of(CustomerSpawnerMode.BREAKFAST, clockMinute(10, 30), 0.0F),

                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(11, 29), 0.0F),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(11, 30), 0.0F),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(13, 30), 0.5F),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(15, 29), 239.0F / 240.0F),
                Arguments.of(CustomerSpawnerMode.LUNCH, clockMinute(15, 30), 0.0F),

                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(16, 29), 0.0F),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(16, 30), 0.0F),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(18, 45), 0.5F),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(20, 59), 269.0F / 270.0F),
                Arguments.of(CustomerSpawnerMode.DINNER, clockMinute(21, 0), 0.0F)
        );
    }

    private static int clockMinute(int hour, int minute) {
        return hour * 60 + minute;
    }

    private static long ticksSinceMidnightAtMinute(int minuteOfDay) {
        return (minuteOfDay * 24000L + 1439L) / 1440L;
    }
}

