package com.vikingkittens.mc.customers.client.customer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerVillagerEntityRendererTest {
    @Test
    void appliesHumanoidSittingLegPose() {
        ModelPart rightLeg = new ModelPart(List.of(), Map.of());
        ModelPart leftLeg = new ModelPart(List.of(), Map.of());

        CustomerVillagerEntityRenderer.Model.applySittingLegPose(rightLeg, leftLeg);

        assertEquals(-1.4137167F, rightLeg.xRot);
        assertEquals((float) (Math.PI / 10.0D), rightLeg.yRot);
        assertEquals(0.07853982F, rightLeg.zRot);
        assertEquals(-1.4137167F, leftLeg.xRot);
        assertEquals((float) (-Math.PI / 10.0D), leftLeg.yRot);
        assertEquals(-0.07853982F, leftLeg.zRot);
    }

    @Test
    void showsSplitJacketWithLowerHalfFollowingLegsWhileSitting() {
        ModelPart jacket = modelPart();
        ModelPart upperJacket = modelPart();
        ModelPart lowerJacket = modelPart();

        CustomerVillagerEntityRenderer.Model.applyJacketPose(
                true,
                jacket,
                upperJacket,
                lowerJacket,
                -1.4137167F
        );

        assertFalse(jacket.visible);
        assertTrue(upperJacket.visible);
        assertTrue(lowerJacket.visible);
        assertEquals(-1.4137167F, lowerJacket.xRot);
    }

    @Test
    void restoresOriginalJacketAfterStanding() {
        ModelPart jacket = modelPart();
        ModelPart upperJacket = modelPart();
        ModelPart lowerJacket = modelPart();
        lowerJacket.xRot = -1.4137167F;

        CustomerVillagerEntityRenderer.Model.applyJacketPose(
                false,
                jacket,
                upperJacket,
                lowerJacket,
                0.0F
        );

        assertTrue(jacket.visible);
        assertFalse(upperJacket.visible);
        assertFalse(lowerJacket.visible);
        assertEquals(0.0F, lowerJacket.xRot);
    }

    @Test
    void customLayerContainsSplitJacketParts() {
        ModelPart root =
                CustomerVillagerEntityRenderer.Model.createBodyLayer().bakeRoot();
        ModelPart body = root.getChild("body");

        body.getChild("upper_jacket");
        body.getChild("lower_jacket");
    }

    @Test
    void lowerJacketOverlapsUpperJacketAndPivotsTwoPixelsFromItsTop() {
        ModelPart root =
                CustomerVillagerEntityRenderer.Model.createBodyLayer().bakeRoot();
        ModelPart lowerJacket = root.getChild("body").getChild("lower_jacket");
        ModelPart.Cube[] cubes = new ModelPart.Cube[1];

        lowerJacket.visit(
                new PoseStack(),
                (pose, path, index, cube) -> cubes[index] = cube
        );

        assertEquals(12.0F, lowerJacket.y);
        assertEquals(-2.0F, cubes[0].minY);
        assertEquals(7.0F, cubes[0].maxY);
        assertEquals(9.0F, cubes[0].maxY - cubes[0].minY);
        assertEquals(10.0F, lowerJacket.y + cubes[0].minY);
        assertEquals(19.0F, lowerJacket.y + cubes[0].maxY);
    }
    private static ModelPart modelPart() {
        return new ModelPart(List.of(), Map.of());
    }
}
