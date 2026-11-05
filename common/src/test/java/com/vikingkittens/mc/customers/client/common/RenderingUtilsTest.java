package com.vikingkittens.mc.customers.client.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderingUtilsTest {
    @Test
    void rendersAdditionalModelsAsTheStackFills() {
        assertEquals(1, RenderingUtils.getModelCount(1, 64));
        assertEquals(2, RenderingUtils.getModelCount(2, 64));
        assertEquals(3, RenderingUtils.getModelCount(32, 64));
        assertEquals(5, RenderingUtils.getModelCount(64, 64));
    }
}
