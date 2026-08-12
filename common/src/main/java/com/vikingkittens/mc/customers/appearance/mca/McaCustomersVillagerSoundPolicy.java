package com.vikingkittens.mc.customers.appearance.mca;

final class McaCustomersVillagerSoundPolicy {
    private McaCustomersVillagerSoundPolicy() {}

    static VoiceMode getVoiceMode(
            boolean useMcaVoices,
            boolean useVanillaVoices
    ) {
        if (useMcaVoices) {
            return VoiceMode.MCA;
        }
        return useVanillaVoices
                ? VoiceMode.VANILLA
                : VoiceMode.SILENT;
    }

    enum VoiceMode {
        MCA,
        VANILLA,
        SILENT
    }
}
