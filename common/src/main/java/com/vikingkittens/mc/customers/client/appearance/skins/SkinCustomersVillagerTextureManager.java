package com.vikingkittens.mc.customers.client.appearance.skins;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;

final class SkinCustomersVillagerTextureManager {
    private static final Set<ResourceLocation> REGISTERED = new HashSet<>();

    private SkinCustomersVillagerTextureManager() {}

    static ResourceLocation getTexture(SkinCustomersVillagerDefinition skin) {
        ResourceLocation source = skin.getTextureLocation();
        if (!skin.legacy()) return source;

        ResourceLocation generated = ResourceLocation.fromNamespaceAndPath(
                Customers.MODID,
                "generated/legacy_skins/" + source.getNamespace() + "/" + source.getPath()
        );
        if (REGISTERED.add(generated)) {
            Minecraft.getInstance().getTextureManager().register(generated, new LegacySkinTexture(source));
        }
        return generated;
    }

    private static final class LegacySkinTexture extends AbstractTexture {
        private final ResourceLocation source;

        private LegacySkinTexture(ResourceLocation source) {
            this.source = source;
        }

        @Override
        public void load(ResourceManager resourceManager) throws IOException {
            NativeImage sourceImage;
            try (InputStream input = resourceManager.getResourceOrThrow(source).open()) {
                sourceImage = NativeImage.read(input);
            }
            NativeImage converted = convert(sourceImage);
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> upload(converted));
            } else {
                upload(converted);
            }
        }

        private void upload(NativeImage image) {
            TextureUtil.prepareImage(getId(), image.getWidth(), image.getHeight());
            image.upload(0, 0, 0, true);
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
}
