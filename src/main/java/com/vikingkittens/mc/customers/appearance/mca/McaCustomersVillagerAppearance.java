package com.vikingkittens.mc.customers.appearance.mca;

import net.conczin.mca.Config;
import net.conczin.mca.registry.SoundsMCA;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;

final class McaCustomersVillagerAppearance
        implements CustomersVillagerAppearance {
    @Override
    public Component getName() {
        return Component.translatable("appearance.customers.mca");
    }

    @Override
    public SoundEvent getAmbientSound(
            CustomersVillager villager
    ) {
        return voiceMode()
                        == McaCustomersVillagerSoundPolicy.VoiceMode.VANILLA
                ? null
                : SoundsMCA.SILENT;
    }

    @Override
    public SoundEvent getHurtSound(
            CustomersVillager villager
    ) {
        return switch (voiceMode()) {
            case MCA -> feminine(villager)
                    ? SoundsMCA.VILLAGER_FEMALE_HURT
                    : SoundsMCA.VILLAGER_MALE_HURT;
            case VANILLA -> null;
            case SILENT -> SoundsMCA.SILENT;
        };
    }

    @Override
    public SoundEvent getDeathSound(
            CustomersVillager villager
    ) {
        return switch (voiceMode()) {
            case MCA -> feminine(villager)
                    ? SoundsMCA.VILLAGER_FEMALE_SCREAM
                    : SoundsMCA.VILLAGER_MALE_SCREAM;
            case VANILLA -> null;
            case SILENT -> SoundsMCA.SILENT;
        };
    }

    private static boolean feminine(
            CustomersVillager villager
    ) {
        return McaCustomersVillagerVariation.fromSeed(
                villager.getVariationSeed()
        ).feminine();
    }

    private static McaCustomersVillagerSoundPolicy.VoiceMode
            voiceMode() {
        Config config = Config.getInstance();
        return McaCustomersVillagerSoundPolicy.getVoiceMode(
                config.useMCAVoices,
                config.useVanillaVoices
        );
    }
}
