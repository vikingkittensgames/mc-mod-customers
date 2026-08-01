package com.vikingkittens.mc.customers.client.compatability;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.AABB;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
class DebugBoxCTest {
    @Test
    void preservesBoundsAndColor() {
        AABB bounds = new AABB(1, 2, 3, 4, 5, 6);
        DebugBoxC box = new DebugBoxC(bounds, 0x80402010);

        assertSame(bounds, box.bounds());
        assertEquals(0x80402010, box.color());
    }
    @Test
    void rejectsNullBounds() {
        assertThrows(
                NullPointerException.class,
                () -> new DebugBoxC(null, 0xFFFFFFFF)
        );
    }
}
