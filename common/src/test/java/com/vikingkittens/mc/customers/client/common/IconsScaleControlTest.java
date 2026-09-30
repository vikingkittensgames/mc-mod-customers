package com.vikingkittens.mc.customers.client.common;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IconsScaleControlTest {
    private static final Identifier ICON =
            Identifier.fromNamespaceAndPath("customers", "test");

    @Test
    void selectsHalfStarValuesFromTheClickedIconHalf() {
        AtomicReference<Float> selectedValue = new AtomicReference<>();
        IconsScaleControl control = new IconsScaleControl(
                10,
                20,
                ICON,
                ICON,
                ICON,
                16,
                16,
                5,
                0.5F,
                5.0F,
                0.5F,
                3.0F,
                selectedValue::set
        );

        control.onClick(new MouseButtonEvent(10, 20, new MouseButtonInfo(0, 0)), false);
        assertEquals(0.5F, control.getValue());
        assertEquals(0.5F, selectedValue.get());

        control.onClick(new MouseButtonEvent(10 + 16, 20, new MouseButtonInfo(0, 0)), false);
        assertEquals(1.5F, control.getValue());

        control.onClick(new MouseButtonEvent(10 + 32 + 8, 20, new MouseButtonInfo(0, 0)), false);
        assertEquals(3.0F, control.getValue());
    }
}
