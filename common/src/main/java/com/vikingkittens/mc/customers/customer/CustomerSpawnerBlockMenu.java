package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.customer.pets.CustomerPet;
import com.vikingkittens.mc.customers.economy.Economy;

public class CustomerSpawnerBlockMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = CustomerSpawnerLevelSettings.INVENTORY_SIZE;
    private static final int SELECTED_LEVEL_DATA_INDEX = 1;
    private static final int MAX_CUSTOMERS_DATA_INDEX = 2;
    private static final int REQUIRED_STARS_DATA_INDEX = 3;
    private static final int PET_PERCENTAGE_DATA_INDEX = 4;
    private static final int AVOID_BLOCK_DATA_INDEX = 5;
    private static final int ECONOMY_ENABLED_DATA_INDEX = 6;
    private static final int FORCE_AUTO_COST_DATA_INDEX = 7;
    private static final int AUTO_COST_DATA_INDEX = 8;
    private static final int APPEARANCE_DATA_START = 9;
    private static final int PET_TYPES_ALL_BUTTON_ID = 1999;
    private static final int PET_TYPE_BUTTON_ID_START = 2000;
    private static final int PET_TYPE_FOOD_BUTTON_ID_START = 3000;
    private static final int DECREMENT_LEVEL_BUTTON_ID = 100;
    private static final int INCREMENT_LEVEL_BUTTON_ID = 101;
    private static final int AUTO_COST_BUTTON_ID = 102;
    private static final int MAX_CUSTOMERS_BUTTON_ID_START = 200;
    private static final int PET_PERCENTAGE_BUTTON_ID_START = 300;
    private static final int REQUIRED_STARS_BUTTON_ID_START = 400;
    private final Container container;
    private final ContainerData data;
    private final CustomerSpawnerBlockEntity blockEntity;
    private final List<ResourceLocation> appearanceIds;
    private final List<CustomerPet.PetType> petTypes;
    private final RegistryAccess registryAccess;
    private final Slot petFoodCostSlot;
    private int selectedLevel;
    private boolean petFoodCostSlotVisible;

    public CustomerSpawnerBlockMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(CONTAINER_SIZE), null);
    }

    CustomerSpawnerBlockMenu(int id, Inventory playerInventory, Container container, CustomerSpawnerBlockEntity blockEntity) {
        super(CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.container = blockEntity == null ? container : new LevelContainer(blockEntity);
        checkContainerSize(this.container, CONTAINER_SIZE);
        registryAccess = playerInventory.player.registryAccess();
        appearanceIds = CustomersVillagerAppearances
                .getAvailableAppearanceIds(registryAccess);
        petTypes = CustomerPet.getAvailablePetTypes(playerInventory.player.level());
        if (blockEntity != null) {
            blockEntity.getLevelSettings(selectedLevel).initializePetFoods(petTypes);
        }
        data = blockEntity == null
                ? new SimpleContainerData(getDataSlotCount())
                : createData(blockEntity);
        addDataSlots(data);
        this.container.startOpen(playerInventory.player);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slotColumn = column;
                addSlot(new Slot(this.container, column + row * 9, getContainerSlotX(column), 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return slotColumn != 8 || !usesAutomaticCost();
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return slotColumn != 8 || !usesAutomaticCost();
                    }
                });
            }
        }
        petFoodCostSlot = addSlot(new Slot(
                this.container,
                CustomerSpawnerLevelSettings.PET_FOOD_COST_SLOT,
                408,
                4
        ) {
            @Override
            public boolean isActive() {
                return blockEntity != null || petFoodCostSlotVisible;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column, 8 + column * 18, 198));
    }

    public static int getContainerSlotX(int column) {
        return 8 + column * 18 + (column == 8 ? 4 : 0);
    }

    public void setPetFoodCostSlotVisible(boolean visible) {
        petFoodCostSlotVisible = visible;
    }

    public static boolean isValidMaxCustomersText(String value) {
        if (value.isEmpty()) return true;
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 1 && parsed <= 99;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isValidPetPercentageText(String value) {
        if (value.isEmpty()) return true;
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 && parsed <= 100;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public CustomerSpawnerMode getSpawnerMode() {
        return CustomerSpawnerMode.values()[Mth.clamp(data.get(0), 0, CustomerSpawnerMode.values().length - 1)];
    }

    public int getSelectedLevel() {
        return Mth.clamp(data.get(SELECTED_LEVEL_DATA_INDEX), 0, CustomerSpawnerBlockEntity.MAX_LEVELS - 1);
    }

    public boolean isEconomyEnabled() {
        return data.get(ECONOMY_ENABLED_DATA_INDEX) != 0;
    }

    public boolean isForceAutoCost() {
        return data.get(FORCE_AUTO_COST_DATA_INDEX) != 0;
    }

    public boolean isAutoCost() {
        return data.get(AUTO_COST_DATA_INDEX) != 0;
    }

    public boolean usesAutomaticCost() {
        return isEconomyEnabled() && (isForceAutoCost() || isAutoCost());
    }

    public ItemStack getAutomaticCost(int row) {
        if (!usesAutomaticCost() || row < 0 || row >= 6) {
            return ItemStack.EMPTY;
        }
        int dataStart = getGeneratedCostDataStart() + row * 2;
        ItemStack cost = BuiltInRegistries.ITEM.byId(data.get(dataStart)).getDefaultInstance();
        cost.setCount(data.get(dataStart + 1));
        return cost;
    }

    public int getMaxCustomers() { return data.get(MAX_CUSTOMERS_DATA_INDEX); }

    public float getRequiredStars() { return data.get(REQUIRED_STARS_DATA_INDEX) / 2.0F; }

    public int getPetPercentagePercent() { return data.get(PET_PERCENTAGE_DATA_INDEX); }

    public boolean hasAvoidBlock() {
        return CustomerCounter.canUseAsAvoidBlock(getAvoidBlock().defaultBlockState());
    }

    public ItemStack getAvoidBlockItem() {
        return hasAvoidBlock() ? new ItemStack(getAvoidBlock()) : ItemStack.EMPTY;
    }

    private Block getAvoidBlock() {
        return BuiltInRegistries.BLOCK.byId(data.get(AVOID_BLOCK_DATA_INDEX));
    }

    public List<ResourceLocation> getAppearanceIds() { return appearanceIds; }
    public Component getAppearanceName(int index) {
        return CustomersVillagerAppearances.getName(
                appearanceIds.get(index),
                registryAccess
        );
    }
    public boolean isAppearanceEnabled(int index) { return data.get(APPEARANCE_DATA_START + index) != 0; }
    public List<CustomerPet.PetType> getPetTypes() { return petTypes; }
    public Component getPetTypeName(int index) { return petTypes.get(index).name(); }
    public ItemStack getPetTypeFood(int index) {
        ItemStack food = BuiltInRegistries.ITEM.byId(data.get(getPetTypeFoodDataStart() + index)).getDefaultInstance();
        return food.isEmpty() ? petTypes.get(index).getFood(null) : food;
    }
    public boolean isPetTypeEnabled(int index) { return data.get(getPetTypeDataStart() + index) != 0; }
    public boolean areAllPetTypesEnabled() {
        if (petTypes.isEmpty()) {
            return false;
        }
        for (int index = 0; index < petTypes.size(); index++) {
            if (!isPetTypeEnabled(index)) {
                return false;
            }
        }
        return true;
    }
    public int modeButtonId(CustomerSpawnerMode mode) { return mode.ordinal(); }
    public int decrementLevelButtonId() { return DECREMENT_LEVEL_BUTTON_ID; }
    public int incrementLevelButtonId() { return INCREMENT_LEVEL_BUTTON_ID; }
    public int autoCostButtonId() { return AUTO_COST_BUTTON_ID; }
    public int maxCustomersButtonId(int value) { return MAX_CUSTOMERS_BUTTON_ID_START + value; }
    public int petPercentageButtonId(int value) {
        return PET_PERCENTAGE_BUTTON_ID_START + Mth.clamp(value, 0, 100);
    }
    public int requiredStarsButtonId(float value) { return REQUIRED_STARS_BUTTON_ID_START + Math.round(value * 2.0F); }
    public int appearanceButtonId(int index) { return 1000 + index; }
    public int petTypesAllButtonId() { return PET_TYPES_ALL_BUTTON_ID; }
    public int petTypeButtonId(int index) { return PET_TYPE_BUTTON_ID_START + index; }
    public int petTypeFoodButtonId(int index) { return PET_TYPE_FOOD_BUTTON_ID_START + index; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null) return false;
        if (id == DECREMENT_LEVEL_BUTTON_ID && selectedLevel > 0) {
            selectedLevel--;
            blockEntity.enforceAutomaticCostInventory(selectedLevel);
            broadcastChanges();
            return true;
        }
        if (id == INCREMENT_LEVEL_BUTTON_ID && selectedLevel < CustomerSpawnerBlockEntity.MAX_LEVELS - 1) {
            selectedLevel++;
            blockEntity.getLevelSettings(selectedLevel).initializePetFoods(petTypes);
            blockEntity.enforceAutomaticCostInventory(selectedLevel);
            broadcastChanges();
            return true;
        }
        if (id == AUTO_COST_BUTTON_ID && Economy.isEnabled() && !Economy.forceAutoCost()) {
            CustomerSpawnerLevelSettings settings = blockEntity.getLevelSettings(selectedLevel);
            blockEntity.setAutoCost(selectedLevel, !settings.isAutoCost());
            broadcastChanges();
            return true;
        }
        if (id >= 0 && id < CustomerSpawnerMode.values().length) {
            blockEntity.setSpawnerMode(CustomerSpawnerMode.values()[id]);
            return true;
        }
        if (id > MAX_CUSTOMERS_BUTTON_ID_START && id <= MAX_CUSTOMERS_BUTTON_ID_START + 99) {
            blockEntity.getLevelSettings(selectedLevel).setMaxCustomers(id - MAX_CUSTOMERS_BUTTON_ID_START);
            return true;
        }
        if (id >= PET_PERCENTAGE_BUTTON_ID_START && id <= PET_PERCENTAGE_BUTTON_ID_START + 100) {
            blockEntity.getLevelSettings(selectedLevel).setPetPercentage(
                    CustomerSpawnerLevelSettings.percentToPetPercentage(id - PET_PERCENTAGE_BUTTON_ID_START)
            );
            return true;
        }
        if (id > REQUIRED_STARS_BUTTON_ID_START && id <= REQUIRED_STARS_BUTTON_ID_START + 10) {
            blockEntity.getLevelSettings(selectedLevel).setRequiredStars(
                    (id - REQUIRED_STARS_BUTTON_ID_START) / 2.0F
            );
            return true;
        }
        int index = id - 1000;
        if (index >= 0 && index < appearanceIds.size()) {
            List<ResourceLocation> enabled = new ArrayList<>(
                    blockEntity.getLevelSettings(selectedLevel).getEnabledAppearanceIds()
            );
            ResourceLocation appearance = appearanceIds.get(index);
            if (!enabled.remove(appearance)) enabled.add(appearance);
            blockEntity.getLevelSettings(selectedLevel).setEnabledAppearanceIds(enabled);
            return true;
        }
        if (id == PET_TYPES_ALL_BUTTON_ID) {
            CustomerSpawnerLevelSettings settings = blockEntity.getLevelSettings(selectedLevel);
            settings.setPetTypesEnabled(
                    petTypes.stream().map(CustomerPet.PetType::entityId).toList(),
                    !areAllPetTypesEnabled()
            );
            return true;
        }
        index = id - PET_TYPE_FOOD_BUTTON_ID_START;
        if (index >= 0 && index < petTypes.size()) {
            blockEntity.getLevelSettings(selectedLevel).cyclePetFood(petTypes.get(index));
            return true;
        }
        index = id - PET_TYPE_BUTTON_ID_START;
        if (index >= 0 && index < petTypes.size()) {
            CustomerSpawnerLevelSettings settings = blockEntity.getLevelSettings(selectedLevel);
            String petTypeId = petTypes.get(index).entityId();
            settings.setPetTypeEnabled(
                    petTypeId,
                    !settings.isPetTypeEnabled(petTypeId),
                    petTypes.stream().map(CustomerPet.PetType::entityId).toList()
            );
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < CONTAINER_SIZE ? !moveItemStackTo(source, CONTAINER_SIZE, slots.size(), true) : !moveItemStackTo(source, 0, CONTAINER_SIZE, false)) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }

    private ContainerData createData(CustomerSpawnerBlockEntity entity) {
        return new ContainerData() {
            @Override public int get(int index) {
                if (index == 0) return entity.getSpawnerMode().ordinal();
                if (index == SELECTED_LEVEL_DATA_INDEX) return selectedLevel;
                CustomerSpawnerLevelSettings settings = entity.getLevelSettings(selectedLevel);
                if (index == MAX_CUSTOMERS_DATA_INDEX) return settings.getMaxCustomers();
                if (index == REQUIRED_STARS_DATA_INDEX) return Math.round(settings.getRequiredStars() * 2.0F);
                if (index == PET_PERCENTAGE_DATA_INDEX) {
                    return CustomerSpawnerLevelSettings.petPercentageToPercent(settings.getPetPercentage());
                }
                if (index == AVOID_BLOCK_DATA_INDEX) {
                    return BuiltInRegistries.BLOCK.getId(
                            entity.getLevel().getBlockState(entity.getBlockPos().below()).getBlock()
                    );
                }
                if (index == ECONOMY_ENABLED_DATA_INDEX) return Economy.isEnabled() ? 1 : 0;
                if (index == FORCE_AUTO_COST_DATA_INDEX) return Economy.forceAutoCost() ? 1 : 0;
                if (index == AUTO_COST_DATA_INDEX) return settings.isAutoCost() ? 1 : 0;
                if (index >= getGeneratedCostDataStart()) {
                    ItemStack cost = getGeneratedCost(settings, (index - getGeneratedCostDataStart()) / 2);
                    return (index - getGeneratedCostDataStart()) % 2 == 0
                            ? BuiltInRegistries.ITEM.getId(cost.getItem())
                            : cost.getCount();
                }
                if (index >= getPetTypeFoodDataStart() && index < getDataSlotCount()) {
                    return BuiltInRegistries.ITEM.getId(
                            settings.getPetFood(petTypes.get(index - getPetTypeFoodDataStart())).getItem()
                    );
                }
                if (index >= getPetTypeDataStart() && index < getPetTypeFoodDataStart()) {
                    return settings.isPetTypeEnabled(petTypes.get(index - getPetTypeDataStart()).entityId()) ? 1 : 0;
                }
                return settings.getEnabledAppearanceIds().contains(appearanceIds.get(index - APPEARANCE_DATA_START)) ? 1 : 0;
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return getDataSlotCount(); }
        };
    }

    private int getPetTypeDataStart() {
        return APPEARANCE_DATA_START + appearanceIds.size();
    }

    private int getPetTypeFoodDataStart() {
        return getPetTypeDataStart() + petTypes.size();
    }

    private int getDataSlotCount() {
        return getGeneratedCostDataStart() + 12;
    }

    private int getGeneratedCostDataStart() {
        return getPetTypeFoodDataStart() + petTypes.size();
    }

    private ItemStack getGeneratedCost(CustomerSpawnerLevelSettings settings, int row) {
        if (!blockEntity.usesAutomaticCost(settings)) {
            return ItemStack.EMPTY;
        }
        Container inventory = settings.getInventory();
        int rowStart = row * 9;
        for (int column = 0; column < 8; column++) {
            ItemStack item = inventory.getItem(rowStart + column);
            if (!item.isEmpty()) {
                ItemStack cost = Economy.calculateItemStackCost(item);
                return cost == null ? ItemStack.EMPTY : cost;
            }
        }
        return ItemStack.EMPTY;
    }

    private class LevelContainer implements Container {
        private final CustomerSpawnerBlockEntity entity;

        private LevelContainer(CustomerSpawnerBlockEntity entity) {
            this.entity = entity;
        }

        private Container getCurrentContainer() {
            return entity.getLevelSettings(selectedLevel).getInventory();
        }

        @Override public int getContainerSize() { return getCurrentContainer().getContainerSize(); }
        @Override public boolean isEmpty() { return getCurrentContainer().isEmpty(); }
        @Override public ItemStack getItem(int slot) { return getCurrentContainer().getItem(slot); }
        @Override public ItemStack removeItem(int slot, int amount) { return getCurrentContainer().removeItem(slot, amount); }
        @Override public ItemStack removeItemNoUpdate(int slot) { return getCurrentContainer().removeItemNoUpdate(slot); }
        @Override public void setItem(int slot, ItemStack stack) { getCurrentContainer().setItem(slot, stack); }
        @Override public void setChanged() { getCurrentContainer().setChanged(); }
        @Override public boolean stillValid(Player player) { return getCurrentContainer().stillValid(player); }
        @Override public void clearContent() { getCurrentContainer().clearContent(); }
        @Override public void startOpen(Player player) { getCurrentContainer().startOpen(player); }
        @Override public void stopOpen(Player player) { getCurrentContainer().stopOpen(player); }
    }
}
