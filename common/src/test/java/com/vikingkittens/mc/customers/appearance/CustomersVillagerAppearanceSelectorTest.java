package com.vikingkittens.mc.customers.appearance;

import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomersVillagerAppearanceSelectorTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void selectsUniformlyFromApplicableAppearances() {
        CustomersVillager villager = mock(CustomersVillager.class);
        CustomersVillagerAppearance unavailable = appearance("unavailable", false);
        CustomersVillagerAppearance first = appearance("first", true);
        CustomersVillagerAppearance second = appearance("second", true);
        IntUnaryOperator randomIndex = mock(IntUnaryOperator.class);
        when(randomIndex.applyAsInt(2)).thenReturn(1);

        CustomersVillagerAppearance selected = CustomersVillagerAppearanceSelector.selectApplicable(
                List.of(unavailable, first, second), villager, randomIndex);

        assertEquals(second, selected);
    }

    @Test
    void returnsNullWhenNoAppearanceIsApplicable() {
        CustomersVillager villager = mock(CustomersVillager.class);

        CustomersVillagerAppearance selected = CustomersVillagerAppearanceSelector.selectApplicable(
                List.of(appearance("unavailable", false)),
                villager,
                mock(IntUnaryOperator.class)
        );

        assertNull(selected);
    }

    @Test
    void retainsTheIdOfADynamicallyResolvedAppearance() {
        CustomersVillager villager = mock(CustomersVillager.class);
        ResourceLocation skinPackId =
                ResourceLocation.parse("customers:mc_skins");
        CustomersVillagerAppearance skinPack =
                appearance("MC Skins", true);

        ResourceLocation selected =
                CustomersVillagerAppearanceSelector.selectApplicableId(
                        List.of(skinPackId),
                        ignored -> skinPack,
                        villager,
                        ignored -> 0
                );

        assertEquals(skinPackId, selected);
    }

    @Test
    void appliesPropertiesProvidedByTheSelectedAppearance() {
        CustomersVillager villager = mock(CustomersVillager.class);
        ResourceLocation appearanceId = ResourceLocation.parse("example:contextual");
        Map<String, String> properties = Map.of("culture", "millenaire:norman");
        CustomersVillagerAppearance appearance = new CustomersVillagerAppearance() {
            @Override
            public Component getName() {
                return Component.literal("Contextual");
            }

            @Override
            public Map<String, String> getAdditionalProperties(CustomersVillager customer) {
                return properties;
            }
        };

        CustomersVillagerAppearances.applySelected(appearanceId, appearance, villager);

        verify(villager).setAppearanceId(appearanceId);
        verify(villager).setAdditionalProperties(properties);
    }

    @Test
    void appliesTheNameProvidedByTheSelectedAppearance() {
        CustomersVillager villager = mock(CustomersVillager.class);
        Component name = Component.translatable("name.example.shopkeeper");
        CustomersVillagerAppearance appearance = new CustomersVillagerAppearance() {
            @Override
            public Component getName() {
                return Component.literal("Named");
            }

            @Override
            public Component getVillagerName(CustomersVillager customer) {
                return name;
            }
        };

        CustomersVillagerAppearances.applySelected(ResourceLocation.parse("example:named"), appearance, villager);

        verify(villager).setVillagerName(name);
    }

    @Test
    void leavesTheVillagerNameAvailableToOtherModsWhenTheAppearanceDoesNotProvideOne() {
        CustomersVillager villager = mock(CustomersVillager.class);

        CustomersVillagerAppearances.applySelected(
                ResourceLocation.parse("example:unnamed"),
                appearance("Unnamed", true),
                villager
        );

        verify(villager, never()).setVillagerName(org.mockito.ArgumentMatchers.any());
    }

    private static CustomersVillagerAppearance appearance(String name, boolean applicable) {
        return new CustomersVillagerAppearance() {
            @Override
            public Component getName() {
                return Component.literal(name);
            }

            @Override
            public boolean isApplicable(CustomersVillager villager) {
                return applicable;
            }
        };
    }
}
