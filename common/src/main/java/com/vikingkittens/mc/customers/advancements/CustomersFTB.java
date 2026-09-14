package com.vikingkittens.mc.customers.advancements;

import java.util.function.Predicate;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomersFTB {
    public static final String MOD_ID = "ftbquests";

    /*
     * FTB Quests integration references:
     *
     * Documentation:
     * https://docs.feed-the-beast.com/mod-docs/mods/suite/Quests/
     *
     * Minecraft 1.21.1 source:
     * https://github.com/FTBTeam/FTB-Quests/tree/1.21.1/main
     *
     * Advancement task implementation:
     * https://github.com/FTBTeam/FTB-Quests/blob/1.21.1/main/common/src/main/java/dev/ftb/mods/ftbquests/quest/task/AdvancementTask.java
     *
     * Maven artifacts:
     * https://maven.ftb.dev/releases/dev/ftb/mods/
     *
     * Prefer an FTB Advancement Task referencing a Customers advancement when
     * the vanilla advancement criterion can express the quest requirement.
     * This avoids an FTB-specific task type and lets the same advancement work
     * with vanilla clients, commands, and other advancement-aware mods.
     *
     * Add a custom FTB task or direct progress bridge only when an advancement
     * cannot represent the requirement. Likely examples include repeatable
     * transaction totals, accumulating event values across multiple serves,
     * team-specific progress, or inspecting event data without defining a
     * separate advancement for every combination.
     *
     * Keep customer and supplier gameplay independent from FTB Quests. Listen
     * to Customers InternalEvents here, resolve the relevant FTB team/task on
     * the logical server, and update progress through the version-pinned FTB
     * API. Register custom TaskType implementations during FTB initialization
     * and keep their serialization, display text, icons, and progress rules
     * with the task implementation.
     *
     * Never load an FTB API class until isEnabled() is true. Minecraft 1.21.1
     * FTB Quests is distributed for NeoForge but not Forge, so direct API code
     * belongs in the NeoForge module or behind a reflection-safe common bridge.
     */

    private CustomersFTB() {}

    public static void initialize() {
        if (!isEnabled()) {
            return;
        }
    }

    public static boolean isEnabled() {
        return isEnabled(CustomersServices.platform()::isModLoaded);
    }

    static boolean isEnabled(Predicate<String> loadedMods) {
        return loadedMods.test(MOD_ID);
    }
}
