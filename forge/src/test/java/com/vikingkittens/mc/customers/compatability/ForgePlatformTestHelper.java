package com.vikingkittens.mc.customers.compatability;

import java.lang.reflect.Field;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;

public final class ForgePlatformTestHelper implements IPlatformTestHelper {
    @Override
    public void bootstrap() {
        SharedConstants.tryDetectVersion();
        try {
            Field bootstrapped = Bootstrap.class.getDeclaredField("isBootstrapped");
            bootstrapped.setAccessible(true);
            bootstrapped.setBoolean(null, true);
            BuiltInRegistries.REGISTRY.keySet();
            bootstrapped.setBoolean(null, false);
            Bootstrap.bootStrap();
        } catch (Throwable ignored) {
        }
    }
}
