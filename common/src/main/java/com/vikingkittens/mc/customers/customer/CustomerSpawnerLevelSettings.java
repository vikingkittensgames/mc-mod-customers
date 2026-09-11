package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceSettings;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;
import com.vikingkittens.mc.customers.customer.pets.CustomerPet;

public final class CustomerSpawnerLevelSettings {
    public static final float MINIMUM_REQUIRED_STARS = 0.5F;
    public static final float MAXIMUM_REQUIRED_STARS = 5.0F;
    public static final float DEFAULT_REQUIRED_STARS = 3.0F;
    public static final float DEFAULT_PET_PERCENTAGE = 0.0F;
    public static final int OFFER_INVENTORY_SIZE = 54;
    public static final int PET_FOOD_COST_SLOT = OFFER_INVENTORY_SIZE;
    public static final int INVENTORY_SIZE = OFFER_INVENTORY_SIZE + 1;

    private static final String TAG_APPEARANCE_SETTINGS = "appearanceSettings";
    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_MAX_CUSTOMERS = "maxCustomers";
    private static final String TAG_PET_TYPES_CUSTOMIZED = "petTypesCustomized";
    private static final String TAG_ENABLED_PET_TYPES = "enabledPetTypes";
    private static final String TAG_PET_FOODS = "petFoods";
    private static final String TAG_PET_PERCENTAGE = "petPercentage";
    private static final String TAG_REQUIRED_STARS = "requiredStars";
    private static final String TAG_AUTO_COST = "autoCost";

    private final Runnable changeListener;
    private final PersistedContainer inventory;
    private final CustomersVillagerAppearanceSettings appearanceSettings =
            new CustomersVillagerAppearanceSettings();
    private float requiredStars = DEFAULT_REQUIRED_STARS;
    private float petPercentage = DEFAULT_PET_PERCENTAGE;
    private boolean petTypesCustomized;
    private final LinkedHashSet<String> enabledPetTypes = new LinkedHashSet<>();
    private final LinkedHashMap<String, ItemStack> petFoods = new LinkedHashMap<>();
    private int maxCustomers;
    private boolean autoCost;

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

    public ItemStack getPetFoodCost() {
        return inventory.getItem(PET_FOOD_COST_SLOT);
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

    public boolean isAutoCost() {
        return autoCost;
    }

    public void setAutoCost(boolean autoCost) {
        this.autoCost = autoCost;
        changeListener.run();
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

    public void setPetTypesEnabled(Collection<String> availablePetTypeIds, boolean enabled) {
        petTypesCustomized = true;
        enabledPetTypes.clear();
        if (enabled) {
            enabledPetTypes.addAll(availablePetTypeIds);
        }
        changeListener.run();
    }

    public void initializePetFoods(Collection<CustomerPet.PetType> availablePetTypes) {
        boolean changed = false;
        for (CustomerPet.PetType petType : availablePetTypes) {
            ItemStack existingFood = petFoods.get(petType.entityId());
            ItemStack selectedFood = petType.getFood(existingFood);
            if (existingFood == null || !ItemStackCUtils.isSameItemAndTags(selectedFood, existingFood)) {
                petFoods.put(petType.entityId(), selectedFood);
                changed = true;
            }
        }
        if (changed) {
            changeListener.run();
        }
    }

    public ItemStack getPetFood(CustomerPet.PetType petType) {
        return petType.getFood(petFoods.get(petType.entityId()));
    }

    public void cyclePetFood(CustomerPet.PetType petType) {
        List<ItemStack> foods = petType.foods();
        ItemStack selectedFood = getPetFood(petType);
        int index = 0;
        for (int foodIndex = 0; foodIndex < foods.size(); foodIndex++) {
            if (ItemStackCUtils.isSameItemAndTags(foods.get(foodIndex), selectedFood)) {
                index = foodIndex;
                break;
            }
        }
        petFoods.put(petType.entityId(), foods.get((index + 1) % foods.size()));
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
        petFoods.clear();
        for (DataReader petFoodInput : input.getChildren(TAG_PET_FOODS)) {
            petFoodInput.getString("petTypeId").ifPresent(petTypeId -> {
                List<ItemStack> food = petFoodInput.getItemStacks("food");
                if (!food.isEmpty()) {
                    petFoods.put(petTypeId, food.getFirst());
                }
            });
        }
        appearanceSettings.read(input.childOrEmpty(TAG_APPEARANCE_SETTINGS));
        autoCost = input.getBoolean(TAG_AUTO_COST);

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
        output.putBoolean(TAG_AUTO_COST, autoCost);
        output.putStrings(TAG_ENABLED_PET_TYPES, enabledPetTypes);
        petFoods.forEach((petTypeId, food) -> {
            DataWriter petFoodOutput = output.addChild(TAG_PET_FOODS);
            petFoodOutput.putString("petTypeId", petTypeId);
            petFoodOutput.putItemStacks("food", List.of(food));
        });
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
