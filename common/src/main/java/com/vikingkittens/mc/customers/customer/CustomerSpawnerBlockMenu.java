package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.RegistryAccess;
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

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;

public class CustomerSpawnerBlockMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 54;
    private static final int SELECTED_LEVEL_DATA_INDEX = 1;
    private static final int MAX_CUSTOMERS_DATA_INDEX = 2;
    private static final int REQUIRED_STARS_DATA_INDEX = 3;
    private static final int APPEARANCE_DATA_START = 4;
    private static final int DECREMENT_LEVEL_BUTTON_ID = 100;
    private static final int INCREMENT_LEVEL_BUTTON_ID = 101;
    private static final int MAX_CUSTOMERS_BUTTON_ID_START = 200;
    private static final int REQUIRED_STARS_BUTTON_ID_START = 400;
    private final Container container;
    private final ContainerData data;
    private final CustomerSpawnerBlockEntity blockEntity;
    private final List<ResourceLocation> appearanceIds;
    private final RegistryAccess registryAccess;
    private int selectedLevel;

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
        data = blockEntity == null
                ? new SimpleContainerData(APPEARANCE_DATA_START + appearanceIds.size())
                : createData(blockEntity);
        addDataSlots(data);
        this.container.startOpen(playerInventory.player);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(this.container, column + row * 9, getContainerSlotX(column), 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column, 8 + column * 18, 198));
    }

    static int getContainerSlotX(int column) {
        return 8 + column * 18 + (column == 8 ? 4 : 0);
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

    public CustomerSpawnerMode getSpawnerMode() {
        return CustomerSpawnerMode.values()[Mth.clamp(data.get(0), 0, CustomerSpawnerMode.values().length - 1)];
    }

    public int getSelectedLevel() {
        return Mth.clamp(data.get(SELECTED_LEVEL_DATA_INDEX), 0, CustomerSpawnerBlockEntity.MAX_LEVELS - 1);
    }

    public int getMaxCustomers() { return data.get(MAX_CUSTOMERS_DATA_INDEX); }

    public float getRequiredStars() { return data.get(REQUIRED_STARS_DATA_INDEX) / 2.0F; }

    public List<ResourceLocation> getAppearanceIds() { return appearanceIds; }
    public Component getAppearanceName(int index) {
        return CustomersVillagerAppearances.getName(
                appearanceIds.get(index),
                registryAccess
        );
    }
    public boolean isAppearanceEnabled(int index) { return data.get(APPEARANCE_DATA_START + index) != 0; }
    public int modeButtonId(CustomerSpawnerMode mode) { return mode.ordinal(); }
    public int decrementLevelButtonId() { return DECREMENT_LEVEL_BUTTON_ID; }
    public int incrementLevelButtonId() { return INCREMENT_LEVEL_BUTTON_ID; }
    public int maxCustomersButtonId(int value) { return MAX_CUSTOMERS_BUTTON_ID_START + value; }
    public int requiredStarsButtonId(float value) { return REQUIRED_STARS_BUTTON_ID_START + Math.round(value * 2.0F); }
    public int appearanceButtonId(int index) { return 1000 + index; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null) return false;
        if (id == DECREMENT_LEVEL_BUTTON_ID && selectedLevel > 0) {
            selectedLevel--;
            broadcastChanges();
            return true;
        }
        if (id == INCREMENT_LEVEL_BUTTON_ID && selectedLevel < CustomerSpawnerBlockEntity.MAX_LEVELS - 1) {
            selectedLevel++;
            blockEntity.getLevelSettings(selectedLevel);
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
                return settings.getEnabledAppearanceIds().contains(appearanceIds.get(index - APPEARANCE_DATA_START)) ? 1 : 0;
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return APPEARANCE_DATA_START + appearanceIds.size(); }
        };
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
