package com.vikingkittens.mc.customers.client.appearance.skins;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerDefinition;
import com.vikingkittens.mc.customers.appearance.skins.SkinCustomersVillagerModel;
import com.vikingkittens.mc.customers.appearance.skins.SkinPackCustomersVillagerAppearance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SkinCustomersVillagerClientAppearanceTest {
    @Test
    void loadsTheStandardSkinClientClassWithoutGeckoLib() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("software.bernie.geckolib.animatable.GeoEntity"));
        assertDoesNotThrow(() -> Class.forName(
                "com.vikingkittens.mc.customers.client.appearance.skins.SkinCustomersVillagerClientAppearance"
        ));
    }

    @Test
    void clientProviderRecognizesOnlySkinPackAppearances() {
        assertTrue(
                SkinCustomersVillagerClientAppearanceProvider.INSTANCE
                        .supports(
                                mock(
                                        SkinPackCustomersVillagerAppearance.class
                                )
                        )
        );
        assertFalse(
                SkinCustomersVillagerClientAppearanceProvider.INSTANCE
                        .supports(
                                mock(CustomersVillagerAppearance.class)
                        )
        );
    }

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
                        List.of(),
                        Map.of(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Map.of(),
                        List.of()
                );

        assertEquals(SkinCustomersVillagerModel.SLIM, skin.model());
        assertEquals(1.1F, skin.scale());
        assertEquals(0.4F, skin.shadowRadius());
        assertEquals(0.2F, skin.nameTagOffset());
    }
}
