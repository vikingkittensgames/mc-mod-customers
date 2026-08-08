package com.vikingkittens.mc.customers.appearance;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomersVillagerAppearanceSettingsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void startsWithOnlyDefaultAppearanceEnabled() {
        CustomersVillagerAppearanceSettings settings =
                new CustomersVillagerAppearanceSettings();

        assertEquals(
                List.of(CustomersVillagerAppearances.DEFAULT),
                settings.getEnabledAppearances()
        );
    }

    @Test
    void restoresDefaultWhenAllAppearancesAreDisabled() {
        CustomersVillagerAppearanceSettings settings =
                new CustomersVillagerAppearanceSettings();

        settings.setEnabledAppearances(List.of());

        assertEquals(
                List.of(CustomersVillagerAppearances.DEFAULT),
                settings.getEnabledAppearances()
        );
    }

    @Test
    void roundTripsDistinctRegisteredAndUnknownAppearanceIds() {
        CustomersVillagerAppearanceSettings settings =
                new CustomersVillagerAppearanceSettings();
        ResourceLocation optionalAppearance =
                ResourceLocation.parse("optional_mod:mca");
        settings.setEnabledAppearances(List.of(
                optionalAppearance,
                CustomersVillagerAppearances.DEFAULT,
                optionalAppearance
        ));
        CompoundTag tag = new CompoundTag();

        settings.write(PersistenceCUtils.writer(tag));
        CustomersVillagerAppearanceSettings restored =
                new CustomersVillagerAppearanceSettings();
        restored.read(PersistenceCUtils.reader(tag));

        assertEquals(
                List.of(
                        optionalAppearance,
                        CustomersVillagerAppearances.DEFAULT
                ),
                restored.getEnabledAppearances()
        );
    }

    @Test
    void retainsInitialAppearancesWhenDataIsMissing() {
        CustomersVillagerAppearanceSettings settings =
                new CustomersVillagerAppearanceSettings();

        settings.read(PersistenceCUtils.reader(new CompoundTag()));

        assertEquals(
                CustomersVillagerAppearances.INITIAL_ENABLED,
                settings.getEnabledAppearances()
        );
    }
}
