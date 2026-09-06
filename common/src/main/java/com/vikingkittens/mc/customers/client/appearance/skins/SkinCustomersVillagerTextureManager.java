package com.vikingkittens.mc.customers.client.appearance.skins;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerTextureManager {
    private static final Set<Identifier> REGISTERED = new HashSet<>();

    private SkinCustomersVillagerTextureManager() {}

    static Identifier getTexture(SkinCustomersVillagerDefinition skin) {
        Identifier source = skin.getTextureLocation();
        if (!skin.legacy()) return source;

        Identifier generated = Identifier.fromNamespaceAndPath(
                Customers.MODID,
                "generated/legacy_skins/" + source.getNamespace() + "/" + source.getPath()
        );
        if (!REGISTERED.contains(generated)) {
            try (InputStream input = Minecraft.getInstance().getResourceManager().getResourceOrThrow(source).open()) {
                NativeImage sourceImage = NativeImage.read(input);
                Minecraft.getInstance().getTextureManager().register(
                        generated,
                        new DynamicTexture(generated::toString, convert(sourceImage))
                );
                REGISTERED.add(generated);
            } catch (IOException exception) {
                return source;
            }
        }
        return generated;
    }

    private static NativeImage convert(NativeImage image) throws IOException {
            if (image.getWidth() != 64 || image.getHeight() != 32) {
                image.close();
                throw new IOException("Legacy skin must be 64x32");
            }

            NativeImage converted = new NativeImage(64, 64, true);
            converted.copyFrom(image);
            image.close();
            converted.fillRect(0, 32, 64, 32, 0);
            converted.copyRect(4, 16, 16, 32, 4, 4, true, false);
            converted.copyRect(8, 16, 16, 32, 4, 4, true, false);
            converted.copyRect(0, 20, 24, 32, 4, 12, true, false);
            converted.copyRect(4, 20, 16, 32, 4, 12, true, false);
            converted.copyRect(8, 20, 8, 32, 4, 12, true, false);
            converted.copyRect(12, 20, 16, 32, 4, 12, true, false);
            converted.copyRect(44, 16, -8, 32, 4, 4, true, false);
            converted.copyRect(48, 16, -8, 32, 4, 4, true, false);
            converted.copyRect(40, 20, 0, 32, 4, 12, true, false);
            converted.copyRect(44, 20, -8, 32, 4, 12, true, false);
            converted.copyRect(48, 20, -16, 32, 4, 12, true, false);
            converted.copyRect(52, 20, -8, 32, 4, 12, true, false);
        return converted;
    }
}
