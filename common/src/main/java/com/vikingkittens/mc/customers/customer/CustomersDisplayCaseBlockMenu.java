package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CustomersDisplayCaseBlockMenu
        extends AbstractContainerMenu {
    private static final int DISPLAY_SLOT_COUNT = 12;
    private static final int DISPLAY_GROUP_SIZE = 6;
    private static final int DISPLAY_COLUMNS = 3;
    private final Container container;

    public CustomersDisplayCaseBlockMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                CustomersDisplayCase.MENU.get(),
                containerId,
                playerInventory,
                new SimpleContainer(DISPLAY_SLOT_COUNT)
        );
    }

    CustomersDisplayCaseBlockMenu(
            int containerId,
            Inventory playerInventory,
            Container container
    ) {
        this(
                CustomersDisplayCase.MENU.get(),
                containerId,
                playerInventory,
                container
        );
    }

    CustomersDisplayCaseBlockMenu(
            MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            Container container
    ) {
        super(menuType, containerId);
        checkContainerSize(container, DISPLAY_SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        for (int slot = 0; slot < DISPLAY_SLOT_COUNT; slot++) {
            addSlot(new Slot(
                    container,
                    slot,
                    getDisplaySlotX(slot),
                    getDisplaySlotY(slot)
            ));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        50 + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    108
            ));
        }
    }

    static int getDisplaySlotX(int slot) {
        int group = slot / DISPLAY_GROUP_SIZE;
        int column = slot % DISPLAY_COLUMNS;
        return 25 + group * 72 + column * 18;
    }

    static int getDisplaySlotY(int slot) {
        int slotInGroup = slot % DISPLAY_GROUP_SIZE;
        return 7 + slotInGroup / DISPLAY_COLUMNS * 18;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        boolean moved = index < DISPLAY_SLOT_COUNT
                ? moveItemStackTo(
                        source,
                        DISPLAY_SLOT_COUNT,
                        slots.size(),
                        true
                )
                : moveItemStackTo(
                        source,
                        0,
                        DISPLAY_SLOT_COUNT,
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
}
