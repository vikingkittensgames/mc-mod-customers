package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerModel;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkinCustomersVillagerClientAppearanceTest {
    @Test
    void preservesClientRendererSettings() {
        SkinCustomersVillagerDefinition skin =
                new SkinCustomersVillagerDefinition(
                        ResourceLocation.parse("example:alex"),
                        SkinCustomersVillagerModel.SLIM,
                        false,
                        1.1F,
                        0.4F,
                        0.2F,
                        Map.of()
                );

        assertEquals(SkinCustomersVillagerModel.SLIM, skin.model());
        assertEquals(1.1F, skin.scale());
        assertEquals(0.4F, skin.shadowRadius());
        assertEquals(0.2F, skin.nameTagOffset());
    }
}
