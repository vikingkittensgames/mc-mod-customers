package com.vikingkittens.mc.customers.client.appearance.skins;

import com.geckolib.animation.state.BoneSnapshot;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkinCustomersVillagerGeckoHeadRotationTest {
    @Test
    void appliesTrackingToTheAnimatedHeadRotation() {
        BoneSnapshot head = mock(BoneSnapshot.class);
        when(head.getRotX()).thenReturn(0.25F);
        when(head.getRotY()).thenReturn(-0.5F);

        SkinCustomersVillagerGeckoHeadRotation.apply(head, 0.5F, 1.0F);

        inOrder(head).verify(head).setRotX(0.75F);
        inOrder(head).verify(head).setRotY(0.5F);
    }
}
