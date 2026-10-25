package com.vikingkittens.mc.customers.client.appearance;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomersVillagerClientAppearanceTest {
    @Test
    void usesDefaultRenderingAdjustments() {
        CustomersVillagerClientAppearance appearance =
                villager -> null;
        CustomersVillager villager = mock(CustomersVillager.class);

        assertEquals(0.0F, appearance.getNameTagOffset(villager));
        assertEquals(0.5F, appearance.getShadowRadius(villager));
        assertEquals(Vec3.ZERO, appearance.getSittingOffset(villager));
    }
}
