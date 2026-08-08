package com.vikingkittens.mc.customers.client.appearance.mca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaCustomersVillagerNameTagPolicyTest {
    @Test
    void rendersConfiguredVisibleNamesWithinRange() {
        assertTrue(McaCustomersVillagerNameTagPolicy.shouldRender(
                true,
                false,
                true,
                true,
                24.0D,
                5.0F,
                false
        ));
    }

    @Test
    void hidesNamesWhenAnyMcaConditionFails() {
        assertFalse(McaCustomersVillagerNameTagPolicy.shouldRender(
                false, false, true, true, 24.0D, 5.0F, false
        ));
        assertFalse(McaCustomersVillagerNameTagPolicy.shouldRender(
                true, true, true, true, 24.0D, 5.0F, false
        ));
        assertFalse(McaCustomersVillagerNameTagPolicy.shouldRender(
                true, false, false, true, 24.0D, 5.0F, false
        ));
        assertFalse(McaCustomersVillagerNameTagPolicy.shouldRender(
                true, false, true, false, 24.0D, 5.0F, false
        ));
        assertFalse(McaCustomersVillagerNameTagPolicy.shouldRender(
                true, false, true, true, 25.0D, 5.0F, false
        ));
        assertFalse(McaCustomersVillagerNameTagPolicy.shouldRender(
                true, false, true, true, 24.0D, 5.0F, true
        ));
    }
}
