package com.vikingkittens.mc.customers.appearance;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomersVillagerAppearancePersistenceTest {
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
                .thenReturn(Optional.of("customers:monsters"));
        when(input.getFloat(
                        CustomersVillagerAppearancePersistence.TAG_VARIATION_SEED
                ))
                .thenReturn(Optional.of(0.625F));

        CustomersVillagerAppearancePersistence.read(input, villager);

        verify(villager).setAppearanceId(
                CustomersVillagerAppearances.MONSTERS
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
                .thenReturn(CustomersVillagerAppearances.MONSTERS);
        when(villager.getVariationSeed()).thenReturn(0.625F);

        CustomersVillagerAppearancePersistence.write(output, villager);

        verify(output).putString(
                CustomersVillagerAppearancePersistence.TAG_APPEARANCE,
                "customers:monsters"
        );
        verify(output).putFloat(
                CustomersVillagerAppearancePersistence.TAG_VARIATION_SEED,
                0.625F
        );
    }
}
