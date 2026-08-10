package com.vikingkittens.mc.customers.appearance.mca;

import net.conczin.mca.registry.SoundsMCA;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class McaCustomersVillagerAppearanceTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void selectsGenderedMcaYesAndNoSounds() {
        assertEquals(
                SoundsMCA.VILLAGER_MALE_YES,
                McaCustomersVillagerAppearance.yesSound(
                        McaCustomersVillagerSoundPolicy.VoiceMode.MCA,
                        false
                )
        );
        assertEquals(
                SoundsMCA.VILLAGER_FEMALE_YES,
                McaCustomersVillagerAppearance.yesSound(
                        McaCustomersVillagerSoundPolicy.VoiceMode.MCA,
                        true
                )
        );
        assertEquals(
                SoundsMCA.VILLAGER_MALE_NO,
                McaCustomersVillagerAppearance.noSound(
                        McaCustomersVillagerSoundPolicy.VoiceMode.MCA,
                        false
                )
        );
        assertEquals(
                SoundsMCA.VILLAGER_FEMALE_NO,
                McaCustomersVillagerAppearance.noSound(
                        McaCustomersVillagerSoundPolicy.VoiceMode.MCA,
                        true
                )
        );
    }

    @Test
    void followsVanillaAndSilentVoicePolicies() {
        assertNull(McaCustomersVillagerAppearance.yesSound(
                McaCustomersVillagerSoundPolicy.VoiceMode.VANILLA,
                false
        ));
        assertNull(McaCustomersVillagerAppearance.noSound(
                McaCustomersVillagerSoundPolicy.VoiceMode.VANILLA,
                true
        ));
        assertEquals(
                SoundsMCA.SILENT,
                McaCustomersVillagerAppearance.yesSound(
                        McaCustomersVillagerSoundPolicy.VoiceMode.SILENT,
                        false
                )
        );
        assertEquals(
                SoundsMCA.SILENT,
                McaCustomersVillagerAppearance.noSound(
                        McaCustomersVillagerSoundPolicy.VoiceMode.SILENT,
                        true
                )
        );
    }
}
