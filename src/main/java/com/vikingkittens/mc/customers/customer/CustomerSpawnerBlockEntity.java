package com.vikingkittens.mc.customers.customer;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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
import net.neoforged.neoforge.network.PacketDistributor;

import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearanceSettings;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.PlayerCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;
import com.vikingkittens.mc.customers.config.Config;

public class CustomerSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    static final int CURRENT_DATA_VERSION = 1;
    static final int MIN_MAX_CUSTOMERS = 1;
    static final int MAX_MAX_CUSTOMERS = 99;
    static final String TAG_DATA_VERSION = "data_version";
    static final String TAG_MAX_CUSTOMERS = "maxCustomers";

    private final CustomersVillagerAppearanceSettings appearanceSettings =
            new CustomersVillagerAppearanceSettings();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int INVENTORY_ROW_SIZE = 9;
    private static final int SPAWN_CHECK_MAX_TICKS = 4;
    private static final int RESERVATION_CLEANUP_LOAD_GRACE_TICKS = 20 * 30;
    private static final double PLAYER_VIEW_RANGE = 64.0D;

    static Item getPaymentItem() {
        return Items.EMERALD;
    }

    static Item getMaxCustomersItem() {
        return Items.REDSTONE;
    }

    static MerchantOffers getOffersFromInventory(
            RandomSource random,
            ItemStackHandler inventory,
            Supplier<Item> defaultPaymentItem
    ) {
        MerchantOffers offers = new MerchantOffers();
        int numRows = inventory.getSlots() / INVENTORY_ROW_SIZE;
        List<ItemStack> rowCosts = new ArrayList<>();
        List<List<ItemStack>> rowItems = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            int row = slot / INVENTORY_ROW_SIZE;
            int column = slot % INVENTORY_ROW_SIZE;
            if (rowCosts.size() <= row) {
                rowCosts.add(new ItemStack(defaultPaymentItem.get()));
            }
            if (rowItems.size() <= row) {
                rowItems.add(new ArrayList<>());
            }
            ItemStack stack = inventory.getStackInSlot(slot);
            if (column == INVENTORY_ROW_SIZE - 1) {
                if (!stack.isEmpty()) {
                    rowCosts.set(row, stack.copy());
                }
            } else if (!stack.isEmpty()) {
                rowItems.get(row).add(stack);
            }
        }
        List<Integer> rowsWithItems = new ArrayList<>();
        for (int row = 0; row < numRows; row++) {
            if (!rowItems.get(row).isEmpty()) {
                rowsWithItems.add(row);
            }
        }
        int numItemsToBuy = rowsWithItems.size() > 1 ? random.nextIntBetweenInclusive(1, rowsWithItems.size()) : rowsWithItems.size();
        while (numItemsToBuy > 0) {
            int rowNum = random.nextInt(rowsWithItems.size());
            int row = rowsWithItems.get(rowNum);

            int itemNum = rowItems.get(row).size() > 1 ? random.nextInt(rowItems.get(row).size()) : 0;
            ItemStack itemStack = rowItems.get(row).get(itemNum);
            int count = itemStack.getCount() > 1 ? random.nextIntBetweenInclusive(1, itemStack.getCount()) : 1;
            ItemStack paymentStack = rowCosts.get(row).copy();
            paymentStack.setCount(paymentStack.getCount() * count);
            offers.add(new MerchantOffer(
                    ItemStackCUtils.createItemCost(itemStack, count),
                    Optional.empty(),
                    paymentStack,
                    1,
                    rowCosts.get(row).getCount(),
                    0
            ));

            rowsWithItems.remove(rowNum);
            numItemsToBuy--;
        }

        return offers;
    }

    static MerchantOffers getOffersFromInventory(
            RandomSource random,
            ItemStackHandler inventory
    ) {
        return getOffersFromInventory(
                random,
                inventory,
                CustomerSpawnerBlockEntity::getPaymentItem
        );
    }

    public static final String NAME = "customer_spawner_block_entity";

    private boolean ticksDisabled = false;
    private long ticksSinceTicksDisabledCheck = 0;

    private boolean needsUpdate = true;
    private long updateDelayTicks = 0;
    private int maxCustomers = clampMaxCustomers(
            Config.MAX_CUSTOMERS.get()
    );
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_ROW_SIZE * 6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private long spawnCheckTicks = 0;
    private final Set<UUID> customerIds = new HashSet<>();
    private final Map<BlockPos, List<UUID>> reservedTargetCounterPositions = new HashMap<>();
    private int reservationCleanupLoadTicks;
    private ServerBossEvent progressBar;
    private final Set<UUID> playerIds = new HashSet<>();
    private long ticksSinceUpdateSpawned = 0;
    private long ticksSinceUpdatePlayers = 0;
    private int totalCustomers = 0;
    private int numCustomersServed = 0;
    private int totalItemsWanted = 0;
    private int numCustomersGaveUp = 0;
    private final Map<UUID, Integer> numItemsServedByPlayer = new HashMap<>();
    private final Map<UUID, Integer> numItemsCraftedByPlayer = new HashMap<>();

    /**
     * Assigns crafted items to active customers tracked by this spawner.
     *
     * @param player player who crafted the items
     * @param stack items available for assignment
     * @return null when fully assigned, an unassigned remainder when partially
     *         assigned, or the original stack when nothing matched
     */
    public @Nullable ItemStack tryAssignCraftedItem(
            Player player,
            ItemStack stack
    ) {
        return tryAssignCraftedItem(player.getUUID(), stack);
    }

    /**
     * Assigns crafted items to active customers and credits the supplied
     * crafting-player UUID, including when that player is offline.
     *
     * @param playerId player who crafted the items
     * @param stack items available for assignment
     * @return null when fully assigned, an unassigned remainder when partially
     *         assigned, or the original stack when nothing matched
     */
    public @Nullable ItemStack tryAssignCraftedItem(
            UUID playerId,
            ItemStack stack
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return stack;
        }
        return tryAssignCraftedItem(
                getActiveCustomers(serverLevel, customerIds),
                numItemsCraftedByPlayer,
                playerId,
                stack
        );
    }

    /**
     * Returns how many items active customers could accept without changing
     * customer assignments or player scores.
     *
     * @param stack items being considered
     * @return number of items that could be assigned
     */
    public int getAssignableCraftedItemCount(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        return getAssignableCraftedItemCount(
                getActiveCustomers(serverLevel, customerIds),
                stack
        );
    }

    /**
     * Reserves items for active customers without changing crafted scores.
     *
     * @param stack items being reserved
     * @return null when fully reserved, otherwise the unreserved remainder
     */
    public @Nullable ItemStack tryReserveCraftedItem(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return stack;
        }
        return tryReserveCraftedItem(
                getActiveCustomers(serverLevel, customerIds),
                stack
        );
    }

    public int releaseCraftedItemAssignment(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        return releaseCraftedItemAssignment(
                getActiveCustomers(serverLevel, customerIds),
                stack
        );
    }
    /**
     * Calculates assignable demand across customers without changing state.
     *
     * @param customers active customers eligible for assignment
     * @param stack items being considered
     * @return number of items that could be assigned
     */
    static int getAssignableCraftedItemCount(
            List<CustomerVillagerEntity> customers,
            ItemStack stack
    ) {
        int assignableCount = 0;
        for (CustomerVillagerEntity customer : customers) {
            int remainingCount = stack.getCount() - assignableCount;
            if (remainingCount == 0) {
                break;
            }
            ItemStack remaining = stack.copy();
            remaining.setCount(remainingCount);
            assignableCount +=
                    customer.getAssignableCraftedItemCount(remaining);
        }
        return assignableCount;
    }

    /**
     * Reserves a crafted stack across customers without changing player scores.
     *
     * @param customers active customers eligible for reservation
     * @param stack crafted items being reserved
     * @return null when fully reserved, otherwise the unreserved remainder
     */
    static @Nullable ItemStack tryReserveCraftedItem(
            List<CustomerVillagerEntity> customers,
            ItemStack stack
    ) {
        ItemStack remainder = stack;
        for (CustomerVillagerEntity customer : customers) {
            remainder = customer.tryAssignCraftedOffer(remainder);
            if (remainder == null) {
                break;
            }
        }
        return remainder;
    }

    static int releaseCraftedItemAssignment(
            List<CustomerVillagerEntity> customers,
            ItemStack stack
    ) {
        int releasedCount = 0;
        for (CustomerVillagerEntity customer : customers) {
            int remainingCount = stack.getCount() - releasedCount;
            if (remainingCount == 0) {
                break;
            }
            ItemStack remaining = stack.copy();
            remaining.setCount(remainingCount);
            releasedCount +=
                    customer.releaseCraftedOfferAssignment(remaining);
        }
        return releasedCount;
    }
    /**
     * Assigns a crafted stack across customers and records the number of item
     * units assigned to the crafting player.
     *
     * @param customers active customers eligible for assignment
     * @param craftedByPlayer crafted item counts keyed by player
     * @param playerId crafting player
     * @param stack items available for assignment
     * @return null when fully assigned, an unassigned remainder when partially
     *         assigned, or the original stack when nothing matched
     */
    static @Nullable ItemStack tryAssignCraftedItem(
            List<CustomerVillagerEntity> customers,
            Map<UUID, Integer> craftedByPlayer,
            UUID playerId,
            ItemStack stack
    ) {
        ItemStack remainder = stack;
        int assignedCount = 0;
        for (CustomerVillagerEntity customer : customers) {
            int countBefore = remainder.getCount();
            ItemStack nextRemainder =
                    customer.tryAssignCraftedOffer(remainder);
            int countAfter =
                    nextRemainder == null ? 0 : nextRemainder.getCount();
            assignedCount += countBefore - countAfter;
            if (nextRemainder == null) {
                remainder = null;
                break;
            }
            remainder = nextRemainder;
        }
        if (assignedCount > 0) {
            craftedByPlayer.merge(playerId, assignedCount, Integer::sum);
        }
        return remainder;
    }

    public CustomerSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CustomerSpawner.CUSTOMER_SPAWNER_ENTITY.get(), pos, blockState);
    }

    static int clampMaxCustomers(int value) {
        return Math.clamp(
                value,
                MIN_MAX_CUSTOMERS,
                MAX_MAX_CUSTOMERS
        );
    }

    public int getMaxCustomers() {
        return maxCustomers;
    }

    public void setMaxCustomers(int maxCustomers) {
        this.maxCustomers = clampMaxCustomers(maxCustomers);
        setChanged();
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
        output.putInt(TAG_MAX_CUSTOMERS, maxCustomers);
        appearanceSettings.write(output);
        try {
            output.putUuids("customers", customerIds);
        } catch (Throwable t) {
            LOGGER.error("Failed to save customers", t);
        }
        saveReservedTargetCounterPositions(output, reservedTargetCounterPositions);
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
        maxCustomers = clampMaxCustomers(
                input.getInt(TAG_MAX_CUSTOMERS)
                        .orElseGet(Config.MAX_CUSTOMERS::get)
        );
        migrateData(loadedDataVersion);
        appearanceSettings.read(input);
        try {
            customerIds.clear();
            input.getUuids("customers").forEach(uuid -> {
                try {
                    customerIds.add(uuid);
                } catch (Throwable t) {
                    LOGGER.warn("Failed to load one of the customers because of error", t);
                }
            });
        } catch (Throwable t) {
            LOGGER.error("Failed to load customers because of error", t);
        }
        reservedTargetCounterPositions.clear();
        reservedTargetCounterPositions.putAll(loadReservedTargetCounterPositions(input));
    }

    private void migrateData(int loadedDataVersion) {
        int dataVersion = Math.max(0, loadedDataVersion);
        while (dataVersion < CURRENT_DATA_VERSION) {
            if (dataVersion == 0) {
                InventoryDataMigrationResult result =
                        migrateVersion0Inventory(
                                inventory,
                                getPaymentItem(),
                                getMaxCustomersItem(),
                                maxCustomers
                        );
                maxCustomers = result.maxCustomers();
            } else {
                LOGGER.warn(
                        "Unable to migrate unknown customer spawner data version {}",
                        dataVersion
                );
                return;
            }
            dataVersion++;
        }
    }

    static InventoryDataMigrationResult migrateVersion0Inventory(
            ItemStackHandler inventory,
            Item paymentItem,
            Item maxCustomersItem,
            int fallbackMaxCustomers
    ) {
        int maxCustomers = clampMaxCustomers(fallbackMaxCustomers);
        boolean maxCustomersFound = false;
        boolean changed = false;

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(maxCustomersItem)) {
                if (!maxCustomersFound) {
                    maxCustomers = clampMaxCustomers(stack.getCount());
                    maxCustomersFound = true;
                }
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                changed = true;
            }
        }

        int rowCount = inventory.getSlots() / INVENTORY_ROW_SIZE;
        for (int row = 0; row < rowCount; row++) {
            int rowStart = row * INVENTORY_ROW_SIZE;
            int costSlot = rowStart + INVENTORY_ROW_SIZE - 1;
            if (inventory.getStackInSlot(costSlot).is(paymentItem)) {
                continue;
            }
            for (int slot = rowStart; slot < costSlot; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.is(paymentItem)) {
                    ItemStack previousCost =
                            inventory.getStackInSlot(costSlot).copy();
                    inventory.setStackInSlot(costSlot, stack.copy());
                    inventory.setStackInSlot(slot, previousCost);
                    changed = true;
                    break;
                }
            }
        }

        return new InventoryDataMigrationResult(maxCustomers, changed);
    }

    record InventoryDataMigrationResult(
            int maxCustomers,
            boolean changed
    ) {}
    static void saveReservedTargetCounterPositions(
            DataWriter output,
            Map<BlockPos, List<UUID>> reservations
    ) {
        reservations.forEach((position, customerIds) -> {
            DataWriter reservationOutput = output.addChild("reservedTargetCounterPositions");
            reservationOutput.putBlockPos("targetPosition", position);
            reservationOutput.putUuids("customerIds", customerIds);
        });
    }
    static Map<BlockPos, List<UUID>> loadReservedTargetCounterPositions(DataReader input) {
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        for (DataReader reservationInput : input.getChildren("reservedTargetCounterPositions")) {
            List<UUID> customerIds = new ArrayList<>(reservationInput.getUuids("customerIds"));
            reservationInput.getBlockPos("targetPosition").ifPresent(position ->
                    reservations.put(position, customerIds)
            );
        }
        return reservations;
    }

    public UUID tryReserveTargetCounterPosition(BlockPos targetPosition, UUID customerId) {
        UUID counterCustomerId = tryReserveTargetCounterPosition(
                reservedTargetCounterPositions,
                targetPosition,
                customerId,
                this::isActiveCustomer
        );
        setChanged();
        return counterCustomerId;
    }

    static UUID tryReserveTargetCounterPosition(
            Map<BlockPos, List<UUID>> reservations,
            BlockPos targetPosition,
            UUID customerId,
            Predicate<UUID> activeCustomer
    ) {
        List<UUID> customerIds = reservations.computeIfAbsent(
                targetPosition,
                ignored -> new ArrayList<>()
        );
        customerIds.removeIf(id -> !activeCustomer.test(id));
        if (!customerIds.contains(customerId)) {
            customerIds.addFirst(customerId);
        }
        return customerIds.getLast();
    }

    public UUID getReservedTargetCounterPositionFollowingCustomerId(
            BlockPos targetPosition,
            UUID customerId
    ) {
        UUID followingCustomerId = getReservedTargetCounterPositionFollowingCustomerId(
                reservedTargetCounterPositions,
                targetPosition,
                customerId,
                this::isActiveCustomer
        );
        setChanged();
        return followingCustomerId;
    }

    static UUID getReservedTargetCounterPositionFollowingCustomerId(
            Map<BlockPos, List<UUID>> reservations,
            BlockPos targetPosition,
            UUID customerId,
            Predicate<UUID> activeCustomer
    ) {
        List<UUID> customerIds = reservations.get(targetPosition);
        if (customerIds == null) {
            return null;
        }
        customerIds.removeIf(id -> !activeCustomer.test(id));
        if (customerIds.isEmpty()) {
            reservations.remove(targetPosition);
            return null;
        }
        int customerIndex = customerIds.indexOf(customerId);
        if (customerIndex < 0 || customerIndex == customerIds.size() - 1) {
            return null;
        }
        return customerIds.get(customerIndex + 1);
    }

    public Map<BlockPos, List<UUID>> getReservedTargetCounterPositions() {
        boolean removed = false;
        Iterator<Map.Entry<BlockPos, List<UUID>>> iterator =
                reservedTargetCounterPositions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, List<UUID>> entry = iterator.next();
            if (entry.getValue().removeIf(id -> !isActiveCustomer(id))) {
                removed = true;
            }
            if (entry.getValue().isEmpty()) {
                iterator.remove();
                removed = true;
            }
        }
        if (removed) {
            setChanged();
        }
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        reservedTargetCounterPositions.forEach((position, customerIds) ->
                reservations.put(position, List.copyOf(customerIds))
        );
        return Map.copyOf(reservations);
    }

    private boolean isActiveCustomer(UUID customerId) {
        if (
                reservationCleanupLoadTicks < RESERVATION_CLEANUP_LOAD_GRACE_TICKS &&
                level instanceof ServerLevel serverLevel &&
                serverLevel.getEntity(customerId) == null
        ) {
            return true;
        }
        return CustomerVillagerEntity.isActiveCustomer(level, customerId);
    }
    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("block.customers.customer_spawner_block");
    }
    public void beforeRemove() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, container);

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

        return new CustomerSpawnerBlockMenu(containerId, playerInventory, containerBridge, this);
    }

    static BlockState updateState(Level level, BlockPos pos, BlockState currentState) {
        CustomerSpawnerMode spawnerMode = currentState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);

        boolean wasDisabled = currentState.getValue(CustomerSpawnerBlock.STATE_DISABLED);
        boolean wasPowered = currentState.getValue(CustomerSpawnerBlock.STATE_POWERED);
        boolean powered = level.hasNeighborSignal(pos);
        boolean disabled;
        if (spawnerMode == CustomerSpawnerMode.MANUAL) {
            disabled = !powered || wasPowered;
        } else {
            disabled = powered;
        }

        return currentState
                .setValue(CustomerSpawnerBlock.STATE_DISABLED, disabled)
                .setValue(CustomerSpawnerBlock.STATE_POWERED, powered);
    }

    void updateState() {
        boolean wasDisabled = getBlockState().getValue(CustomerSpawnerBlock.STATE_DISABLED);

        BlockState newState = updateState(getLevel(), getBlockPos(), getBlockState());
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);

        if (
                newState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE) == CustomerSpawnerMode.MANUAL &&
                newState.getValue(CustomerSpawnerBlock.STATE_DISABLED) != wasDisabled
        ) {
            needsUpdate = true;
            updateDelayTicks = 2;
        }
    }

    static BlockState cycleSpawnMode(Level level, BlockPos pos, BlockState currentState) {
        CustomerSpawnerMode spawnerMode = currentState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);

        CustomerSpawnerMode nextSpawnerMode = switch (spawnerMode) {
            case CONTINUOUS -> CustomerSpawnerMode.DAY;
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
        return newState;
    }

    void cycleSpawnMode() {
        BlockState newState = cycleSpawnMode(getLevel(), getBlockPos(), getBlockState());

        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public CustomerSpawnerMode getSpawnerMode() {
        return getBlockState().getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
    }

    public void setSpawnerMode(CustomerSpawnerMode spawnerMode) {
        BlockState newState = updateState(getLevel(), getBlockPos(), getBlockState().setValue(CustomerSpawnerBlock.STATE_SPAWN_MODE, spawnerMode));
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
        setChanged();
    }

    public Set<UUID> getCustomerIds() {
        return customerIds;
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

    public void spawnCustomer() {
        MerchantOffers offers = getOffersFromInventory(level.getRandom(), inventory);
        if (!offers.isEmpty()) {
            BlockState counterBlockState = level.getBlockState(getBlockPos().above());
            BlockState avoidBlockState = level.getBlockState(getBlockPos().below());
            CustomerSpawnerMode spawnerMode =
                    getBlockState().getValue(
                            CustomerSpawnerBlock.STATE_SPAWN_MODE
                    );
            CustomerVillagerEntity customer = CustomerVillagerEntity.spawn(
                    level,
                    getBlockPos(),
                    offers,
                    counterBlockState,
                    avoidBlockState
            );
            if (customer != null) {
                float variationSeed = level.getRandom().nextFloat();
                customer.setAppearanceContext(
                        CustomersVillagerAppearances.DEFAULT,
                        variationSeed,
                        spawnerMode,
                        false
                );
                customer.setAppearanceId(
                        CustomersVillagerAppearances.select(
                                appearanceSettings.getEnabledAppearances(),
                                customer,
                                level.getRandom()::nextInt
                        )
                );
                customerIds.add(customer.getUUID());
                setChanged();
                scoreboardAddCustomer();
                scoreboardAddItemsWanted(offers.size());
            }
        }
    }

    private void updateSpawned() {
        Set<UUID> idsToRemove = new HashSet<>();
        for (UUID customerId : customerIds) {
            try {
                Entity entity = ((ServerLevel)level).getEntity(customerId);
                if (entity instanceof CustomerVillagerEntity customer) {
                    if (!customer.isAlive() || customer.isRemoved()) {
                        idsToRemove.add(customerId);
                    }
                } else {
                    idsToRemove.add(customerId);
                }
            } catch (Throwable t) {
                LOGGER.warn("Removing customer " + customerId + " from tracking because of error", t);
                idsToRemove.add(customerId);
            }
        }
        customerIds.removeAll(idsToRemove);
    }

    private long countActiveCustomers() {
        ServerLevel serverLevel = (ServerLevel) level;
        return customerIds.stream()
                .map(serverLevel::getEntity)
                .filter(CustomerVillagerEntity.class::isInstance)
                .map(CustomerVillagerEntity.class::cast)
                .filter(customer -> customer.getState() != null)
                .filter(customer -> customer.getState().countsTowardSpawnerLimit())
                .count();
    }

    static Set<UUID> getPlayerIdsInRange(
            BlockPos spawnerPos,
            Collection<ServerPlayer> players,
            double range
    ) {
        double rangeSquared = range * range;
        Set<UUID> playerIds = new HashSet<>();
        for (ServerPlayer player : players) {
            if (
                    player.blockPosition().distToCenterSqr(
                            spawnerPos.getCenter()
                    ) <= rangeSquared
            ) {
                playerIds.add(player.getUUID());
            }
        }
        return playerIds;
    }
    static List<CustomerVillagerEntity> getActiveCustomers(
            ServerLevel serverLevel,
            Collection<UUID> customerIds
    ) {
        return customerIds.stream()
                .filter(customerId ->
                        CustomerVillagerEntity.isActiveCustomer(
                                serverLevel,
                                customerId
                        ))
                .map(serverLevel::getEntity)
                .map(CustomerVillagerEntity.class::cast)
                .toList();
    }

    static PlayerRangeChanges getPlayerRangeChanges(
            Set<UUID> currentPlayerIds,
            Set<UUID> nextPlayerIds
    ) {
        Set<UUID> entering = new HashSet<>(nextPlayerIds);
        entering.removeAll(currentPlayerIds);
        Set<UUID> leaving = new HashSet<>(currentPlayerIds);
        leaving.removeAll(nextPlayerIds);
        return new PlayerRangeChanges(entering, leaving);
    }

    static Set<UUID> getPlayerIdsToAddToBossBar(
            Set<UUID> bossBarPlayerIds,
            Set<UUID> inRangePlayerIds
    ) {
        Set<UUID> playerIdsToAdd = new HashSet<>(inRangePlayerIds);
        playerIdsToAdd.removeAll(bossBarPlayerIds);
        return Set.copyOf(playerIdsToAdd);
    }

    record PlayerRangeChanges(
            Set<UUID> entering,
            Set<UUID> leaving
    ) {
        PlayerRangeChanges {
            entering = Set.copyOf(entering);
            leaving = Set.copyOf(leaving);
        }
    }
    public void addPlayer(UUID playerId) {
        if (!playerIds.contains(playerId)) {
            updatePlayers();
        }
    }
    public void updatePlayers() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        List<ServerPlayer> levelPlayers = serverLevel.players();
        Set<UUID> nextPlayerIds = getPlayerIdsInRange(
                getBlockPos(),
                levelPlayers,
                PLAYER_VIEW_RANGE
        );
        PlayerRangeChanges changes = getPlayerRangeChanges(
                playerIds,
                nextPlayerIds
        );

        for (UUID playerId : changes.leaving()) {
            ServerPlayer player = serverLevel.getServer()
                    .getPlayerList()
                    .getPlayer(playerId);
            if (player != null) {
                if (progressBar != null) {
                    progressBar.removePlayer(player);
                }
                sendSnapshotRemoval(player);
            }
        }

        playerIds.clear();
        playerIds.addAll(nextPlayerIds);

        Map<UUID, ServerPlayer> playersById = new HashMap<>();
        for (ServerPlayer player : levelPlayers) {
            playersById.put(player.getUUID(), player);
        }

        if (progressBar != null) {
            Set<UUID> bossBarPlayerIds = progressBar.getPlayers()
                    .stream()
                    .map(ServerPlayer::getUUID)
                    .collect(Collectors.toSet());
            Set<UUID> playerIdsToAdd = getPlayerIdsToAddToBossBar(
                    bossBarPlayerIds,
                    playerIds
            );
            for (UUID playerId : playerIdsToAdd) {
                ServerPlayer player = playersById.get(playerId);
                if (player != null) {
                    progressBar.addPlayer(player);
                }
            }
        }

        CustomerSpawnerSnapshot snapshot = createSnapshot(serverLevel);
        for (UUID playerId : playerIds) {
            ServerPlayer player = playersById.get(playerId);
            if (player != null) {
                PacketDistributor.sendToPlayer(
                        player,
                        new CustomerSpawnerSnapshotPayload(
                                getBlockPos(),
                                Optional.of(snapshot)
                        )
                );
            }
        }
    }

    private CustomerSpawnerSnapshot createSnapshot(ServerLevel serverLevel) {
        List<CustomerVillagerEntity> customers = getActiveCustomers(
                serverLevel,
                customerIds
        );
        BlockState state = getBlockState();
        Optional<UUID> bossEventId =
                progressBar != null && progressBar.isVisible()
                        ? Optional.of(progressBar.getId())
                        : Optional.empty();
        return CustomerSpawnerSnapshot.create(
                getBlockPos(),
                state.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE),
                bossEventId,
                customers
        );
    }

    private void sendSnapshotRemoval(ServerPlayer player) {
        PacketDistributor.sendToPlayer(
                player,
                new CustomerSpawnerSnapshotPayload(
                        getBlockPos(),
                        Optional.empty()
                )
        );
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            for (UUID playerId : playerIds) {
                ServerPlayer player = serverLevel.getServer()
                        .getPlayerList()
                        .getPlayer(playerId);
                if (player != null) {
                    sendSnapshotRemoval(player);
                }
            }
            if (progressBar != null) {
                progressBar.removeAllPlayers();
            }
            playerIds.clear();
        }
        super.setRemoved();
    }
    public void sentPlayersMessage(Component message) {
        if (!LevelCUtils.isClientSide(level)) {
            for (UUID playerId : playerIds) {
                try {
                    Player player = level.getPlayerByUUID(playerId);
                    PlayerCUtils.sendActionBarMessage(player, message);
                } catch (Throwable t) {
                    LOGGER.warn("Unable to send message to player because of error", t);
                }
            }
        }
    }

    public void sentPlayersChat(Component message) {
        if (!LevelCUtils.isClientSide(level)) {
            for (UUID playerId : playerIds) {
                try {
                    Player player = level.getPlayerByUUID(playerId);
                    PlayerCUtils.sendSystemMessage(player, message);
                } catch (Throwable t) {
                    LOGGER.warn("Unable to send chat to player because of error", t);
                }
            }
        }
    }

    private boolean hasScoreboard() {
        return totalCustomers > 0;
    }

    private void scoreboardReset() {
        totalCustomers = 0;
        numCustomersServed = 0;
        totalItemsWanted = 0;
        numCustomersGaveUp = 0;
        numItemsServedByPlayer.clear();
        numItemsCraftedByPlayer.clear();
    }

    private float scoreboardGetPercentage() {
        int totalItemsServed = numItemsServedByPlayer.values().stream()
                .reduce(0, Integer::sum);
        return ((float)totalItemsServed / (float)totalItemsWanted);
    }

    private void sendShiftFinishedPayload(CustomerSpawnerMode spawnerMode) {
        CustomerShiftFinishedPayload payload = new CustomerShiftFinishedPayload(
                spawnerMode,
                scoreboardGetPercentage(),
                totalCustomers,
                numCustomersServed,
                numCustomersGaveUp,
                numItemsServedByPlayer,
                numItemsCraftedByPlayer
        );
        for (UUID playerId : playerIds) {
            try {
                Player player = level.getPlayerByUUID(playerId);
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, payload);
                }
            } catch (Throwable throwable) {
                LOGGER.warn("Unable to send completed customer shift results to player {}", playerId, throwable);
            }
        }
    }

    private void scoreboardShow() {
    }

    private void scoreboardShowFinal() {
        CustomerSpawnerMode spawnerMode = getBlockState().getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
        if (!CustomerSpawnerMode.shouldShowScore(spawnerMode)) {
            return;
        }

        int color = 0x36991C;
        if (scoreboardGetPercentage() <= 0.25) {
            color = 0xFF0000;
        } else if (scoreboardGetPercentage() <= 0.50) {
            color = 0xFE8B00;
        }

        sendShiftFinishedPayload(spawnerMode);

        Component summary = Component.translatable(
                "messages.customers.scoreboard.summary",
                spawnerMode.getTitle(),
                (int)(scoreboardGetPercentage() * 100) + "%"
        ).withColor(color);
        sentPlayersMessage(summary);

        sentPlayersChat(summary);
        sentPlayersChat(Component.translatable(
                "messages.customers.scoreboard.detail.total_customers",
                totalCustomers
        ).withColor(color));
        sentPlayersChat(Component.translatable(
                "messages.customers.scoreboard.detail.customers_served",
                numCustomersServed
        ).withColor(color));
        sentPlayersChat(Component.translatable(
                "messages.customers.scoreboard.detail.customers_gave_up",
                numCustomersGaveUp
        ).withColor(color));
        for (UUID playerId : numItemsServedByPlayer.keySet()) {
            try {
                Player player = level.getPlayerByUUID(playerId);
                sentPlayersChat(Component.translatable(
                        "messages.customers.scoreboard.detail.player_served_items",
                        player.getDisplayName(),
                        numItemsServedByPlayer.get(playerId),
                        totalItemsWanted
                ).withColor(color));
            } catch (Throwable t) {
                LOGGER.warn("Unable to add player score because of error", t);
            }
        }
    }

    public void scoreboardAddCustomer() {
        totalCustomers++;
    }

    public void scoreboardAddItemsWanted(int numItemsWanted) {
        totalItemsWanted += numItemsWanted;
    }

    public void scoreboardAddCustomerServed() {
        numCustomersServed++;
    }

    public void scoreboardAddCustomerGaveUp() {
        numCustomersGaveUp++;
    }

    /**
     * Credits the actual number of item units served by a player.
     *
     * @param playerId serving player
     * @param itemCount item units served
     */
    public void scoreboardAddItemsServed(
            UUID playerId,
            int itemCount
    ) {
        numItemsServedByPlayer.merge(
                playerId,
                itemCount,
                Integer::sum
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomerSpawnerBlockEntity entity) {
        if (!LevelCUtils.isClientSide(level)) {
            if (entity.reservationCleanupLoadTicks < RESERVATION_CLEANUP_LOAD_GRACE_TICKS) {
                entity.reservationCleanupLoadTicks++;
            }
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

            if (entity.needsUpdate) {
                if (entity.updateDelayTicks <= 0) {
                    entity.needsUpdate = false;
                    entity.updateState();
                }
                entity.updateDelayTicks--;
            }

            if (
                    entity.ticksSinceUpdatePlayers == 0 ||
                    entity.ticksSinceUpdatePlayers >= 20
            ) {
                entity.ticksSinceUpdatePlayers = 0;
                entity.updatePlayers();
            }
            entity.ticksSinceUpdatePlayers++;

            if (entity.spawnCheckTicks > SPAWN_CHECK_MAX_TICKS) {
                entity.updateSpawned();

                CustomerSpawnerMode spawnerMode = state.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
                long timeOfDay = (level.getDayTime() + 6000L) % 24000L;
                boolean shouldSpawn = CustomerSpawnerMode.shouldSpawn(spawnerMode, timeOfDay);
                if (!state.getValue(CustomerSpawnerBlock.STATE_DISABLED) && shouldSpawn) {
                    float progress = CustomerSpawnerMode.generateProgress(spawnerMode, timeOfDay);
                    int maxCustomers = CustomerSpawnerMode.getVariedMaxCustomers(
                            spawnerMode,
                            entity.getMaxCustomers(),
                            progress
                    );
                    if (entity.countActiveCustomers() < maxCustomers) {
                        BlockState counterBlockState = entity.level.getBlockState(entity.getBlockPos().above());
                        if (!counterBlockState.isEmpty() && !counterBlockState.isAir()) {
                            List<BlockPos> counterPositions = CustomerCounter.findCounterPositions(
                                    entity.level,
                                    entity.getBlockPos(),
                                    counterBlockState
                            );
                            if (!counterPositions.isEmpty()) {
                                entity.spawnCustomer();
                            }
                        }
                    }

                    if (CustomerSpawnerMode.shouldShowProgress(spawnerMode)) {
                        if (entity.progressBar == null) {
                            entity.progressBar = new ServerBossEvent(
                                    spawnerMode.getTitle(),
                                    BossEvent.BossBarColor.GREEN,
                                    BossEvent.BossBarOverlay.PROGRESS
                            );
                            entity.progressBar.setPlayBossMusic(false);
                            entity.progressBar.setCreateWorldFog(false);
                            entity.progressBar.setDarkenScreen(false);
                            entity.updatePlayers();
                        }
                        entity.progressBar.setProgress(1.0F - progress);
                        Component progressBarTitle = spawnerMode.getTitle();
                        if (entity.progressBar.getName().getString() != progressBarTitle.getString()) {
                            entity.progressBar.setName(progressBarTitle);
                        }
                        BossEvent.BossBarColor progressBarColor = BossEvent.BossBarColor.GREEN;
                        if (entity.progressBar.getProgress() <= 0.25F) {
                            progressBarColor = BossEvent.BossBarColor.RED;
                        } else if (entity.progressBar.getProgress() <= 0.5F) {
                            progressBarColor = BossEvent.BossBarColor.YELLOW;
                        }
                        if (entity.progressBar.getColor() != progressBarColor) {
                            entity.progressBar.setColor(progressBarColor);
                        }
                        entity.progressBar.setVisible(true);
                    } else if (entity.progressBar != null) {
                        entity.progressBar.setVisible(false);
                    }
                } else {
                    if (CustomerSpawnerMode.shouldRemoveCustomers(spawnerMode)) {
                        for (UUID customerId : entity.customerIds) {
                            Entity customerEntity = ((ServerLevel)level).getEntity(customerId);
                            if (
                                    customerEntity instanceof CustomerVillagerEntity customer &&
                                    customer.getState().compareTo(CustomerState.BUYING) <= 0
                            ) {
                                customer.setState(CustomerState.FORCED_GIVING_UP);
                            }
                        }
                    }
                    if (entity.progressBar != null) {
                        entity.progressBar.setVisible(false);
                    }
                    if (entity.hasScoreboard()) {
                        entity.scoreboardShowFinal();
                    }
                    entity.scoreboardReset();
                }
                entity.spawnCheckTicks = 0;
            }
            entity.spawnCheckTicks++;
        }
    }
}
