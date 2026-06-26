package com.vikingkittens.mc.customers.spawner;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CustomerSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int INVENTORY_ROW_SIZE = 9;

    private static MerchantOffers getOffersFromInventory(RandomSource random, ItemStackHandler inventory) {
        MerchantOffers offers = new MerchantOffers();
        int numRows = inventory.getSlots() / INVENTORY_ROW_SIZE;
        List<Integer> rowCost = new ArrayList<>(Collections.nCopies(numRows, 0));
        List<List<ItemStack>> rowItems = new ArrayList<>(Collections.nCopies(numRows, new ArrayList<>()));
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            int row = slot / INVENTORY_ROW_SIZE;
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                if (stack.is(Items.EMERALD)) {
                    if (rowCost.get(row) < stack.getCount()) {
                        rowCost.set(row, stack.getCount());
                    }
                } else {
                    rowItems.get(row).add(stack);
                }
            }
        }
        List<Integer> rowsWithItems = new ArrayList<>();
        for (int row = 0; row < numRows; row++) {
            if (!rowItems.get(row).isEmpty()) {
                rowsWithItems.add(row);
            }
        }
        int numItemsToBuy = random.nextInt(1, rowsWithItems.size());
        while (numItemsToBuy > 0) {
            int rowNum = random.nextInt(rowsWithItems.size());
            int row = rowsWithItems.get(rowNum);

            int itemNum = random.nextInt(rowItems.get(row).size());
            ItemStack itemStack = rowItems.get(row).get(itemNum);
            int count = random.nextInt(1, itemStack.getCount());
            offers.add(new MerchantOffer(
                    new ItemCost(itemStack.getItem(), count),
                    Optional.empty(),
                    new ItemStack(Items.EMERALD, rowCost.get(row)),
                    1,
                    rowCost.get(row) * count,
                    0
            ));

            rowsWithItems.remove(rowNum);
            numItemsToBuy--;
        }

        return offers;
    }

    public static final String NAME = "customer_spawner_block_entity";

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_ROW_SIZE * 6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide) {
                LOGGER.info("Customer Spawner Inventory Changed");
            }
        }
    };

    public CustomerSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CustomerSpawner.CUSTOMER_SPAWNER_ENTITY.get(), pos, blockState);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("block.customers.customer_spawner_block");
    }

    public void beforeRemove() {
        // Drop all items when block is broken
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, container);

        // TODO: Despawn all of the customers
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
                CustomerSpawnerBlockEntity.this.setChanged();
            }

            @Override
            public boolean stillValid(Player player) {
                return Container.stillValidBlockEntity(CustomerSpawnerBlockEntity.this, player);
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", this.inventory.serializeNBT(registries));
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            this.inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
    }

    public void cycleSpawnMode() {
        CustomerSpawnerMode spawnerMode = getBlockState().getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
        CustomerSpawnerMode nextSpawnerMode = switch (spawnerMode) {
            case CONTINUOUS -> CustomerSpawnerMode.HOURLY;
            case HOURLY -> CustomerSpawnerMode.DAY;
            case DAY -> CustomerSpawnerMode.NIGHT;
            case NIGHT -> CustomerSpawnerMode.BREAKFAST;
            case BREAKFAST -> CustomerSpawnerMode.LUNCH;
            case LUNCH -> CustomerSpawnerMode.DINNER;
            case DINNER -> CustomerSpawnerMode.MANUAL;
            case MANUAL -> CustomerSpawnerMode.CONTINUOUS;
        };
        BlockState newState = getBlockState().setValue(CustomerSpawnerBlock.STATE_SPAWN_MODE, nextSpawnerMode);
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public void spawnCustomer() {
        CustomerVillagerEntity.spawn(
                getLevel(),
                getBlockPos(),
                getOffersFromInventory(level.getRandom(), inventory)
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomerSpawnerBlockEntity entity) {
        if (!level.isClientSide()) {
            CustomerSpawnerMode spawnerMode = state.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
            long timeOfDay = level.getDayTime() % 24000;
        }
    }
}
