package com.vikingkittens.mc.customers.spawner;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

import static com.ibm.icu.lang.UCharacter.GraphemeClusterBreak.L;

public enum CustomerSpawnerMode implements StringRepresentable {
    CONTINUOUS("continuous"),
    MANUAL("manual"),
    DAY("day"),
    NIGHT("night"),
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    DINNER("dinner");

    private final String name;

    CustomerSpawnerMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Component getTitle() {
        return Component.translatable("titles.customers.spawn_mode." + name);
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    private static int timeToMinuteOfDay(long timeOfDay) {
        return (int)(timeOfDay * 1440L / 24000L); // 0 - 1439
    }
    private static int clockToMinuteOfDay(int hour, int minute) {
        return hour * 60 + minute;
    }
    private static final int DAY_START = clockToMinuteOfDay(5, 0);
    private static final int DAY_END = clockToMinuteOfDay(19, 0);
    private static final int NIGHT_START = clockToMinuteOfDay(17, 0);
    private static final int NIGHT_END = clockToMinuteOfDay(5, 0);
    private static final int BREAKFAST_START = clockToMinuteOfDay(5, 30);
    private static final int BREAKFAST_END = clockToMinuteOfDay(10, 30);
    private static final int LUNCH_START = clockToMinuteOfDay(11, 30);
    private static final int LUNCH_END = clockToMinuteOfDay(15, 30);
    private static final int DINNER_START = clockToMinuteOfDay(16, 30);
    private static final int DINNER_END = clockToMinuteOfDay(21, 0);

    private static float generateProgress(int minuteOfDay, int startMinute, int endMinute) {
        if (startMinute < endMinute) {
            if (minuteOfDay < startMinute || minuteOfDay >= endMinute) {
                return 0.0F;
            }
            return (float)(minuteOfDay - startMinute) / (float)(endMinute - startMinute);
        }

        int duration = (1440 - startMinute) + endMinute;
        if (minuteOfDay >= startMinute) {
            return (float)(minuteOfDay - startMinute) / (float)duration;
        }
        if (minuteOfDay < endMinute) {
            return (float)(minuteOfDay + 1440 - startMinute) / (float)duration;
        }
        return 0.0F;
    }

    public static boolean shouldShowProgress(CustomerSpawnerMode spawnerMode) {
        return switch (spawnerMode) {
            case DAY -> true;
            case NIGHT -> true;
            case BREAKFAST -> true;
            case LUNCH -> true;
            case DINNER -> true;
            default -> false;
        };
    }

    public static float generateProgress(CustomerSpawnerMode spawnerMode, long timeOfDay) {
        int minuteOfDay = timeToMinuteOfDay(timeOfDay);

        return switch (spawnerMode) {
            case DAY -> generateProgress(minuteOfDay, DAY_START, DAY_END);
            case NIGHT -> generateProgress(minuteOfDay, NIGHT_START, NIGHT_END);
            case BREAKFAST -> generateProgress(minuteOfDay, BREAKFAST_START, BREAKFAST_END);
            case LUNCH -> generateProgress(minuteOfDay, LUNCH_START, LUNCH_END);
            case DINNER -> generateProgress(minuteOfDay, DINNER_START, DINNER_END);
            default -> 0.0F;
        };
    }

    public static boolean shouldSpawn(CustomerSpawnerMode spawnerMode, long timeOfDay) {
        int minuteOfDay = timeToMinuteOfDay(timeOfDay);

        return switch (spawnerMode) {
            case DAY -> minuteOfDay >= DAY_START && minuteOfDay < DAY_END;
            case NIGHT -> minuteOfDay >= NIGHT_START || minuteOfDay < NIGHT_END;
            case BREAKFAST -> minuteOfDay >= BREAKFAST_START && minuteOfDay < BREAKFAST_END;
            case LUNCH -> minuteOfDay >= LUNCH_START && minuteOfDay < LUNCH_END;
            case DINNER -> minuteOfDay >= DINNER_START && minuteOfDay < DINNER_END;
            default -> true;
        };
    }
}
