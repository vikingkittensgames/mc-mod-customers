package com.vikingkittens.mc.customers.spawner;

import com.mojang.logging.LogUtils;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public enum CustomerSpawnerMode implements StringRepresentable {
    CONTINUOUS("continuous"),
    MANUAL("manual"),
    HOURLY("hourly"),
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

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean shouldSpawn(CustomerSpawnerMode spawnerMode, long timeOfDay, boolean powered, boolean pulsed) {
        int hour = (int) ((timeOfDay / 1000) + 6) % 24;
        int minute = (int) (((timeOfDay % 1000) / 1000.0) * 60);
        return false;
    }
}
