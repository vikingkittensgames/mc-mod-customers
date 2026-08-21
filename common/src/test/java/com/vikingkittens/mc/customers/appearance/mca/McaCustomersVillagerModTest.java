package com.vikingkittens.mc.customers.appearance.mca;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaCustomersVillagerModTest {
    @Test
    void mcaAppearanceOwnsItsId() {
        assertEquals(
                ResourceLocation.parse("customers:mca"),
                McaCustomersVillagerAppearance.ID
        );
    }

    @Test
    void detectsMcaByItsModId() {
        assertTrue(McaCustomersVillagerMod.isLoaded("mca"::equals));
    }

    @Test
    void supportsMcaWhenItIsLoadedAndHasTheRequiredApi() {
        assertTrue(McaCustomersVillagerMod.isSupported("mca"::equals, () -> true));
    }

    @Test
    void doesNotSupportMcaWhenItsRequiredApiIsMissing() {
        assertFalse(McaCustomersVillagerMod.isSupported("mca"::equals, () -> false));
    }

    @Test
    void reportsMcaMissingWhenItsModIdIsAbsent() {
        assertFalse(McaCustomersVillagerMod.isLoaded(ignored -> false));
    }
}
