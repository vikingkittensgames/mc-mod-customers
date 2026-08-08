package com.vikingkittens.mc.customers.appearance;

import java.util.List;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
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
