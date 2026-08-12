package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkinPackCustomersVillagerAppearanceTest {
    private static final ResourceLocation STEVE = ResourceLocation.parse("example:steve");
    private static final ResourceLocation ALEX = ResourceLocation.parse("example:alex");

    @Test
    void selectsPackSkinDeterministicallyFromVariationSeed() {
        List<ResourceLocation> skins = List.of(STEVE, ALEX);

        assertEquals(Optional.of(STEVE), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 0.0F));
        assertEquals(Optional.of(STEVE), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 0.49F));
        assertEquals(Optional.of(ALEX), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 0.5F));
        assertEquals(Optional.of(ALEX), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 1.0F));
        assertEquals(Optional.empty(), SkinPackCustomersVillagerAppearance.selectSkinId(List.of(), 0.5F));
    }

    @Test
    void createsSoundEventsUsingReferencedSoundIds() {
        ResourceLocation ambient = ResourceLocation.parse("example:steve_ambient");
        SkinCustomersVillagerDefinition definition = new SkinCustomersVillagerDefinition(
                STEVE,
                SkinCustomersVillagerModel.WIDE,
                false,
                SkinCustomersVillagerDefinition.DEFAULT_SCALE,
                SkinCustomersVillagerDefinition.DEFAULT_SHADOW_RADIUS,
                0.0F,
                Map.of("ambient", ambient)
        );

        assertEquals(ambient, definition.getSound(SkinCustomersVillagerSound.AMBIENT).orElseThrow());
        assertNull(definition.getSound(SkinCustomersVillagerSound.DEATH).orElse(null));
        assertEquals(ambient, SoundEvent.createVariableRangeEvent(ambient).getLocation());
    }
}
