package com.vikingkittens.mc.customers.client.compatability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class TextureCTest {
    @Test
    void preservesNamespaceAndPath() {
        TextureC texture = new TextureC(
                "customers",
                "textures/gui/receipt.png"
        );

        assertEquals("customers", texture.namespace());
        assertEquals(
                "textures/gui/receipt.png",
                texture.path()
        );
    }
    @Test
    void rejectsNullComponents() {
        assertThrows(
                NullPointerException.class,
                () -> new TextureC(null, "path")
        );
        assertThrows(
                NullPointerException.class,
                () -> new TextureC("customers", null)
        );
    }
}
