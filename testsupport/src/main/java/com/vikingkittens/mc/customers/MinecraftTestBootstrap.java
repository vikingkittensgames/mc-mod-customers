package com.vikingkittens.mc.customers;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.vikingkittens.mc.customers.compatability.IPlatformTestHelper;

public final class MinecraftTestBootstrap {
    private static boolean initialized;

    private MinecraftTestBootstrap() {}

    public static synchronized void bootstrap() {
        if (initialized) {
            return;
        }
        Iterator<IPlatformTestHelper> helpers =
                ServiceLoader.load(IPlatformTestHelper.class).iterator();
        if (!helpers.hasNext()) {
            throw new IllegalStateException("No platform test helper was found");
        }
        IPlatformTestHelper helper = helpers.next();
        if (helpers.hasNext()) {
            throw new IllegalStateException("Multiple platform test helpers were found");
        }
        helper.bootstrap();
        initialized = true;
    }
}
