package com.vikingkittens.mc.customers.appearance.monsters;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.sounds.SoundEvents;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonsterCustomersVillagerAppearanceTest {
    private final MonsterCustomersVillagerAppearance appearance =
            new MonsterCustomersVillagerAppearance();

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void appliesToCustomersInEveryModeButNotSuppliers() {
        CustomersVillager customer = villager(0.0F);
        when(customer.getCustomersVillagerType())
                .thenReturn(CustomersVillagerType.CUSTOMER_NORMAL);

        assertTrue(appearance.isApplicable(customer));

        when(customer.getCustomersVillagerType())
                .thenReturn(CustomersVillagerType.SUPPLIER);
        assertFalse(appearance.isApplicable(customer));
    }

    @Test
    void usesVariationSeedForMonsterSounds() {
        CustomersVillager zombie = villager(0.0F);
        CustomersVillager skeleton = villager(1.0F / 6.0F);
        CustomersVillager witch = villager(2.0F / 6.0F);

        assertEquals(
                SoundEvents.ZOMBIE_AMBIENT,
                appearance.getAmbientSound(zombie)
        );
        assertEquals(
                SoundEvents.SKELETON_HURT,
                appearance.getHurtSound(skeleton)
        );
        assertEquals(
                SoundEvents.WITCH_DEATH,
                appearance.getDeathSound(witch)
        );
        assertNull(appearance.getStepSound(witch));
    }

    @Test
    void usesUnderwaterDrownedSounds() {
        CustomersVillager drowned = villager(4.0F / 6.0F);
        when(drowned.isInWater()).thenReturn(true);

        assertEquals(
                SoundEvents.DROWNED_AMBIENT_WATER,
                appearance.getAmbientSound(drowned)
        );
        assertEquals(
                SoundEvents.DROWNED_HURT_WATER,
                appearance.getHurtSound(drowned)
        );
        assertEquals(
                SoundEvents.DROWNED_DEATH_WATER,
                appearance.getDeathSound(drowned)
        );
        assertEquals(
                SoundEvents.DROWNED_STEP,
                appearance.getStepSound(drowned)
        );
    }

    private static CustomersVillager villager(float variationSeed) {
        CustomersVillager villager = mock(CustomersVillager.class);
        when(villager.getVariationSeed()).thenReturn(variationSeed);
        return villager;
    }
}
