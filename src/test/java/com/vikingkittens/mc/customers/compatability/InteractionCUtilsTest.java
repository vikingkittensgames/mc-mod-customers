package com.vikingkittens.mc.customers.compatability;

import net.minecraft.world.InteractionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
class InteractionCUtilsTest {
    @Test
    void returnsClientSuccess() {
        assertSame(
                InteractionResult.SUCCESS,
                InteractionCUtils.sidedSuccess(true)
        );
    }
    @Test
    void returnsServerSuccess() {
        assertSame(
                InteractionResult.CONSUME,
                InteractionCUtils.sidedSuccess(false)
        );
    }
}
