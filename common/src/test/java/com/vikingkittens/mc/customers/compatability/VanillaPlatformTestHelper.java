package com.vikingkittens.mc.customers.compatability;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class VanillaPlatformTestHelper implements IPlatformTestHelper {
    @Override
    public void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
