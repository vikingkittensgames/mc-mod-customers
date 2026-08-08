package com.vikingkittens.mc.customers.client.appearance.mca;

import java.lang.ref.WeakReference;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;

final class McaCustomersVillagerProxyState {
    private static final int ADULT_AGE = 0;
    private static final double HUMANOID_SEATED_HIP_HEIGHT = 0.75D;

    private McaCustomersVillagerProxyState() {}

    static int adultAge() {
        return ADULT_AGE;
    }

    static boolean isPassenger(
            @Nullable WeakReference<CustomersVillager> source
    ) {
        CustomersVillager villager =
                source == null ? null : source.get();
        return villager != null && villager.isSitting();
    }

    static @Nullable Entity vehicle(
            @Nullable WeakReference<Mob> source
    ) {
        Mob entity = source == null ? null : source.get();
        return entity == null ? null : entity.getVehicle();
    }

    static Vec3 sittingOffset(float verticalScale) {
        return new Vec3(
                0.0D,
                HUMANOID_SEATED_HIP_HEIGHT
                        * (1.0D - verticalScale),
                0.0D
        );
    }
}
