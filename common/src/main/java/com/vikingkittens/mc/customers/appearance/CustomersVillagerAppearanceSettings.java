package com.vikingkittens.mc.customers.appearance;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

public final class CustomersVillagerAppearanceSettings {
    static final String TAG_ENABLED_APPEARANCES =
            "CustomersEnabledAppearances";

    private final LinkedHashSet<Identifier> enabledAppearances =
            new LinkedHashSet<>(
                    CustomersVillagerAppearances.INITIAL_ENABLED
            );

    public List<Identifier> getEnabledAppearances() {
        return List.copyOf(enabledAppearances);
    }

    public void setEnabledAppearances(
            Collection<Identifier> appearanceIds
    ) {
        enabledAppearances.clear();
        enabledAppearances.addAll(appearanceIds);
        if (enabledAppearances.isEmpty()) {
            enabledAppearances.add(
                    CustomersVillagerAppearances.DEFAULT
            );
        }
    }

    public void read(DataReader input) {
        List<Identifier> loadedAppearanceIds =
                input.getStrings(TAG_ENABLED_APPEARANCES).stream()
                        .map(Identifier::tryParse)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList();
        if (!loadedAppearanceIds.isEmpty()) {
            setEnabledAppearances(loadedAppearanceIds);
        }
    }

    public void write(DataWriter output) {
        output.putStrings(
                TAG_ENABLED_APPEARANCES,
                enabledAppearances.stream()
                        .map(Identifier::toString)
                        .toList()
        );
    }
}
