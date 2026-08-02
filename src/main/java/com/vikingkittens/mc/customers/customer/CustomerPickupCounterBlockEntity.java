package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public class CustomerPickupCounterBlockEntity extends BlockEntity {
    public static final String NAME = "customer_pickup_counter";
    public static final int INVENTORY_SIZE = 9;
    private static final Direction[] CONNECTED_DIRECTIONS = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST
    };

    private final ItemStackHandler inventory =
            new ItemStackHandler(INVENTORY_SIZE) {
                @Override
                protected void onContentsChanged(int slot) {
                    inventoryChanged();
                }
            };

    public CustomerPickupCounterBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    private void inventoryChanged() {
        setChanged();
        if (level != null && !LevelCUtils.isClientSide(level)) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(
                    registries,
                    tag.getCompound("inventory")
            );
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack insertStack(ItemStack stack) {
        return insertStack(inventory, stack);
    }

    public ItemStack insertStackConnected(ItemStack stack) {
        return level == null
                ? insertStack(stack)
                : insertStackConnected(level, worldPosition, stack);
    }

    public ItemStack removeOldest() {
        return removeOldest(inventory);
    }

    public ItemStack removeOldestConnected() {
        return level == null
                ? removeOldest()
                : removeOldestConnected(level, worldPosition);
    }

    public List<ItemStack> getDisplayItems() {
        return getDisplayItems(inventory);
    }

    static ItemStack insertStack(
            ItemStackHandler inventory,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                return inventory.insertItem(slot, stack.copy(), false);
            }
        }
        return stack.copy();
    }

    static ItemStack removeOldest(ItemStackHandler inventory) {
        if (inventory.getStackInSlot(0).isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = inventory.getStackInSlot(0).copy();
        for (int slot = 1; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(
                    slot - 1,
                    inventory.getStackInSlot(slot).copy()
            );
        }
        inventory.setStackInSlot(
                inventory.getSlots() - 1,
                ItemStack.EMPTY
        );
        return removed;
    }

    static List<ItemStack> getDisplayItems(ItemStackHandler inventory) {
        List<ItemStack> items = new ArrayList<>(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }
        return List.copyOf(items);
    }

    static ItemStack insertStackConnected(
            Level level,
            BlockPos pos,
            ItemStack stack
    ) {
        return insertStackConnected(
                level,
                pos,
                stack,
                new HashSet<>()
        );
    }

    private static ItemStack insertStackConnected(
            Level level,
            BlockPos pos,
            ItemStack stack,
            Set<BlockPos> visited
    ) {
        if (!visited.add(pos)
                || !(level.getBlockEntity(pos)
                        instanceof CustomerPickupCounterBlockEntity counter)) {
            return stack.copy();
        }

        ItemStack remainder = counter.insertStack(stack);
        if (remainder.getCount() < stack.getCount()) {
            return remainder;
        }

        for (Direction direction : CONNECTED_DIRECTIONS) {
            remainder = insertStackConnected(
                    level,
                    pos.relative(direction),
                    stack,
                    visited
            );
            if (remainder.getCount() < stack.getCount()) {
                return remainder;
            }
        }
        return stack.copy();
    }

    static ItemStack removeOldestConnected(
            Level level,
            BlockPos pos
    ) {
        return removeOldestConnected(
                level,
                pos,
                new HashSet<>()
        );
    }

    private static ItemStack removeOldestConnected(
            Level level,
            BlockPos pos,
            Set<BlockPos> visited
    ) {
        if (!visited.add(pos)
                || !(level.getBlockEntity(pos)
                        instanceof CustomerPickupCounterBlockEntity counter)) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = counter.removeOldest();
        if (!removed.isEmpty()) {
            return removed;
        }

        for (Direction direction : CONNECTED_DIRECTIONS) {
            removed = removeOldestConnected(
                    level,
                    pos.relative(direction),
                    visited
            );
            if (!removed.isEmpty()) {
                return removed;
            }
        }
        return ItemStack.EMPTY;
    }
}
