package com.vikingkittens.mc.customers.spawner;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class CustomerSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int INVENTORY_ROW_SIZE = 9;
    private static final int MAX_CUSTOMERS = 4;

    private static MerchantOffers getOffersFromInventory(RandomSource random, ItemStackHandler inventory) {
        MerchantOffers offers = new MerchantOffers();
        int numRows = inventory.getSlots() / INVENTORY_ROW_SIZE;
        List<Integer> rowCost = new ArrayList<>();
        List<List<ItemStack>> rowItems = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            int row = slot / INVENTORY_ROW_SIZE;
            if (rowCost.size() <= row) {
                rowCost.add(1);
            }
            if (rowItems.size() <= row) {
                rowItems.add(new ArrayList<>());
            }
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
        int numItemsToBuy = rowsWithItems.size() > 1 ? random.nextInt(1, rowsWithItems.size()) : rowsWithItems.size();
        while (numItemsToBuy > 0) {
            int rowNum = random.nextInt(rowsWithItems.size());
            int row = rowsWithItems.get(rowNum);

            int itemNum = rowItems.get(row).size() > 1 ? random.nextInt(rowItems.get(row).size()) : 0;
            ItemStack itemStack = rowItems.get(row).get(itemNum);
            int count = itemStack.getCount() > 1 ? random.nextInt(1, itemStack.getCount()) : 1;
            offers.add(new MerchantOffer(
                    new ItemCost(itemStack.getItem(), count),
                    Optional.empty(),
                    new ItemStack(Items.EMERALD, rowCost.get(row)),
                    1,
                    rowCost.get(row),
                    0
            ));

            rowsWithItems.remove(rowNum);
            numItemsToBuy--;
        }

        return offers;
    }

    public static final String NAME = "customer_spawner_block_entity";

    private boolean needsUpdate = true;
    private long updateDelayTicks = 0;
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_ROW_SIZE * 6) {
        @Override
        protected void onContentsChanged(int slot) {
            LOGGER.debug("Inventory changed");
            setChanged();
        }
    };
    private final Set<UUID> customerIds = new HashSet<>();

    public CustomerSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CustomerSpawner.CUSTOMER_SPAWNER_ENTITY.get(), pos, blockState);
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

        updateSpawned();
        try {
            ListTag customersTag = new ListTag();
            for (UUID uuid : customerIds) {
                try {
                    customersTag.add(NbtUtils.createUUID(uuid));
                } catch (Throwable t) {
                    LOGGER.error("Couldn't add customer to saved list because of error", t);
                }
            }
            tag.put("customers", customersTag);
        } catch (Throwable t) {
            LOGGER.error("Failed to save customers", t);
        }
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
        if (tag.contains("customers")) {
            try {
                ListTag customersTag = tag.getList("customers", Tag.TAG_INT_ARRAY);
                for (int i = 0; i < customersTag.size(); i++) {
                    try {
                        customerIds.add(NbtUtils.loadUUID(customersTag.getCompound(i)));
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to load one of the customers because of error", t);
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("Failed to load customers because of error", t);
            }
        }
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

        // Despawn customers
        for (UUID uuid : customerIds) {
            Entity customerEntity = ((ServerLevel)level).getEntity(uuid);
            if (customerEntity instanceof CustomerVillagerEntity) {
                customerEntity.discard();
            }
        }
        customerIds.clear();
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

    /* package private */ static BlockState updateState(Level level, BlockPos pos, BlockState currentState) {
        CustomerSpawnerMode spawnerMode = currentState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
        boolean wasDisabled = currentState.getValue(CustomerSpawnerBlock.STATE_DISABLED);
        boolean wasPowered = currentState.getValue(CustomerSpawnerBlock.STATE_POWERED);
        boolean wasSpecialEnabled = currentState.getValue(CustomerSpawnerBlock.STATE_SPECIAL_ENABLED);

        boolean powered = level.hasNeighborSignal(pos);
        boolean disabled;
        if (spawnerMode == CustomerSpawnerMode.MANUAL) {
            disabled = !powered || wasPowered;
        } else {
            disabled = powered;
        }

        List<BlockState> neighbors = new ArrayList<>();
        neighbors.add(level.getBlockState(pos.north()));
        neighbors.add(level.getBlockState(pos.south()));
        neighbors.add(level.getBlockState(pos.east()));
        neighbors.add(level.getBlockState(pos.west()));
        boolean specialEnabled = switch (spawnerMode) {
            case NIGHT -> neighbors.stream().anyMatch(blockState -> blockState.is(Blocks.JACK_O_LANTERN));
            default -> false;
        };

        BlockState newState = currentState
                .setValue(CustomerSpawnerBlock.STATE_DISABLED, disabled)
                .setValue(CustomerSpawnerBlock.STATE_POWERED, powered)
                .setValue(CustomerSpawnerBlock.STATE_SPECIAL_ENABLED, specialEnabled);
        LOGGER.debug("spawn_mode[{}].{}: {} -> {}", pos, spawnerMode, wasDisabled, disabled);
        LOGGER.debug("spawn_mode[{}].{}: {} -> {}", pos, spawnerMode, wasPowered, powered);
        LOGGER.debug("spawn_mode[{}].{}: {} -> {}", pos, spawnerMode, wasSpecialEnabled, specialEnabled);
        return newState;
    }

    /* package private */ void updateState() {
        boolean wasDisabled = getBlockState().getValue(CustomerSpawnerBlock.STATE_DISABLED);
        BlockState newState = updateState(getLevel(), getBlockPos(), getBlockState());
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
        if (
                newState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE) == CustomerSpawnerMode.MANUAL &&
                newState.getValue(CustomerSpawnerBlock.STATE_DISABLED) != wasDisabled
        ) {
            needsUpdate = true;
            updateDelayTicks = 4;
        }
    }

    /* package private */ static BlockState cycleSpawnMode(Level level, BlockPos pos, BlockState currentState) {
        CustomerSpawnerMode spawnerMode = currentState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
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
        BlockState newState = updateState(
                level,
                pos,
                currentState.setValue(CustomerSpawnerBlock.STATE_SPAWN_MODE, nextSpawnerMode)
        );
        LOGGER.debug("spawn_mode[{}]: {} -> {}", pos, spawnerMode, nextSpawnerMode);
        return newState;
    }

    /* package private */ void cycleSpawnMode() {
        BlockState newState = cycleSpawnMode(getLevel(), getBlockPos(), getBlockState());
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public void spawnCustomer() {
        MerchantOffers offers = getOffersFromInventory(level.getRandom(), inventory);
        LOGGER.debug("Trying to spawn customer: Num offers = {}", offers.size());
        if (!offers.isEmpty()) {
            BlockState counterBlockState = level.getBlockState(getBlockPos().above());

            LOGGER.debug("Spawning a customer");
            CustomerVillagerEntity customer = CustomerVillagerEntity.spawn(
                    level,
                    getBlockPos(),
                    offers,
                    counterBlockState
            );
            if (customer != null) {
                LOGGER.debug("Customer UUID={}, pos={}", customer.getUUID(), customer.blockPosition());
                customerIds.add(customer.getUUID());
                setChanged();
            }
        }
    }

    private void updateSpawned() {
        Set<UUID> idsToRemove = new HashSet<>();
        for (UUID id : customerIds) {
            try {
                Entity entity = ((ServerLevel) level).getEntity(id);
                if (entity instanceof CustomerVillagerEntity customer) {
                    if (!customer.isAlive() || customer.isRemoved()) {
                        idsToRemove.add(id);
                    }
                } else {
                    idsToRemove.add(id);
                }
            } catch (Throwable t) {
                LOGGER.warn("Removing customer from tracking because of error", t);
                idsToRemove.add(id);
            }
        }
        customerIds.removeAll(idsToRemove);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomerSpawnerBlockEntity entity) {
        if (!level.isClientSide()) {
            if (entity.needsUpdate) {
                if (entity.updateDelayTicks <= 0) {
                    entity.needsUpdate = false;
                    entity.updateState();
                }
                entity.updateDelayTicks--;
            }

            Set<UUID> customerIdsToRemove = new HashSet<>();
            for (UUID uuid : entity.customerIds) {
                Entity customerEntity = ((ServerLevel)level).getEntity(uuid);
                if (customerEntity == null || !customerEntity.isAlive() || !(customerEntity instanceof CustomerVillagerEntity)) {
                    customerIdsToRemove.add(uuid);
                }
            }
            entity.customerIds.removeAll(customerIdsToRemove);
            if (entity.customerIds.size() < MAX_CUSTOMERS) {
                CustomerSpawnerMode spawnerMode = state.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
                long timeOfDay = level.getDayTime() % 24000;
            }
        }
    }
}
