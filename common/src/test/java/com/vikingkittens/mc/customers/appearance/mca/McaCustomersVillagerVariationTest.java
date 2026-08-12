package com.vikingkittens.mc.customers.appearance.mca;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaCustomersVillagerVariationTest {
    @Test
    void derivesTheSameMcaVariationFromTheSameSeed() {
        assertEquals(
                McaCustomersVillagerVariation.fromSeed(0.25F),
                McaCustomersVillagerVariation.fromSeed(0.25F)
        );
    }

    @Test
    void derivesDifferentMcaVariationsFromDifferentSeeds() {
        assertNotEquals(
                McaCustomersVillagerVariation.fromSeed(0.25F),
                McaCustomersVillagerVariation.fromSeed(0.75F)
        );
    }

    @Test
    void dividesGenderSelectionAcrossTheVariationRange() {
        assertFalse(
                McaCustomersVillagerVariation.fromSeed(0.499F).feminine()
        );
        assertTrue(
                McaCustomersVillagerVariation.fromSeed(0.5F).feminine()
        );
    }

    @Test
    void keepsAllMcaChoicesInTheUnitRange() {
        McaCustomersVillagerVariation variation =
                McaCustomersVillagerVariation.fromSeed(0.75F);
        List<Float> choices = List.of(
                variation.size(),
                variation.width(),
                variation.breast(),
                variation.melanin(),
                variation.hemoglobin(),
                variation.eumelanin(),
                variation.pheomelanin(),
                variation.skin(),
                variation.face(),
                variation.voice(),
                variation.voiceTone(),
                variation.clothingChoice(),
                variation.hairChoice(),
                variation.hairDyeChoice()
        );

        assertTrue(choices.stream().allMatch(
                choice -> choice >= 0.0F && choice < 1.0F
        ));
    }
}
