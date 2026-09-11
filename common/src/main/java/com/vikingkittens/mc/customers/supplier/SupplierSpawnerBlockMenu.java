package com.vikingkittens.mc.customers.supplier;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
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

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.economy.Economy;

public class SupplierSpawnerBlockMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 54;
    private static final int VISIBLE_CONTAINER_SIZE = 48;
    private static final int AUTO_COST_BUTTON_ID = 2000;
    private final Container container;
    private final ContainerData data;
    private final SupplierSpawnerBlockEntity blockEntity;
    private final List<ResourceLocation> appearanceIds;
    private final RegistryAccess registryAccess;

    public SupplierSpawnerBlockMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(CONTAINER_SIZE), null);
    }

    SupplierSpawnerBlockMenu(
            int id,
            Inventory playerInventory,
            Container container,
            SupplierSpawnerBlockEntity blockEntity
    ) {
        super(SupplierSpawner.SUPPLIER_SPAWNER_MENU.get(), id);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        this.blockEntity = blockEntity;
        registryAccess = playerInventory.player.registryAccess();
        appearanceIds = CustomersVillagerAppearances
                .getAvailableAppearanceIds(registryAccess);
        data = blockEntity == null
                ? new SimpleContainerData(getDataSlotCount())
                : createData(blockEntity);
        addDataSlots(data);
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                int slotColumn = column;
                addSlot(new Slot(
                        container,
                        getContainerSlotIndex(row, column),
                        getContainerSlotX(column),
                        18 + row * 18
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return slotColumn % 2 == 0 || !usesAutomaticCost();
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return slotColumn % 2 == 0 || !usesAutomaticCost();
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        140 + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    198
            ));
        }
    }

    public static int getContainerSlotX(int column) {
        return 8 + column * 18 + column / 2 * 4;
    }

    static int getContainerSlotIndex(int row, int column) {
        return row * 9 + column;
    }

    public List<ResourceLocation> getAppearanceIds() {
        return appearanceIds;
    }

    public Component getAppearanceName(int index) {
        return CustomersVillagerAppearances.getName(
                appearanceIds.get(index),
                registryAccess
        );
    }

    public boolean isAppearanceEnabled(int index) {
        return data.get(index) != 0;
    }

    public int appearanceButtonId(int index) {
        return 1000 + index;
    }

    public int autoCostButtonId() {
        return AUTO_COST_BUTTON_ID;
    }

    public boolean isEconomyEnabled() {
        return data.get(getEconomyEnabledDataIndex()) != 0;
    }

    public boolean isForceAutoCost() {
        return data.get(getForceAutoCostDataIndex()) != 0;
    }

    public boolean isAutoCost() {
        return data.get(getAutoCostDataIndex()) != 0;
    }

    public boolean usesAutomaticCost() {
        return isEconomyEnabled() && (isForceAutoCost() || isAutoCost());
    }

    public ItemStack getAutomaticCost(int row, int pair) {
        int start = getGeneratedCostDataStart() + (row * 4 + pair) * 2;
        ItemStack cost = BuiltInRegistries.ITEM.byId(data.get(start)).getDefaultInstance();
        cost.setCount(data.get(start + 1));
        return cost;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity == null) {
            return false;
        }
        if (id == AUTO_COST_BUTTON_ID && Economy.isEnabled() && !Economy.forceAutoCost()) {
            blockEntity.setAutoCost(!blockEntity.isAutoCost());
            broadcastChanges();
            return true;
        }
        int index = id - 1000;
        if (index < 0 || index >= appearanceIds.size()) {
            return false;
        }

        List<ResourceLocation> enabled =
                new ArrayList<>(blockEntity.getEnabledAppearanceIds());
        ResourceLocation appearance = appearanceIds.get(index);
        if (!enabled.remove(appearance)) {
            enabled.add(appearance);
        }
        blockEntity.setEnabledAppearanceIds(enabled);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved = index < VISIBLE_CONTAINER_SIZE
                ? moveItemStackTo(
                        source,
                        VISIBLE_CONTAINER_SIZE,
                        slots.size(),
                        true
                )
                : moveItemStackTo(
                        source,
                        0,
                        VISIBLE_CONTAINER_SIZE,
                        false
                );
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private ContainerData createData(
            SupplierSpawnerBlockEntity entity
    ) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == getEconomyEnabledDataIndex()) return Economy.isEnabled() ? 1 : 0;
                if (index == getForceAutoCostDataIndex()) return Economy.forceAutoCost() ? 1 : 0;
                if (index == getAutoCostDataIndex()) return entity.isAutoCost() ? 1 : 0;
                if (index >= getGeneratedCostDataStart()) {
                    int pairIndex = (index - getGeneratedCostDataStart()) / 2;
                    ItemStack result = container.getItem(pairIndex / 4 * 9 + pairIndex % 4 * 2);
                    ItemStack cost = entity.usesAutomaticCost()
                            ? Economy.calculateItemStackCost(result)
                            : ItemStack.EMPTY;
                    if (cost == null) cost = ItemStack.EMPTY;
                    return (index - getGeneratedCostDataStart()) % 2 == 0
                            ? BuiltInRegistries.ITEM.getId(cost.getItem())
                            : cost.getCount();
                }
                return entity.getEnabledAppearanceIds().contains(
                                appearanceIds.get(index)
                        )
                        ? 1
                        : 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return getDataSlotCount();
            }
        };
    }

    private int getEconomyEnabledDataIndex() {
        return appearanceIds.size();
    }

    private int getForceAutoCostDataIndex() {
        return getEconomyEnabledDataIndex() + 1;
    }

    private int getAutoCostDataIndex() {
        return getForceAutoCostDataIndex() + 1;
    }

    private int getGeneratedCostDataStart() {
        return getAutoCostDataIndex() + 1;
    }

    private int getDataSlotCount() {
        return getGeneratedCostDataStart() + 48;
    }
}
