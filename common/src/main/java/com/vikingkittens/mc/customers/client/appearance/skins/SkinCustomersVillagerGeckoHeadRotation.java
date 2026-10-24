package com.vikingkittens.mc.customers.client.appearance.skins;

import software.bernie.geckolib.cache.object.GeoBone;

final class SkinCustomersVillagerGeckoHeadRotation {
    private SkinCustomersVillagerGeckoHeadRotation() {}

    static void apply(GeoBone head, float trackingPitch, float trackingYaw) {
        head.setRotX(head.getRotX() + trackingPitch);
        head.setRotY(head.getRotY() + trackingYaw);
        head.resetStateChanges();
    }
}
