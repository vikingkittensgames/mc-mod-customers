package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;

public class CustomerSpawnerBlockMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 54;
    private static final int APPEARANCE_DATA_START = 2;
    private final Container container;
    private final ContainerData data;
    private final CustomerSpawnerBlockEntity blockEntity;
    private final List<ResourceLocation> appearanceIds;

    public CustomerSpawnerBlockMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(CONTAINER_SIZE), null);
    }

    CustomerSpawnerBlockMenu(int id, Inventory playerInventory, Container container, CustomerSpawnerBlockEntity blockEntity) {
        super(CustomerSpawner.CUSTOMER_SPAWNER_MENU.get(), id);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        this.blockEntity = blockEntity;
        appearanceIds = CustomersVillagerAppearance.APPEARANCE_REGISTRY.keySet().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString)).toList();
        data = blockEntity == null ? new SimpleContainerData(2 + appearanceIds.size()) : createData(blockEntity);
        addDataSlots(data);
        container.startOpen(playerInventory.player);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(container, column + row * 9, getContainerSlotX(column), 18 + row * 18));
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
        return CustomerSpawnerMode.values()[Math.clamp(data.get(0), 0, CustomerSpawnerMode.values().length - 1)];
    }

    public int getMaxCustomers() { return data.get(1); }
    public List<ResourceLocation> getAppearanceIds() { return appearanceIds; }
    public boolean isAppearanceEnabled(int index) { return data.get(APPEARANCE_DATA_START + index) != 0; }
    public int modeButtonId(CustomerSpawnerMode mode) { return mode.ordinal(); }
    public int maxCustomersButtonId(int value) { return 100 + value; }
    public int appearanceButtonId(int index) { return 1000 + index; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null) return false;
        if (id >= 0 && id < CustomerSpawnerMode.values().length) {
            blockEntity.setSpawnerMode(CustomerSpawnerMode.values()[id]);
            return true;
        }
        if (id > 100 && id <= 199) {
            blockEntity.setMaxCustomers(id - 100);
            return true;
        }
        int index = id - 1000;
        if (index >= 0 && index < appearanceIds.size()) {
            List<ResourceLocation> enabled = new ArrayList<>(blockEntity.getEnabledAppearanceIds());
            ResourceLocation appearance = appearanceIds.get(index);
            if (!enabled.remove(appearance)) enabled.add(appearance);
            blockEntity.setEnabledAppearanceIds(enabled);
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
                if (index == 1) return entity.getMaxCustomers();
                return entity.getEnabledAppearanceIds().contains(appearanceIds.get(index - 2)) ? 1 : 0;
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 2 + appearanceIds.size(); }
        };
    }
}
