package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
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
    private static final int SPAWN_CHECK_MAX_TICKS = 4;
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
                    new ItemStack(Items.EMERALD, rowCost.get(row) * count),
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

    private boolean ticksDisabled = false;
    private long ticksSinceTicksDisabledCheck = 0;

    private boolean needsUpdate = true;
    private long updateDelayTicks = 0;
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_ROW_SIZE * 6) {
        @Override
        protected void onContentsChanged(int slot) {
            LOGGER.debug("Inventory changed");
            setChanged();
        }
    };
    private long spawnCheckTicks = 0;
    private final Set<UUID> customerIds = new HashSet<>();
    private ServerBossEvent progressBar;
    private final Set<UUID> playerIds = new HashSet<>();
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
                customerIds.clear();
                ListTag customersTag = tag.getList("customers", Tag.TAG_INT_ARRAY);
                for (int i = 0; i < customersTag.size(); i++) {
                    try {
                        customerIds.add(NbtUtils.loadUUID(customersTag.get(i)));
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
            boolean spawnSpecial = specialEnabled && level.getRandom().nextIntBetweenInclusive(0, 100) < 25;

            CustomerVillagerEntity customer = CustomerVillagerEntity.spawn(
                    level,
                    getBlockPos(),
                    offers,
                    counterBlockState,
                    avoidBlockState
            );
            if (customer != null) {
                LOGGER.debug("Customer UUID={}, pos={}", customer.getUUID(), customer.blockPosition());
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
                Entity entity = ((ServerLevel) level).getEntity(customerId);
                if (entity instanceof CustomerVillagerEntity customer) {
                    if (!customer.isAlive() || customer.isRemoved()) {
                        idsToRemove.add(customerId);
                    }
                } else {
                    idsToRemove.add(customerId);
                }
            } catch (Throwable t) {
                LOGGER.warn("Removing customer from tracking because of error", t);
                idsToRemove.add(customerId);
            }
        }
        customerIds.removeAll(idsToRemove);
    }

    public void addPlayer(UUID playerId) {
        if (!playerIds.contains(playerId)) {
            LOGGER.debug("Added player {}", playerId);
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
        if (!playerIdsToRemove.isEmpty()) {
            LOGGER.debug("Removed players: {}", playerIdsToRemove);
        }

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
                    player.sendSystemMessage(message);
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

    private void scoreboardShow() {
    }

    private void scoreboardShowFinal() {
        CustomerSpawnerMode spawnerMode = getBlockState().getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
        int color = 0x36991C;
        if (scoreboardGetPercentage() <= 0.25) {
            color = 0xFF0000;
        } else if (scoreboardGetPercentage() <= 0.50) {
            color = 0xFE8B00;
        }

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

            entity.updateSpawned();

            if (entity.ticksSinceUpdatePlayers == 0 || entity.ticksSinceUpdatePlayers > 20 * 5) {
                entity.ticksSinceUpdatePlayers = 0;
                entity.updatePlayers();
            }
            entity.ticksSinceUpdatePlayers++;

            if (entity.spawnCheckTicks > SPAWN_CHECK_MAX_TICKS) {
                CustomerSpawnerMode spawnerMode = state.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
                long timeOfDay = (level.getDayTime() + 6000L) % 24000L;
                boolean shouldSpawn = CustomerSpawnerMode.shouldSpawn(spawnerMode, timeOfDay);
                if (!state.getValue(CustomerSpawnerBlock.STATE_DISABLED) && shouldSpawn) {
                    if (entity.customerIds.size() < MAX_CUSTOMERS) {
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
                        entity.progressBar.setProgress(1.0F - CustomerSpawnerMode.generateProgress(spawnerMode, timeOfDay));
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


