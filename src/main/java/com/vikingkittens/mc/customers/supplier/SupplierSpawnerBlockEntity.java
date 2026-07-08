package com.vikingkittens.mc.customers.supplier;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class SupplierSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int INVENTORY_ROW_SIZE = 9;

    private static MerchantOffers getOffersFromInventory(RandomSource random, ItemStackHandler inventory) {
        MerchantOffers offers = new MerchantOffers();
        ItemStack lastItemStack = null;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                LOGGER.debug("Slot[{}]: item={}, count={}, lastItemStack={}", slot, stack.getItem(), stack.getCount(), lastItemStack);
                if (stack.is(Items.EMERALD)) {
                    if (lastItemStack != null) {
                        offers.removeLast();
                        offers.add(new MerchantOffer(
                                new ItemCost(Items.EMERALD, stack.getCount()),
                                Optional.empty(),
                                new ItemStack(lastItemStack.getItem(), lastItemStack.getCount()),
                                10,
                                0,
                                0
                        ));
                        LOGGER.debug("Updated cost of {} to {}/{}", offers.getLast().getCostA().getItem(), offers.getLast().getCostA().getCount(), stack.getCount());
                    }
                    lastItemStack = null;
                } else {
                    lastItemStack = stack;
                    offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 1),
                            Optional.empty(),
                            new ItemStack(stack.getItem(), stack.getCount()),
                            10,
                            0,
                            0
                    ));
                }
            } else {
                lastItemStack = null;
            }
        }

        return offers;
    }

    public static final String NAME = "supplier_spawner_block_entity";

    private boolean ticksDisabled = false;
    private long ticksSinceTicksDisabledCheck = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_ROW_SIZE * 6) {
        @Override
        protected void onContentsChanged(int slot) {
            LOGGER.debug("Inventory changed");
            setChanged();
        }
    };

    private long lastTimeOfDay = 0;

    public SupplierSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(SupplierSpawner.SUPPLIER_SPAWNER_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        LOGGER.debug("Saving");
        super.saveAdditional(tag, registries);

        try {
            tag.put("inventory", this.inventory.serializeNBT(registries));
        } catch (Throwable t) {
            LOGGER.error("Failed to save inventory", t);
        }

        tag.putLong("lastTimeOfDay", lastTimeOfDay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        LOGGER.debug("Loading");
        super.loadAdditional(tag, registries);

        if (tag.contains("inventory")) {
            try {
                inventory.deserializeNBT(registries, tag.getCompound("inventory"));
            } catch (Throwable t) {
                LOGGER.error("Failed to load inventory because of error", t);
            }
        }

        if (tag.contains("lastTimeOfDay")) {
            lastTimeOfDay = tag.getLong("lastTimeOfDay");
        }
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("block.customers.supplier_spawner_block");
    }

    public void beforeRemove() {
        // Drop all items when block is broken
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, container);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        // Direct anonymous bridge converting NeoForge's Handler to a Vanilla Container
        Container containerBridge = new Container() {
            @Override
            public int getContainerSize() {
                return inventory.getSlots();
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    if (!inventory.getStackInSlot(i).isEmpty()) return false;
                }
                return true;
            }

            @Override
            public ItemStack getItem(int slot) {
                return inventory.getStackInSlot(slot);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack stack = inventory.extractItem(slot, amount, false);
                setChanged();
                return stack;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack stack = inventory.getStackInSlot(slot).copy();
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                setChanged();
                return stack;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                inventory.setStackInSlot(slot, stack);
                setChanged();
            }

            @Override
            public void setChanged() {
                SupplierSpawnerBlockEntity.this.setChanged();
            }

            @Override
            public boolean stillValid(Player player) {
                return Container.stillValidBlockEntity(SupplierSpawnerBlockEntity.this, player);
            }

            @Override
            public void clearContent() {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                }
                setChanged();
            }
        };

        // NeoForge / Modern Mojang uses sixRows for the 54-slot UI (9x6)
        return ChestMenu.sixRows(containerId, playerInventory, containerBridge);
    }

    /* package private */ static BlockState updateState(Level level, BlockPos pos, BlockState currentState) {
        boolean disabled = level.hasNeighborSignal(pos);

        BlockState newState = currentState
                .setValue(SupplierSpawnerBlock.STATE_DISABLED, disabled);
        return newState;
    }

    /* package private */ void updateState() {
        BlockState newState = updateState(getLevel(), getBlockPos(), getBlockState());
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public void spawnSupplier() {
        MerchantOffers offers = getOffersFromInventory(level.getRandom(), inventory);
        if (!offers.isEmpty()) {
            SupplierVillagerEntity supplier = SupplierVillagerEntity.spawn(
                    level,
                    getBlockPos(),
                    offers
            );
            if (supplier != null) {
                LOGGER.debug("Supplier UUID={}, pos={}", supplier.getUUID(), supplier.blockPosition());
                setChanged();
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SupplierSpawnerBlockEntity entity) {
        if (!level.isClientSide()) {
            if (entity.ticksSinceTicksDisabledCheck == 0 || entity.ticksSinceTicksDisabledCheck > 20) {
                try {
                    entity.ticksDisabled = SearchUtils.findEntitiesInSphere(level, Player.class, pos, 64, (p, e) -> true).isEmpty();
                } catch (Throwable t) {
                    entity.ticksDisabled = false;
                }
                entity.ticksSinceTicksDisabledCheck = 0;
            }
            entity.ticksSinceTicksDisabledCheck++;
            if (entity.ticksDisabled) {
                return;
            }

            long timeOfDay = (level.getDayTime() + 6000L) % 24000L;
            if (timeOfDay < entity.lastTimeOfDay && !state.getValue(SupplierSpawnerBlock.STATE_DISABLED)) {
                entity.spawnSupplier();
            }
            entity.lastTimeOfDay = timeOfDay;
        }
    }
}
