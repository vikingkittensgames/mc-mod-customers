package com.vikingkittens.mc.customers.advancements.ftb;

import java.util.function.Predicate;

import net.neoforged.fml.ModList;

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
     * Built-in task implementations:
     * AdvancementTask checks a completed advancement or criterion.
     * StatTask checks a player's current custom-statistic value.
     *
     * Prefer an Advancement Task for Customers goals already represented by
     * advancements. Examples include crafting either spawner, serving the
     * first item, reaching an existing milestone, serving during a particular
     * shift, or satisfying a data-driven customers:item_served criterion.
     *
     * Prefer a Stat Task when only a per-player numeric total matters and a
     * dedicated advancement would add no value. Examples include reaching an
     * arbitrary customers:item_served or customers:shift_finished total chosen
     * by a modpack author.
     *
     * FTB's ObjectStarted, ObjectProgress, and ObjectCompleted events report
     * quest lifecycle changes. They do not replace Customers InternalEvents as
     * the source of gameplay facts. CustomersFTBEvents independently consumes
     * ItemServed, ShiftFinished, and future Customers events before updating
     * matching FTB tasks.
     *
     * Use a custom task when the requirement needs event values or shared team
     * accumulation that a player advancement or statistic cannot express. The
     * initial customers:pet_items_served task supports a quest such as "Feed
     * 25 Customer Pets Together." Every qualifying ItemServed event increments
     * the task once for the serving player's FTB team.
     *
     * Possible future custom tasks include accumulating items crafted or served
     * during completed shifts, reaching a combined team shift score, completing
     * a shift with no abandoned customers, or tracking supplier events whose
     * values are not represented by a persistent player statistic.
     *
     * FTB grouping is quest data rather than task registration. A suggested
     * structure mirroring the Customers advancement tree is:
     *
     * Customers chapter group
     * - Builder chapter
     *   - Customer Spawner quest: Advancement Task
     *   - Supplier Spawner quest: Advancement Task
     * - Customer Service chapter
     *   - First Item Served quest: Advancement Task
     *   - Service Milestones: Stat or Advancement Tasks
     *   - Feed Customer Pets Together: PetItemsServedTask
     * - Supplier Service chapter
     *   - Supplier quests as their events and advancements are added
     *
     * Quest dependencies reproduce the advancement tree's edges. Multiple
     * tasks can be placed in one quest, sequential tasks can enforce ordering,
     * and dependencies can connect quests across chapters.
     *
     * Every class in this package belongs to the NeoForge module because FTB
     * Quests 1.21.1 has no Forge artifact. CustomersFTB must remain free of
     * dev.ftb imports so it can safely perform the availability check before
     * the JVM loads CustomersFTBTasks or CustomersFTBEvents.
     */

    private CustomersFTB() {}

    public static void initialize() {
        if (!isEnabled()) {
            return;
        }
        CustomersFTBTasks.initialize();
        CustomersFTBEvents.initialize();
    }

    public static boolean isEnabled() {
        return isEnabled(ModList.get()::isLoaded);
    }

    static boolean isEnabled(Predicate<String> loadedMods) {
        return loadedMods.test(MOD_ID);
    }
}
