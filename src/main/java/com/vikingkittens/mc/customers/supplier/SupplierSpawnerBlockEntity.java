package com.vikingkittens.mc.customers.supplier;

import java.util.*;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceSettings;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

public class SupplierSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    static final int CURRENT_DATA_VERSION = 1;
    static final String TAG_DATA_VERSION = "data_version";
    private final CustomersVillagerAppearanceSettings appearanceSettings =
            new CustomersVillagerAppearanceSettings();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int INVENTORY_ROW_SIZE = 9;

    static MerchantOffers getOffersFromInventory(
            RandomSource random,
            ItemStackHandler inventory
    ) {
        MerchantOffers offers = new MerchantOffers();
        int rowCount = inventory.getSlots() / INVENTORY_ROW_SIZE;
        for (int row = 0; row < rowCount; row++) {
            int rowStart = row * INVENTORY_ROW_SIZE;
            for (int column = 0; column < 8; column += 2) {
                ItemStack result = inventory.getStackInSlot(
                        rowStart + column
                );
                ItemStack cost = inventory.getStackInSlot(
                        rowStart + column + 1
                );
                if (result.isEmpty() || cost.isEmpty()) {
                    continue;
                }
                offers.add(new MerchantOffer(
                        ItemStackCUtils.createItemCost(
                                cost,
                                cost.getCount()
                        ),
                        Optional.empty(),
                        result.copy(),
                        10,
                        0,
                        0
                ));
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        try {
            tag.put("inventory", this.inventory.serializeNBT(registries));
        } catch (Throwable t) {
            LOGGER.error("Failed to save inventory", t);
        }

        writeSpawnerData(PersistenceCUtils.writer(tag));
    }
    void writeSpawnerData(DataWriter output) {
        output.putInt(TAG_DATA_VERSION, CURRENT_DATA_VERSION);
        appearanceSettings.write(output);
        output.putBoolean("daytimeStateInitialized", daytimeStateInitialized);
        output.putBoolean("lastTickWasDaytime", lastTickWasDaytime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("inventory")) {
            try {
                inventory.deserializeNBT(registries, tag.getCompound("inventory"));
            } catch (Throwable t) {
                LOGGER.error("Failed to load inventory because of error", t);
            }
        }

        readSpawnerData(PersistenceCUtils.reader(tag));
    }
    void readSpawnerData(DataReader input) {
        int loadedDataVersion =
                input.getInt(TAG_DATA_VERSION).orElse(0);
        migrateData(loadedDataVersion);
        appearanceSettings.read(input);
        daytimeStateInitialized = input.getBoolean("daytimeStateInitialized");
        lastTickWasDaytime = input.getBoolean("lastTickWasDaytime");
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("block.customers.supplier_spawner_block");
    }

    public void beforeRemove() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, container);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
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

        return new SupplierSpawnerBlockMenu(
                containerId,
                playerInventory,
                containerBridge,
                this
        );
    }

    private void migrateData(int loadedDataVersion) {
        int dataVersion = Math.max(0, loadedDataVersion);
        while (dataVersion < CURRENT_DATA_VERSION) {
            if (dataVersion == 0) {
                migrateVersion0Inventory(inventory);
            } else {
                LOGGER.warn(
                        "Unable to migrate unknown supplier spawner data version {}",
                        dataVersion
                );
                return;
            }
            dataVersion++;
        }
    }

    static void migrateVersion0Inventory(
            ItemStackHandler inventory
    ) {
        int rowCount = inventory.getSlots() / INVENTORY_ROW_SIZE;
        for (int row = 0; row < rowCount; row++) {
            int rowStart = row * INVENTORY_ROW_SIZE;
            List<ItemStack> legacyRow = new ArrayList<>(8);
            for (int column = 0; column < 8; column++) {
                legacyRow.add(
                        inventory.getStackInSlot(
                                rowStart + column
                        ).copy()
                );
            }

            List<ItemStack> migratedRow = new ArrayList<>(8);
            int column = 0;
            while (column < legacyRow.size()
                    && migratedRow.size() < 8) {
                ItemStack result = legacyRow.get(column);
                if (result.isEmpty() || result.is(Items.EMERALD)) {
                    column++;
                    continue;
                }

                ItemStack cost = new ItemStack(Items.EMERALD);
                if (column + 1 < legacyRow.size()
                        && legacyRow.get(column + 1)
                                .is(Items.EMERALD)) {
                    cost = legacyRow.get(column + 1);
                    column++;
                }
                migratedRow.add(result);
                migratedRow.add(cost);
                column++;
            }

            for (int rowColumn = 0;
                    rowColumn < INVENTORY_ROW_SIZE;
                    rowColumn++) {
                inventory.setStackInSlot(
                        rowStart + rowColumn,
                        ItemStack.EMPTY
                );
            }
            for (int migratedColumn = 0;
                    migratedColumn < migratedRow.size();
                    migratedColumn++) {
                inventory.setStackInSlot(
                        rowStart + migratedColumn,
                        migratedRow.get(migratedColumn)
                );
            }
        }
    }

    static BlockState updateState(Level level, BlockPos pos, BlockState currentState) {
        boolean disabled = level.hasNeighborSignal(pos);

        BlockState newState = currentState
                .setValue(SupplierSpawnerBlock.STATE_DISABLED, disabled);
        return newState;
    }

    void updateState() {
        BlockState newState = updateState(getLevel(), getBlockPos(), getBlockState());

        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public List<ResourceLocation> getEnabledAppearanceIds() {
        return appearanceSettings.getEnabledAppearances();
    }

    public void setEnabledAppearanceIds(
            Collection<ResourceLocation> appearanceIds
    ) {
        appearanceSettings.setEnabledAppearances(appearanceIds);
        setChanged();
    }

    public void spawnSupplier() {
        MerchantOffers offers = getOffersFromInventory(
                level.getRandom(),
                inventory
        );
        if (!offers.isEmpty()) {
            SupplierVillagerEntity supplier = SupplierVillagerEntity.spawn(
                    level,
                    getBlockPos(),
                    offers
            );
            if (supplier != null) {
                float variationSeed = level.getRandom().nextFloat();
                supplier.setAppearanceContext(
                        CustomersVillagerAppearances.DEFAULT,
                        variationSeed
                );
                supplier.setAppearanceId(
                        CustomersVillagerAppearances.select(
                                appearanceSettings.getEnabledAppearances(),
                                supplier,
                                level.getRandom()::nextInt
                        )
                );
                setChanged();
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SupplierSpawnerBlockEntity entity) {
        if (!LevelCUtils.isClientSide(level)) {
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

            boolean isDaytime = LevelCUtils.isDaytime(level);
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
