package com.vikingkittens.mc.customers.client.appearance.skins;

import com.geckolib.animation.state.BoneSnapshot;

final class SkinCustomersVillagerGeckoHeadRotation {
    private SkinCustomersVillagerGeckoHeadRotation() {}

    static void apply(BoneSnapshot head, float trackingPitch, float trackingYaw) {
        head.setRotX(head.getRotX() + trackingPitch);
        head.setRotY(head.getRotY() + trackingYaw);
    }
}
