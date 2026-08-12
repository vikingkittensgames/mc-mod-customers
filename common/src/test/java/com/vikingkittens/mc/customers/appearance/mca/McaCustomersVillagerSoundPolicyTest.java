package com.vikingkittens.mc.customers.appearance.mca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McaCustomersVillagerSoundPolicyTest {
    @Test
    void prefersMcaVoicesWhenTheyAreEnabled() {
        assertEquals(
                McaCustomersVillagerSoundPolicy.VoiceMode.MCA,
                McaCustomersVillagerSoundPolicy.getVoiceMode(
                        true,
                        true
                )
        );
    }

    @Test
    void usesVanillaVoicesWhenOnlyTheyAreEnabled() {
        assertEquals(
                McaCustomersVillagerSoundPolicy.VoiceMode.VANILLA,
                McaCustomersVillagerSoundPolicy.getVoiceMode(
                        false,
                        true
                )
        );
    }

    @Test
    void usesSilenceWhenVoicesAreDisabled() {
        assertEquals(
                McaCustomersVillagerSoundPolicy.VoiceMode.SILENT,
                McaCustomersVillagerSoundPolicy.getVoiceMode(
                        false,
                        false
                )
        );
    }
}
