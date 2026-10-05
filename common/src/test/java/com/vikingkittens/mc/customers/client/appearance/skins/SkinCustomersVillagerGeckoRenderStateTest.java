package com.vikingkittens.mc.customers.client.appearance.skins;

import org.junit.jupiter.api.Test;

import com.mojang.blaze3d.vertex.PoseStack;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinCustomersVillagerGeckoRenderStateTest {
    @Test
    void restoresCallerPoseAfterGeckoRendererLeavesNestedPoses() {
        PoseStack poseStack = new PoseStack();
        PoseStack.Pose callerPose = poseStack.last();
        poseStack.pushPose();
        poseStack.translate(1.0D, 2.0D, 3.0D);
        poseStack.pushPose();

        SkinCustomersVillagerGeckoRenderState.restorePoseStack(poseStack, callerPose);

        assertSame(callerPose, poseStack.last());
        assertTrue(poseStack.clear());
    }
}
