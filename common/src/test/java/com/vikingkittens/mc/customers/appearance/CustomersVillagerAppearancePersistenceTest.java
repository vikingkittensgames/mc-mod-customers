package com.vikingkittens.mc.customers.appearance;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomersVillagerAppearancePersistenceTest {
    private static final ResourceLocation TEST_APPEARANCE =
            ResourceLocation.parse("example:test");

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void readsAppearanceAndVariationSeed() {
        DataReader input = mock(DataReader.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        when(input.getString(
                        CustomersVillagerAppearancePersistence.TAG_APPEARANCE
                ))
                .thenReturn(Optional.of(TEST_APPEARANCE.toString()));
        when(input.getFloat(
                        CustomersVillagerAppearancePersistence.TAG_VARIATION_SEED
                ))
                .thenReturn(Optional.of(0.625F));

        CustomersVillagerAppearancePersistence.read(input, villager);

        verify(villager).setAppearanceId(
                TEST_APPEARANCE
        );
        verify(villager).setVariationSeed(0.625F);
    }

    @Test
    void ignoresMissingOrInvalidAppearanceData() {
        DataReader input = mock(DataReader.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        when(input.getString(
                        CustomersVillagerAppearancePersistence.TAG_APPEARANCE
                ))
                .thenReturn(Optional.of("not a resource location"));
        when(input.getFloat(
                        CustomersVillagerAppearancePersistence.TAG_VARIATION_SEED
                ))
                .thenReturn(Optional.empty());

        CustomersVillagerAppearancePersistence.read(input, villager);

        verify(villager, never()).setAppearanceId(
                CustomersVillagerAppearances.DEFAULT
        );
        verify(villager, never()).setVariationSeed(0.0F);
    }

    @Test
    void writesAppearanceAndVariationSeed() {
        DataWriter output = mock(DataWriter.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        when(villager.getAppearanceId())
                .thenReturn(TEST_APPEARANCE);
        when(villager.getVariationSeed()).thenReturn(0.625F);

        CustomersVillagerAppearancePersistence.write(output, villager);

        verify(output).putString(
                CustomersVillagerAppearancePersistence.TAG_APPEARANCE,
                TEST_APPEARANCE.toString()
        );
        verify(output).putFloat(
                CustomersVillagerAppearancePersistence.TAG_VARIATION_SEED,
                0.625F
        );
    }

    @Test
    void readsAdditionalProperties() {
        DataReader input = mock(DataReader.class);
        DataReader property = mock(DataReader.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        when(input.getChildren(CustomersVillagerAppearancePersistence.TAG_ADDITIONAL_PROPERTIES))
                .thenReturn(List.of(property));
        when(property.getString(CustomersVillagerAppearancePersistence.TAG_PROPERTY_KEY))
                .thenReturn(Optional.of("culture"));
        when(property.getString(CustomersVillagerAppearancePersistence.TAG_PROPERTY_VALUE))
                .thenReturn(Optional.of("millenaire:norman"));

        CustomersVillagerAppearancePersistence.read(input, villager);

        verify(villager).setAdditionalProperties(Map.of("culture", "millenaire:norman"));
    }

    @Test
    void missingAdditionalPropertiesClearPreviouslyHeldValues() {
        DataReader input = mock(DataReader.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        when(input.getChildren(CustomersVillagerAppearancePersistence.TAG_ADDITIONAL_PROPERTIES))
                .thenReturn(List.of());

        CustomersVillagerAppearancePersistence.read(input, villager);

        verify(villager).setAdditionalProperties(Map.of());
    }

    @Test
    void writesAdditionalProperties() {
        DataWriter output = mock(DataWriter.class);
        DataWriter property = mock(DataWriter.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        when(villager.getAppearanceId()).thenReturn(TEST_APPEARANCE);
        when(villager.getAdditionalProperties()).thenReturn(Map.of("style", "nordic"));
        when(output.addChild(CustomersVillagerAppearancePersistence.TAG_ADDITIONAL_PROPERTIES))
                .thenReturn(property);

        CustomersVillagerAppearancePersistence.write(output, villager);

        verify(property).putString(CustomersVillagerAppearancePersistence.TAG_PROPERTY_KEY, "style");
        verify(property).putString(CustomersVillagerAppearancePersistence.TAG_PROPERTY_VALUE, "nordic");
    }

    @Test
    void convertsAdditionalPropertiesToAndFromSynchedData() {
        Map<String, String> properties = Map.of(
                "culture",
                "millenaire:norman",
                "villager_type",
                "millenaire:norman_farmer"
        );

        assertEquals(
                properties,
                CustomersVillagerAppearancePersistence.fromSynchedData(
                        CustomersVillagerAppearancePersistence.toSynchedData(properties)
                )
        );
    }
}
