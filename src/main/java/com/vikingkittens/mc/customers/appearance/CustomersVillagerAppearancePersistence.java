package com.vikingkittens.mc.customers.appearance;

import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

public final class CustomersVillagerAppearancePersistence {
    static final String TAG_APPEARANCE = "CustomersAppearance";
    static final String TAG_VARIATION_SEED = "CustomersVariationSeed";

    private CustomersVillagerAppearancePersistence() {}

    public static void read(
            DataReader input,
            CustomersVillager villager
    ) {
        input.getString(TAG_APPEARANCE)
                .map(ResourceLocation::tryParse)
                .ifPresent(villager::setAppearanceId);
        input.getFloat(TAG_VARIATION_SEED)
                .ifPresent(villager::setVariationSeed);
    }

    public static void write(
            DataWriter output,
            CustomersVillager villager
    ) {
        output.putString(
                TAG_APPEARANCE,
                villager.getAppearanceId().toString()
        );
        output.putFloat(
                TAG_VARIATION_SEED,
                villager.getVariationSeed()
        );
    }
}
