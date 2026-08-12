package com.vikingkittens.mc.customers.client.appearance;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomersVillagerClientAppearanceTest {
    @Test
    void usesNoSittingOffsetByDefault() {
        CustomersVillagerClientAppearance appearance =
                villager -> null;

        assertEquals(
                Vec3.ZERO,
                appearance.getSittingOffset(mock(
                        com.vikingkittens.mc.customers.appearance.CustomersVillager.class
                ))
        );
    }
}
