package com.vikingkittens.mc.customers.client.appearance.mca;

import java.lang.ref.WeakReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.appearance.CustomersVillager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McaCustomersVillagerProxyStateTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void usesTheAdultMinecraftAge() {
        assertEquals(0, McaCustomersVillagerProxyState.adultAge());
    }

    @Test
    void reportsSittingVillagersAsPassengers() {
        CustomersVillager villager = mock(CustomersVillager.class);
        when(villager.isSitting()).thenReturn(true);

        assertTrue(
                McaCustomersVillagerProxyState.isPassenger(
                        new WeakReference<>(villager)
                )
        );
    }

    @Test
    void doesNotReportStandingOrMissingVillagersAsPassengers() {
        CustomersVillager villager = mock(CustomersVillager.class);

        assertFalse(
                McaCustomersVillagerProxyState.isPassenger(
                        new WeakReference<>(villager)
                )
        );
        assertFalse(
                McaCustomersVillagerProxyState.isPassenger(null)
        );
        assertFalse(
                McaCustomersVillagerProxyState.isPassenger(
                        new WeakReference<>(null)
                )
        );
    }

    @Test
    void exposesTheSourceVillagersVehicle() {
        Mob source = mock(Mob.class);
        Entity vehicle = mock(Entity.class);
        when(source.getVehicle()).thenReturn(vehicle);

        assertEquals(
                vehicle,
                McaCustomersVillagerProxyState.vehicle(
                        new WeakReference<>(source)
                )
        );
        assertEquals(
                null,
                McaCustomersVillagerProxyState.vehicle(null)
        );
    }

    @Test
    void offsetsScaledModelsToKeepTheirHipsOnTheSeat() {
        assertEquals(
                new Vec3(0.0D, 0.1875D, 0.0D),
                McaCustomersVillagerProxyState.sittingOffset(0.75F)
        );
        assertEquals(
                Vec3.ZERO,
                McaCustomersVillagerProxyState.sittingOffset(1.0F)
        );
        assertEquals(
                new Vec3(0.0D, -0.1875D, 0.0D),
                McaCustomersVillagerProxyState.sittingOffset(1.25F)
        );
    }
}
