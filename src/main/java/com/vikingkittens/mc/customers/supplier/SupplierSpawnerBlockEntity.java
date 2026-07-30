package com.vikingkittens.mc.customers.supplier;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
            setChanged();
        }
    };

    private boolean daytimeStateInitialized = false;
    private boolean lastTickWasDaytime = false;

    public SupplierSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(SupplierSpawner.SUPPLIER_SPAWNER_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        try {
            inventory.serialize(output.child("inventory"));
        } catch (Throwable t) {
            LOGGER.error("Failed to save inventory", t);
        }

        output.putBoolean("daytimeStateInitialized", daytimeStateInitialized);
        output.putBoolean("lastTickWasDaytime", lastTickWasDaytime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        try {
            inventory.deserialize(input.childOrEmpty("inventory"));
        } catch (Throwable t) {
            LOGGER.error("Failed to load inventory because of error", t);
        }

        daytimeStateInitialized =
                input.getBooleanOr("daytimeStateInitialized", false);
        lastTickWasDaytime = input.getBooleanOr("lastTickWasDaytime", false);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("block.customers.supplier_spawner_block");
    }

    /**
     * Drops inventory before this block entity is removed.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        // Drop all items when block is broken
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, container);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
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

            boolean isDaytime = level.isBrightOutside();
            if (!entity.daytimeStateInitialized) {
                entity.daytimeStateInitialized = true;
                entity.lastTickWasDaytime = isDaytime;
                entity.setChanged();
                return;
            }

            if (
                    shouldSpawnForDaytimeTransition(
                            entity.daytimeStateInitialized,
                            entity.lastTickWasDaytime,
                            isDaytime
                    ) &&
                    !state.getValue(SupplierSpawnerBlock.STATE_DISABLED)
            ) {
                entity.spawnSupplier();
            }

            if (entity.lastTickWasDaytime != isDaytime) {
                entity.lastTickWasDaytime = isDaytime;
                entity.setChanged();
            }
        }
    }

    static boolean shouldSpawnForDaytimeTransition(
            boolean initialized,
            boolean previousDaytime,
            boolean currentDaytime
    ) {
        return initialized && !previousDaytime && currentDaytime;
    }
}
