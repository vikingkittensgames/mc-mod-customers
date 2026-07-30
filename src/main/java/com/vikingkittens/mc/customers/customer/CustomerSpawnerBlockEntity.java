package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CustomerSpawnerBlockEntity extends BlockEntity implements MenuProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int INVENTORY_ROW_SIZE = 9;
    private static final int SPAWN_CHECK_MAX_TICKS = 4;
    private static final int RESERVATION_CLEANUP_LOAD_GRACE_TICKS = 20 * 30;

    static Item getPaymentItem() {
        return Items.EMERALD;
    }

    static Item getMaxCustomersItem() {
        return Items.REDSTONE;
    }

    static MerchantOffers getOffersFromInventory(
            RandomSource random,
            ItemStackHandler inventory,
            Supplier<Item> paymentItem,
            Supplier<Item> maxCustomersItem
    ) {
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
                if (stack.is(paymentItem.get())) {
                    if (rowCost.get(row) < stack.getCount()) {
                        rowCost.set(row, stack.getCount());
                    }
                } else if (!stack.is(maxCustomersItem.get())) {
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
        int numItemsToBuy = rowsWithItems.size() > 1 ? random.nextIntBetweenInclusive(1, rowsWithItems.size()) : rowsWithItems.size();
        while (numItemsToBuy > 0) {
            int rowNum = random.nextInt(rowsWithItems.size());
            int row = rowsWithItems.get(rowNum);

            int itemNum = rowItems.get(row).size() > 1 ? random.nextInt(rowItems.get(row).size()) : 0;
            ItemStack itemStack = rowItems.get(row).get(itemNum);
            int count = itemStack.getCount() > 1 ? random.nextIntBetweenInclusive(1, itemStack.getCount()) : 1;
            offers.add(new MerchantOffer(
                    new ItemCost(itemStack.getItem(), count),
                    Optional.empty(),
                    new ItemStack(paymentItem.get(), rowCost.get(row) * count),
                    1,
                    rowCost.get(row),
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
                CustomerSpawnerBlockEntity::getPaymentItem,
                CustomerSpawnerBlockEntity::getMaxCustomersItem
        );
    }

    static OptionalInt getMaxCustomersOverrideFromInventory(
            ItemStackHandler inventory,
            Supplier<Item> maxCustomersItem
    ) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(maxCustomersItem.get())) {
                return OptionalInt.of(stack.getCount());
            }
        }
        return OptionalInt.empty();
    }

    static OptionalInt getMaxCustomersOverrideFromInventory(ItemStackHandler inventory) {
        return getMaxCustomersOverrideFromInventory(
                inventory,
                CustomerSpawnerBlockEntity::getMaxCustomersItem
        );
    }

    public static final String NAME = "customer_spawner_block_entity";

    private boolean ticksDisabled = false;
    private long ticksSinceTicksDisabledCheck = 0;

    private boolean needsUpdate = true;
    private long updateDelayTicks = 0;
    private OptionalInt maxCustomersOverride = OptionalInt.empty();
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_ROW_SIZE * 6) {
        @Override
        protected void onContentsChanged(int slot) {
            onInventoryUpdate();
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
    // Scoreboard
    private int totalCustomers = 0;
    private int numCustomersServed = 0;
    private int totalItemsWanted = 0;
    private int numCustomersGaveUp = 0;
    private final Map<UUID, Integer> numItemsServedByPlayer = new HashMap<>();

    public CustomerSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
        super(CustomerSpawner.CUSTOMER_SPAWNER_ENTITY.get(), pos, blockState);
    }

    private void onInventoryUpdate() {
        maxCustomersOverride = getMaxCustomersOverrideFromInventory(inventory);
    }

    private int getMaxCustomers() {
        return maxCustomersOverride.orElseGet(Config.MAX_CUSTOMERS::get);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        try {
            inventory.serialize(output.child("inventory"));
        } catch (Throwable t) {
            LOGGER.error("Failed to save inventory", t);
        }

        try {
            ValueOutput.TypedOutputList<UUID> customerOutputs =
                    output.list("customers", UUIDUtil.CODEC);
            for (UUID uuid : customerIds) {
                try {
                    customerOutputs.add(uuid);
                } catch (Throwable t) {
                    LOGGER.error("Couldn't add customer to saved list because of error", t);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to save customers", t);
        }

        saveReservedTargetCounterPositions(output, reservedTargetCounterPositions);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            inventory.deserialize(input.childOrEmpty("inventory"));
        } catch (Throwable t) {
            LOGGER.error("Failed to load inventory because of error", t);
        }
        onInventoryUpdate();
        try {
            customerIds.clear();
            input.listOrEmpty("customers", UUIDUtil.CODEC).forEach(uuid -> {
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

    static void saveReservedTargetCounterPositions(
            ValueOutput output,
            Map<BlockPos, List<UUID>> reservations
    ) {
        ValueOutput.ValueOutputList reservationOutputs =
                output.childrenList("reservedTargetCounterPositions");
        reservations.forEach((position, customerIds) -> {
            ValueOutput reservationOutput = reservationOutputs.addChild();
            reservationOutput.store("targetPosition", BlockPos.CODEC, position);
            ValueOutput.TypedOutputList<UUID> customerIdOutputs =
                    reservationOutput.list("customerIds", UUIDUtil.CODEC);
            for (UUID customerId : customerIds) {
                customerIdOutputs.add(customerId);
            }
        });
    }

    static Map<BlockPos, List<UUID>> loadReservedTargetCounterPositions(ValueInput input) {
        Map<BlockPos, List<UUID>> reservations = new HashMap<>();
        for (ValueInput reservationInput :
                input.childrenListOrEmpty("reservedTargetCounterPositions")) {
            List<UUID> customerIds = new ArrayList<>();
            reservationInput.listOrEmpty("customerIds", UUIDUtil.CODEC)
                    .forEach(customerIds::add);
            reservationInput.read("targetPosition", BlockPos.CODEC).ifPresent(position ->
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
    /**
     * Drops inventory and removes tracked customers before this block entity is removed.
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
            updateDelayTicks = 2;
        }
    }

    /* package private */ static BlockState cycleSpawnMode(Level level, BlockPos pos, BlockState currentState) {
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

    /* package private */ void cycleSpawnMode() {
        BlockState newState = cycleSpawnMode(getLevel(), getBlockPos(), getBlockState());
        level.setBlock(getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public Set<UUID> getCustomerIds() {
        return customerIds;
    }

    public void spawnCustomer() {
        MerchantOffers offers = getOffersFromInventory(level.getRandom(), inventory);
        if (!offers.isEmpty()) {
            BlockState counterBlockState = level.getBlockState(getBlockPos().above());
            BlockState avoidBlockState = level.getBlockState(getBlockPos().below());
            boolean specialEnabled = getBlockState().getValue(CustomerSpawnerBlock.STATE_SPECIAL_ENABLED);

            List<EntityType<? extends CustomerVillagerEntity>> entityTypes = new ArrayList<>();
            entityTypes.add(Customer.CUSTOMER_VILLAGER.get());
            if (specialEnabled) {
                CustomerSpawnerMode spawnerMode = getBlockState().getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
                if (spawnerMode == CustomerSpawnerMode.NIGHT) {
                    entityTypes.add(Customer.CUSTOMER_ZOMBIE.get());
                    entityTypes.add(Customer.CUSTOMER_SKELETON.get());
                    entityTypes.add(Customer.CUSTOMER_WITCH.get());
                    entityTypes.add(Customer.CUSTOMER_HUSK.get());
                    entityTypes.add(Customer.CUSTOMER_DROWNED.get());
                    entityTypes.add(Customer.CUSTOMER_STRAY.get());
                }
            }
            EntityType<? extends CustomerVillagerEntity> entityType = entityTypes.get(level.getRandom().nextInt(entityTypes.size()));
            CustomerVillagerEntity customer = CustomerVillagerEntity.spawn(
                    entityType,
                    level,
                    getBlockPos(),
                    offers,
                    counterBlockState,
                    avoidBlockState
            );
            if (customer != null) {
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

    public void addPlayer(UUID playerId) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
            updatePlayers();
        }
    }
    public void updatePlayers() {
        // Since the customers will add players our job here is to remove them
        Set<UUID> playerIdsToRemove = new HashSet<>();
        for (UUID playerId : playerIds) {
            try {
                Player player = level.getPlayerByUUID(playerId);
                if (player != null) {
                    if (player.blockPosition().distToCenterSqr(getBlockPos().getCenter()) > 64 * 64) {
                        playerIdsToRemove.add(playerId);
                    }
                } else {
                    playerIdsToRemove.add(playerId);
                }
            } catch (Throwable t) {
                LOGGER.warn("Removing player because of error", t);
                playerIdsToRemove.add(playerId);
            }
        }
        playerIds.removeAll(playerIdsToRemove);

        if (progressBar != null) {
            for (UUID playerId : playerIdsToRemove) {
                try {
                    Player player = level.getPlayerByUUID(playerId);
                    if (player != null) {
                        progressBar.removePlayer((ServerPlayer) player);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Failed to remove player from progress bar because of error", t);
                }
            }
            for (UUID playerId : playerIds) {
                try {
                    Player player = level.getPlayerByUUID(playerId);
                    if (player != null) {
                        progressBar.addPlayer((ServerPlayer)player);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Unable to add player to progress bar", t);
                }
            }
        }
    }

    public void sentPlayersMessage(Component message) {
        if (!level.isClientSide()) {
            for (UUID playerId : playerIds) {
                try {
                    Player player = level.getPlayerByUUID(playerId);
                    player.displayClientMessage(message, true);
                } catch (Throwable t) {
                    LOGGER.warn("Unable to send message to player because of error", t);
                }
            }
        }
    }

    public void sentPlayersChat(Component message) {
        if (!level.isClientSide()) {
            for (UUID playerId : playerIds) {
                try {
                    Player player = level.getPlayerByUUID(playerId);
                    player.displayClientMessage(message, false);
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
                numItemsServedByPlayer
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

    public void scoreboardAddItemServed(UUID playerId) {
        numItemsServedByPlayer.put(playerId, numItemsServedByPlayer.getOrDefault(playerId, 0) + 1);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CustomerSpawnerBlockEntity entity) {
        if (!level.isClientSide()) {
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

            if (entity.ticksSinceUpdatePlayers == 0 || entity.ticksSinceUpdatePlayers > 20 * 5) {
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
                        entity.spawnCustomer();
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
                    // All customers still buying or arriving should give up
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



