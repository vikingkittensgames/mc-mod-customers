package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceSettings;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;

public final class CustomerSpawnerLevelSettings {
    public static final float MINIMUM_REQUIRED_STARS = 0.5F;
    public static final float MAXIMUM_REQUIRED_STARS = 5.0F;
    public static final float DEFAULT_REQUIRED_STARS = 3.0F;
    public static final float DEFAULT_PET_PERCENTAGE = 0.0F;
    public static final int INVENTORY_SIZE = 54;

    private static final String TAG_APPEARANCE_SETTINGS = "appearanceSettings";
    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_MAX_CUSTOMERS = "maxCustomers";
    private static final String TAG_PET_TYPES_CUSTOMIZED = "petTypesCustomized";
    private static final String TAG_ENABLED_PET_TYPES = "enabledPetTypes";
    private static final String TAG_PET_PERCENTAGE = "petPercentage";
    private static final String TAG_REQUIRED_STARS = "requiredStars";

    private final Runnable changeListener;
    private final PersistedContainer inventory;
    private final CustomersVillagerAppearanceSettings appearanceSettings =
            new CustomersVillagerAppearanceSettings();
    private float requiredStars = DEFAULT_REQUIRED_STARS;
    private float petPercentage = DEFAULT_PET_PERCENTAGE;
    private boolean petTypesCustomized;
    private final LinkedHashSet<String> enabledPetTypes = new LinkedHashSet<>();
    private int maxCustomers;

    public CustomerSpawnerLevelSettings(
            int defaultMaxCustomers,
            Runnable changeListener,
            Predicate<Player> validity
    ) {
        this.changeListener = changeListener;
        maxCustomers = CustomerSpawnerBlockEntity.clampMaxCustomers(defaultMaxCustomers);
        inventory = new PersistedContainer(INVENTORY_SIZE, changeListener, validity);
    }

    public Container getInventory() {
        return inventory;
    }

    PersistedContainer getPersistedInventory() {
        return inventory;
    }

    public float getRequiredStars() {
        return requiredStars;
    }

    public void setRequiredStars(float requiredStars) {
        this.requiredStars = clampRequiredStars(requiredStars);
        changeListener.run();
    }

    public int getMaxCustomers() {
        return maxCustomers;
    }

    public void setMaxCustomers(int maxCustomers) {
        this.maxCustomers = CustomerSpawnerBlockEntity.clampMaxCustomers(maxCustomers);
        changeListener.run();
    }

    public float getPetPercentage() {
        return petPercentage;
    }

    public void setPetPercentage(float petPercentage) {
        this.petPercentage = clampPetPercentage(petPercentage);
        changeListener.run();
    }

    public List<String> getEnabledPetTypes(Collection<String> availablePetTypeIds) {
        if (!petTypesCustomized) {
            return List.copyOf(availablePetTypeIds);
        }
        return availablePetTypeIds.stream()
                .filter(enabledPetTypes::contains)
                .toList();
    }

    public boolean isPetTypeEnabled(String petTypeId) {
        return !petTypesCustomized || enabledPetTypes.contains(petTypeId);
    }

    public void setPetTypeEnabled(
            String petTypeId,
            boolean enabled,
            Collection<String> availablePetTypeIds
    ) {
        if (!petTypesCustomized) {
            enabledPetTypes.clear();
            enabledPetTypes.addAll(availablePetTypeIds);
            petTypesCustomized = true;
        }
        if (enabled) {
            enabledPetTypes.add(petTypeId);
        } else {
            enabledPetTypes.remove(petTypeId);
        }
        changeListener.run();
    }

    public List<ResourceLocation> getEnabledAppearanceIds() {
        return appearanceSettings.getEnabledAppearances();
    }

    public void setEnabledAppearanceIds(Collection<ResourceLocation> appearanceIds) {
        appearanceSettings.setEnabledAppearances(appearanceIds);
        changeListener.run();
    }

    CustomersVillagerAppearanceSettings getAppearanceSettings() {
        return appearanceSettings;
    }

    void read(DataReader input) {
        requiredStars = clampRequiredStars(
                input.getFloat(TAG_REQUIRED_STARS).orElse(DEFAULT_REQUIRED_STARS)
        );
        maxCustomers = CustomerSpawnerBlockEntity.clampMaxCustomers(
                input.getInt(TAG_MAX_CUSTOMERS).orElse(maxCustomers)
        );
        petPercentage = clampPetPercentage(
                input.getFloat(TAG_PET_PERCENTAGE).orElse(DEFAULT_PET_PERCENTAGE)
        );
        petTypesCustomized = input.getBoolean(TAG_PET_TYPES_CUSTOMIZED);
        enabledPetTypes.clear();
        enabledPetTypes.addAll(input.getStrings(TAG_ENABLED_PET_TYPES));
        appearanceSettings.read(input.childOrEmpty(TAG_APPEARANCE_SETTINGS));

        List<ItemStack> itemStacks = input.getItemStacks(TAG_INVENTORY);
        for (int slot = 0; slot < Math.min(inventory.getContainerSize(), itemStacks.size()); slot++) {
            inventory.setItem(slot, itemStacks.get(slot));
        }
    }

    void write(DataWriter output) {
        output.putFloat(TAG_REQUIRED_STARS, requiredStars);
        output.putInt(TAG_MAX_CUSTOMERS, maxCustomers);
        output.putFloat(TAG_PET_PERCENTAGE, petPercentage);
        output.putBoolean(TAG_PET_TYPES_CUSTOMIZED, petTypesCustomized);
        output.putStrings(TAG_ENABLED_PET_TYPES, enabledPetTypes);
        appearanceSettings.write(output.child(TAG_APPEARANCE_SETTINGS));

        List<ItemStack> itemStacks = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            itemStacks.add(inventory.getItem(slot));
        }
        output.putItemStacks(TAG_INVENTORY, itemStacks);
    }

    public static float clampRequiredStars(float value) {
        return Mth.clamp(
                Math.round(value * 2.0F) / 2.0F,
                MINIMUM_REQUIRED_STARS,
                MAXIMUM_REQUIRED_STARS
        );
    }

    public static float clampPetPercentage(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }

    public static int petPercentageToPercent(float value) {
        return Math.round(clampPetPercentage(value) * 100.0F);
    }

    public static float percentToPetPercentage(int value) {
        return Mth.clamp(value, 0, 100) / 100.0F;
    }
}
