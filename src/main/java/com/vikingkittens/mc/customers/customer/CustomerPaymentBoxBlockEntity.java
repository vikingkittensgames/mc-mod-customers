package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class CustomerPaymentBoxBlockEntity
        extends BaseContainerBlockEntity {
    public static final String NAME =
            "customer_payment_box_block_entity";
    public static final int INVENTORY_SIZE = 27;

    private NonNullList<ItemStack> items =
            NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final IItemHandler itemHandler = new InvWrapper(this);

    public CustomerPaymentBoxBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.customers.customer_payment_box");
    }

    @Override
    protected AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory
    ) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    AbstractContainerMenu createContainerMenu(
            int containerId,
            Inventory inventory
    ) {
        return createMenu(containerId, inventory);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public boolean tryInsertPayment(ItemStack payment) {
        if (payment.isEmpty()) {
            return true;
        }
        ItemStack simulatedRemainder =
                ItemHandlerHelper.insertItemStacked(
                        itemHandler,
                        payment.copy(),
                        true
                );
        if (!simulatedRemainder.isEmpty()) {
            return false;
        }
        ItemStack remainder =
                ItemHandlerHelper.insertItemStacked(
                        itemHandler,
                        payment.copy(),
                        false
                );
        if (!remainder.isEmpty()) {
            throw new IllegalStateException(
                    "Simulated payment-box capacity was unavailable"
            );
        }
        return true;
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(
                getContainerSize(),
                ItemStack.EMPTY
        );
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }
}
