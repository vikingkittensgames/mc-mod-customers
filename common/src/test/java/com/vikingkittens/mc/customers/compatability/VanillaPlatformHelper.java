package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.entity.player.Player;

public final class VanillaPlatformHelper implements IPlatformHelper {
    @Override
    public String platformName() {
        return "Vanilla Test";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return false;
    }

    @Override
    public void closeContainer(Player player) {}
}
