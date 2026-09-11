package com.vikingkittens.mc.customers.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CostToggleTextureResourceTest {
    private static final Path GUI_TEXTURES =
            Path.of("src/main/resources/assets/customers/textures/gui");

    @Test
    void costToggleTexturesAreTwelvePixelsSquare() throws IOException {
        for (String texture : List.of("cost.png", "costauto.png")) {
            BufferedImage image = ImageIO.read(GUI_TEXTURES.resolve(texture).toFile());

            assertNotNull(image, texture);
            assertEquals(12, image.getWidth(), texture);
            assertEquals(12, image.getHeight(), texture);
        }
    }
}
