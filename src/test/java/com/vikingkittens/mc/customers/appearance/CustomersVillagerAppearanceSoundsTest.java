package com.vikingkittens.mc.customers.appearance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersVillagerAppearanceSoundsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void usesAppearanceYesAndNoSounds() {
        CustomersVillagerAppearance appearance =
                mock(CustomersVillagerAppearance.class);
        CustomersVillager villager = mock(CustomersVillager.class);
        SoundEvent yes = mock(SoundEvent.class);
        SoundEvent no = mock(SoundEvent.class);
        when(appearance.getYesSound(villager)).thenReturn(yes);
        when(appearance.getNoSound(villager)).thenReturn(no);

        assertEquals(
                yes,
                CustomersVillagerAppearanceSounds.getYesSound(
                        appearance,
                        villager,
                        SoundEvents.VILLAGER_YES
                )
        );
        assertEquals(
                no,
                CustomersVillagerAppearanceSounds.getNoSound(
                        appearance,
                        villager,
                        SoundEvents.VILLAGER_NO
                )
        );
    }

    @Test
    void usesFallbacksForMissingAppearanceSounds() {
        CustomersVillagerAppearance appearance =
                mock(CustomersVillagerAppearance.class);
        CustomersVillager villager = mock(CustomersVillager.class);

        assertEquals(
                SoundEvents.VILLAGER_YES,
                CustomersVillagerAppearanceSounds.getYesSound(
                        appearance,
                        villager,
                        SoundEvents.VILLAGER_YES
                )
        );
        assertEquals(
                SoundEvents.VILLAGER_NO,
                CustomersVillagerAppearanceSounds.getNoSound(
                        null,
                        villager,
                        SoundEvents.VILLAGER_NO
                )
        );
    }
}
