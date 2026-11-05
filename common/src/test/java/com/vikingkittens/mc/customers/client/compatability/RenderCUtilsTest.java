package com.vikingkittens.mc.customers.client.compatability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderCUtilsTest {
    @Test
    void translucentNoDepthTestsDepthWithoutWritingIt() {
        RenderCUtils.RenderState state = RenderCUtils
                .getTranslucentNoDepthRenderState();

        assertTrue(state.depthTest());
        assertFalse(state.depthWrite());
        assertFalse(state.cull());
    }
}
