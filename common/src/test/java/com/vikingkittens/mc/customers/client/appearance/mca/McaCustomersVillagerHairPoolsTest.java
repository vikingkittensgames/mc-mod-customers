package com.vikingkittens.mc.customers.client.appearance.mca;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.conczin.mca.entity.ai.relationship.Gender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class McaCustomersVillagerHairPoolsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void selectsCurrentHairStyleListWhenBothApisAreAvailable() {
        assertEquals(
                "net.conczin.mca.resources.HairStyleList",
                McaCustomersVillagerHairPools.getHairListClassName(ignored -> true)
        );
    }

    @Test
    void fallsBackToLegacyHairListWhenCurrentApiIsUnavailable() {
        assertEquals(
                "net.conczin.mca.resources.HairList",
                McaCustomersVillagerHairPools.getHairListClassName(
                        "net.conczin.mca.resources.HairList"::equals
                )
        );
    }

    @Test
    void returnsNoHairApiWhenNeitherApiIsAvailable() {
        assertNull(McaCustomersVillagerHairPools.getHairListClassName(ignored -> false));
    }

    @Test
    void selectsAUsableHairFromTheInstalledMcaVersion() throws ReflectiveOperationException {
        Class<?> hairStyleList = findClass("net.conczin.mca.resources.HairStyleList");
        if (hairStyleList != null) {
            assertCurrentHairStyleSelection(hairStyleList);
        } else {
            assertLegacyHairSelection();
        }
    }

    private static void assertCurrentHairStyleSelection(Class<?> hairStyleList)
            throws ReflectiveOperationException {
        HairListInstance instance = instance(hairStyleList);
        Object hairList = instance.value();
        Map<String, Object> styles = values(hairStyleList, hairList, "styles");
        Map<String, Object> original = new HashMap<>(styles);
        try {
            Object style = Class.forName("net.conczin.mca.resources.data.skin.HairStyle")
                    .getConstructor(
                            String.class,
                            Gender.class,
                            float.class,
                            String.class,
                            String.class,
                            String.class,
                            String.class,
                            String.class
                    )
                    .newInstance(
                            "customers:test_style",
                            Gender.FEMALE,
                            1.0F,
                            "customers:test_base",
                            "",
                            "",
                            "",
                            ""
                    );
            styles.clear();
            styles.put("customers:test_style", style);

            McaCustomersVillagerHairPools.Selection selection =
                    McaCustomersVillagerHairPools.select(Gender.FEMALE, 0.0F);

            assertNotNull(selection);
            assertEquals("customers:test_style", selection.id());
            assertNotNull(selection.style());
            assertEquals(
                    "customers:test_base",
                    selection.style().getClass().getMethod("base").invoke(selection.style())
            );
        } finally {
            styles.clear();
            styles.putAll(original);
            instance.restore();
        }
    }

    private static void assertLegacyHairSelection() throws ReflectiveOperationException {
        Class<?> hairListClass = Class.forName("net.conczin.mca.resources.HairList");
        HairListInstance instance = instance(hairListClass);
        Object hairList = instance.value();
        Map<String, Object> hair = values(hairListClass, hairList, "hair");
        Map<String, Object> original = new HashMap<>(hair);
        try {
            Object legacyHair = Class.forName("net.conczin.mca.resources.data.skin.Hair")
                    .getConstructor(String.class, Gender.class, float.class)
                    .newInstance("customers:test_hair", Gender.FEMALE, 1.0F);
            hair.clear();
            hair.put("customers:test_hair", legacyHair);

            McaCustomersVillagerHairPools.Selection selection =
                    McaCustomersVillagerHairPools.select(Gender.FEMALE, 0.0F);

            assertNotNull(selection);
            assertEquals("customers:test_hair", selection.id());
            assertNull(selection.style());
        } finally {
            hair.clear();
            hair.putAll(original);
            instance.restore();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> values(
            Class<?> hairListClass,
            Object hairList,
            String fieldName
    ) throws NoSuchFieldException, IllegalAccessException {
        return (Map<String, Object>) hairListClass.getField(fieldName).get(hairList);
    }

    private static HairListInstance instance(Class<?> hairListClass)
            throws ReflectiveOperationException {
        Object original = hairListClass.getMethod("getInstance").invoke(null);
        if (original != null) {
            return new HairListInstance(null, null, original);
        }

        Field field = hairListClass.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        Object value = hairListClass.getConstructor().newInstance();
        field.set(null, value);
        return new HairListInstance(field, null, value);
    }

    private record HairListInstance(
            Field field,
            Object original,
            Object value
    ) {
        void restore() throws IllegalAccessException {
            if (field != null) {
                field.set(null, original);
            }
        }
    }

    private static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }
}
