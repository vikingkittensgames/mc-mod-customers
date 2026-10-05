package com.vikingkittens.mc.customers.client.appearance.skins;

import com.mojang.blaze3d.vertex.PoseStack;

final class SkinCustomersVillagerGeckoRenderState {
    private SkinCustomersVillagerGeckoRenderState() {}

    static void restorePoseStack(PoseStack poseStack, PoseStack.Pose callerPose) {
        while (poseStack.last() != callerPose) {
            poseStack.popPose();
        }
    }
}
