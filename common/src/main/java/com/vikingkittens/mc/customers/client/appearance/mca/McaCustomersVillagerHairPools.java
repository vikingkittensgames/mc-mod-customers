package com.vikingkittens.mc.customers.client.appearance.mca;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.WeightedPool;
import org.jetbrains.annotations.Nullable;

final class McaCustomersVillagerHairPools {
    private static final String CURRENT_HAIR_LIST =
            "net.conczin.mca.resources.HairStyleList";
    private static final String LEGACY_HAIR_LIST =
            "net.conczin.mca.resources.HairList";

    private McaCustomersVillagerHairPools() {}

    record Selection(String id, @Nullable Object style) {}

    static @Nullable Selection select(Gender gender, float choice) {
        Selection selection = select(CURRENT_HAIR_LIST, gender, choice, true);
        return selection == null ? select(LEGACY_HAIR_LIST, gender, choice, false) : selection;
    }

    static void apply(VillagerEntityMCA villager, Selection selection) {
        if (selection.style() == null) {
            villager.setHair(selection.id());
            return;
        }
        try {
            villager.getClass()
                    .getMethod("setHairStyle", selection.style().getClass())
                    .invoke(villager, selection.style());
        } catch (IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | LinkageError exception) {
            villager.setHair(selection.id());
        }
    }

    static @Nullable String getHairListClassName(Predicate<String> classExists) {
        if (classExists.test(CURRENT_HAIR_LIST)) {
            return CURRENT_HAIR_LIST;
        }
        return classExists.test(LEGACY_HAIR_LIST) ? LEGACY_HAIR_LIST : null;
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, McaCustomersVillagerHairPools.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Selection select(
            String className,
            Gender gender,
            float choice,
            boolean usesHairStyles
    ) {
        try {
            Class<?> hairListClass = Class.forName(className);
            Object hairList = hairListClass.getMethod("getInstance").invoke(null);
            if (hairList == null) {
                return null;
            }
            Object pool = hairListClass.getMethod("getPool", Gender.class).invoke(hairList, gender);
            if (!(pool instanceof WeightedPool<?>)) {
                return null;
            }
            WeightedPool.Entry<String> selected =
                    McaCustomersVillagerWeightedSelector.select(
                            ((WeightedPool<String>) pool).getEntries(),
                            WeightedPool.Entry::getWeight,
                            choice,
                            null
                    );
            if (selected == null) {
                return null;
            }
            Object style = usesHairStyles
                    ? hairListClass.getMethod("get", String.class)
                            .invoke(hairList, selected.getValue())
                    : null;
            return usesHairStyles && style == null
                    ? null
                    : new Selection(selected.getValue(), style);
        } catch (ClassNotFoundException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException | LinkageError exception) {
            return null;
        }
    }
}
