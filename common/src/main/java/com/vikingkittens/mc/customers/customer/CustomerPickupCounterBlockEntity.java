package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.vikingkittens.mc.customers.common.OfferUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.ItemInsertionTarget;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.PlayerCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

public class CustomerPickupCounterBlockEntity extends BlockEntity {
    enum CustomerScope {
        ALL,
        COUNTER_BLOCK
    }

    static CustomerScope customerScope(@Nullable UUID crafterId) {
        return crafterId == null
                ? CustomerScope.COUNTER_BLOCK
                : CustomerScope.ALL;
    }

    public static final String NAME = "customer_pickup_counter";
    public static final int INVENTORY_SIZE = 9;
    private static final int CUSTOMER_SPAWNER_SEARCH_SIZE = 64;
    private static final long CUSTOMER_SPAWNER_CACHE_TICKS = 20L;
    private static final long ITEM_COLLECTION_INTERVAL_TICKS = 8L;
    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_STACK_METADATA = "stackMetadata";
    private static final String TAG_CRAFTER_ID = "crafterId";
    private static final Direction[] CONNECTED_DIRECTIONS = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST
    };

    private final PersistedContainer inventory = new PersistedContainer(INVENTORY_SIZE, this::inventoryChanged);
    private final UUID[] crafterIds = new UUID[INVENTORY_SIZE];
    private final ItemInsertionTarget itemInsertionTarget = new InsertionTarget(this);
    private long customerSpawnerCacheGameTime = Long.MIN_VALUE;
    private List<CustomerSpawnerBlockEntity> cachedAllCustomerSpawners = List.of();
    private List<CustomerSpawnerBlockEntity> cachedCounterCustomerSpawners = List.of();
    private long lastItemCollectionGameTime = Long.MIN_VALUE;

    static final class InsertionTarget implements ItemInsertionTarget {
        private final CustomerPickupCounterBlockEntity counter;

        InsertionTarget(CustomerPickupCounterBlockEntity counter) {
            this.counter = counter;
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return simulate
                    ? counter.previewCraftedStackConnected(null, stack)
                    : counter.insertCraftedStackConnectedByOwner(
                            null,
                            stack
                    );
        }

        @Override
        public boolean accepts(ItemStack stack) {
            return !stack.isEmpty();
        }
    }

    public record StoredStack(
            ItemStack stack,
            @Nullable UUID crafterId
    ) {
        public StoredStack {
            stack = stack.copy();
        }

        public StoredStack(
                ItemStack stack,
                boolean ignoredAssigned,
                @Nullable UUID crafterId
        ) {
            this(stack, crafterId);
        }
    }

    record CustomerOffer(
            CustomerSpawnerBlockEntity spawner,
            MerchantOffer offer
    ) {
    }

    record IncomingAllocation(
            int acceptedCount,
            Map<CustomerSpawnerBlockEntity, Integer>
                    acceptedBySpawner
    ) {
        IncomingAllocation {
            acceptedBySpawner = Map.copyOf(acceptedBySpawner);
        }
    }

    static IncomingAllocation allocateIncoming(
            List<CustomerOffer> customerOffers,
            List<ItemStack> existingStacks,
            ItemStack incomingStack
    ) {
        List<MerchantOffer> offers = customerOffers.stream()
                .map(CustomerOffer::offer)
                .toList();
        OfferUtils.Allocation existingAllocation =
                OfferUtils.allocateDetailed(offers, existingStacks);
        List<ItemStack> stacksWithIncoming =
                new ArrayList<>(existingStacks);
        stacksWithIncoming.add(incomingStack);
        OfferUtils.Allocation combinedAllocation =
                OfferUtils.allocateDetailed(
                        offers,
                        stacksWithIncoming
                );
        int acceptedCount = combinedAllocation.stackCounts().get(
                combinedAllocation.stackCounts().size() - 1
        );
        Map<CustomerSpawnerBlockEntity, Integer>
                acceptedBySpawner = new LinkedHashMap<>();
        for (int index = 0; index < customerOffers.size(); index++) {
            int acceptedForOffer =
                    combinedAllocation.offerCounts().get(index)
                            - existingAllocation.offerCounts().get(index);
            if (acceptedForOffer > 0) {
                acceptedBySpawner.merge(
                        customerOffers.get(index).spawner(),
                        acceptedForOffer,
                        Integer::sum
                );
            }
        }
        return new IncomingAllocation(
                acceptedCount,
                acceptedBySpawner
        );
    }

    static ItemStack insertLiveDemandStack(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners,
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        List<ItemStack> existingStacks = counters.stream()
                .flatMap(counter -> counter.getDisplayItems().stream())
                .toList();
        IncomingAllocation allocation = allocateIncoming(
                findCustomerOffers(spawners),
                existingStacks,
                stack
        );
        if (allocation.acceptedCount() == 0) {
            return stack.copy();
        }

        ItemStack acceptedStack = stack.copy();
        acceptedStack.setCount(allocation.acceptedCount());
        List<StoredStack> storedStacks = List.of(
                new StoredStack(
                        acceptedStack,
                        true,
                        crafterId
                )
        );
        if (!hasCapacity(counters, storedStacks)) {
            return stack.copy();
        }
        if (!insertStoredStacksConnected(counters, storedStacks)) {
            throw new IllegalStateException(
                    "Previewed pickup counter capacity was unavailable"
            );
        }
        allocation.acceptedBySpawner().forEach(
                (spawner, count) ->
                        spawner.scoreboardAddItemsCrafted(
                                crafterId,
                                count
                        )
        );

        ItemStack remainder = stack.copy();
        remainder.shrink(allocation.acceptedCount());
        return remainder;
    }

    static ItemStack previewLiveDemandStack(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners,
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        List<ItemStack> existingStacks = counters.stream()
                .flatMap(counter -> counter.getDisplayItems().stream())
                .toList();
        IncomingAllocation allocation = allocateIncoming(
                findCustomerOffers(spawners),
                existingStacks,
                stack
        );
        if (allocation.acceptedCount() == 0) {
            return stack.copy();
        }

        ItemStack acceptedStack = stack.copy();
        acceptedStack.setCount(allocation.acceptedCount());
        List<StoredStack> storedStacks = List.of(
                new StoredStack(
                        acceptedStack,
                        true,
                        crafterId
                )
        );
        if (!hasCapacity(counters, storedStacks)) {
            return stack.copy();
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(allocation.acceptedCount());
        return remainder;
    }

    static List<StoredStack> splitByAssignment(
            ItemStack offered,
            @Nullable ItemStack remainder,
            @Nullable UUID crafterId
    ) {
        int remainderCount =
                remainder == null ? 0 : remainder.getCount();
        int assignedCount = offered.getCount() - remainderCount;
        List<StoredStack> stacks = new ArrayList<>(2);
        if (assignedCount > 0) {
            ItemStack assigned = offered.copy();
            assigned.setCount(assignedCount);
            stacks.add(new StoredStack(
                    assigned,
                    true,
                    crafterId
            ));
        }

        return List.copyOf(stacks);
    }

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
        tag.put(TAG_INVENTORY, inventory.serializeNBT(registries));
        writeStackMetadata(
                PersistenceCUtils.writer(tag),
                inventory,
                crafterIds
        );
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        if (tag.contains(TAG_INVENTORY)) {
            inventory.deserializeNBT(
                    registries,
                    tag.getCompound(TAG_INVENTORY)
            );
        }
        readStackMetadata(
                PersistenceCUtils.reader(tag),
                inventory,
                crafterIds
        );
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
        return insertStoredStack(
                new StoredStack(stack, false, null)
        );
    }

    /**
     * Inserts a stack while preserving its assignment and crafting-player
     * metadata.
     *
     * @param storedStack stack and metadata to insert
     * @return the portion that did not fit
     */
    public ItemStack insertStoredStack(StoredStack storedStack) {
        if (storedStack.stack().isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = storedStack.stack().copy();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (canMerge(slot, storedStack)) {
                remainder = inventory.insertItem(
                        slot,
                        remainder,
                        false
                );
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                crafterIds[slot] = storedStack.crafterId();
                int countBefore = remainder.getCount();
                remainder = inventory.insertItem(
                        slot,
                        remainder,
                        false
                );
                if (remainder.getCount() == countBefore) {
                    crafterIds[slot] = null;
                }
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remainder;
    }

    private boolean canMerge(
            int slot,
            StoredStack storedStack
    ) {
        ItemStack existing = inventory.getItem(slot);
        return !existing.isEmpty()
                && Objects.equals(
                        crafterIds[slot],
                        storedStack.crafterId()
                )
                && ItemStackCUtils.isSameItemAndTags(
                        existing,
                        storedStack.stack()
                );
    }

    public ItemStack insertStackConnected(ItemStack stack) {
        return level == null
                ? insertStack(stack)
                : insertStackConnected(level, worldPosition, stack);
    }

    /**
     * Returns the number of empty FIFO slots in this counter.
     *
     * @return available stack slots
     */
    public int getFreeSlotCount() {
        int freeSlots = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                freeSlots++;
            }
        }
        return freeSlots;
    }

    /**
     * Inserts all stored stacks into this connected counter network only when
     * the complete operation fits.
     *
     * @param stacks stacks and metadata to insert
     * @return true when every stack was inserted
     */
    public boolean insertStoredStacksConnected(
            List<StoredStack> stacks
    ) {
        if (level == null) {
            return insertStoredStacksConnected(
                    List.of(this),
                    stacks
            );
        }
        return insertStoredStacksConnected(
                getConnectedCounters(level, worldPosition),
                stacks
        );
    }

    /**
     * Finds every pickup counter connected horizontally to the starting
     * position.
     *
     * @param level counter level
     * @param pos starting counter position
     * @return counters in traversal order
     */
    static List<CustomerPickupCounterBlockEntity> getConnectedCounters(
            Level level,
            BlockPos pos
    ) {
        List<CustomerPickupCounterBlockEntity> counters =
                new ArrayList<>();
        collectConnectedCounters(
                level,
                pos,
                new HashSet<>(),
                counters
        );
        return List.copyOf(counters);
    }

    /**
     * Traverses horizontally connected pickup counters without revisiting a
     * position.
     *
     * @param level counter level
     * @param pos position to inspect
     * @param visited positions already inspected
     * @param counters connected counters found so far
     */
    private static void collectConnectedCounters(
            Level level,
            BlockPos pos,
            Set<BlockPos> visited,
            List<CustomerPickupCounterBlockEntity> counters
    ) {
        if (!visited.add(pos)
                || !(level.getBlockEntity(pos)
                        instanceof CustomerPickupCounterBlockEntity counter)) {
            return;
        }
        counters.add(counter);
        for (Direction direction : CONNECTED_DIRECTIONS) {
            collectConnectedCounters(
                    level,
                    pos.relative(direction),
                    visited,
                    counters
            );
        }
    }

    /**
     * Atomically inserts stored stacks across a known counter network.
     *
     * @param counters connected counters in insertion order
     * @param stacks stacks and metadata to insert
     * @return true when every stack was inserted
     */
    static boolean insertStoredStacksConnected(
            List<CustomerPickupCounterBlockEntity> counters,
            List<StoredStack> stacks
    ) {
        if (!hasCapacity(counters, stacks)) {
            return false;
        }

        for (StoredStack storedStack : stacks) {
            ItemStack remainder = storedStack.stack();
            for (CustomerPickupCounterBlockEntity counter : counters) {
                if (remainder.isEmpty()) {
                    break;
                }
                remainder = counter.insertStoredStack(
                        new StoredStack(
                                remainder,
                                storedStack.crafterId()
                        )
                );
            }
            if (!remainder.isEmpty()) {
                throw new IllegalStateException(
                        "Reserved pickup counter capacity was unavailable"
                );
            }
        }
        return true;
    }

    /**
     * Checks whether a connected counter network can hold every supplied
     * stored stack.
     *
     * @param counters connected counters
     * @param stacks stacks and metadata being considered
     * @return true when the complete operation fits
     */
    static boolean hasCapacity(
            List<CustomerPickupCounterBlockEntity> counters,
            List<StoredStack> stacks
    ) {
        return hasCapacity(counters, stacks, 0);
    }

    /**
     * Checks capacity while accounting for slots that will be freed before
     * insertion.
     *
     * @param counters connected counters
     * @param stacks stacks and metadata being considered
     * @param additionalFreeSlots slots released by the transaction
     * @return true when the complete operation fits
     */
    static boolean hasCapacity(
            List<CustomerPickupCounterBlockEntity> counters,
            List<StoredStack> stacks,
            int additionalFreeSlots
    ) {
        List<StoredStack> simulated = new ArrayList<>();
        int totalSlots = additionalFreeSlots;
        for (CustomerPickupCounterBlockEntity counter : counters) {
            totalSlots += counter.inventory.getContainerSize();
            for (int slot = 0;
                    slot < counter.inventory.getContainerSize();
                    slot++) {
                ItemStack stack =
                        counter.inventory.getItem(slot);
                if (!stack.isEmpty()) {
                    simulated.add(new StoredStack(
                            stack,
                            true,
                            counter.crafterIds[slot]
                    ));
                }
            }
        }

        for (StoredStack incoming : stacks) {
            int remaining = incoming.stack().getCount();
            for (int index = 0;
                    index < simulated.size() && remaining > 0;
                    index++) {
                StoredStack existing = simulated.get(index);
                if (!canMerge(existing, incoming)) {
                    continue;
                }
                int available = existing.stack().getMaxStackSize()
                        - existing.stack().getCount();
                int inserted = Math.min(available, remaining);
                if (inserted > 0) {
                    ItemStack merged = existing.stack().copy();
                    merged.grow(inserted);
                    simulated.set(index, new StoredStack(
                            merged,
                            existing.crafterId()
                    ));
                    remaining -= inserted;
                }
            }
            while (remaining > 0 && simulated.size() < totalSlots) {
                int inserted = Math.min(
                        incoming.stack().getMaxStackSize(),
                        remaining
                );
                ItemStack insertedStack = incoming.stack().copy();
                insertedStack.setCount(inserted);
                simulated.add(new StoredStack(
                        insertedStack,
                        incoming.crafterId()
                ));
                remaining -= inserted;
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean canMerge(
            StoredStack first,
            StoredStack second
    ) {
        return Objects.equals(
                        first.crafterId(),
                        second.crafterId()
                )
                && ItemStackCUtils.isSameItemAndTags(
                        first.stack(),
                        second.stack()
                );
    }

    static List<StoredStack> previewCraftedStacks(
            List<CustomerSpawnerBlockEntity> spawners,
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        int assignableCount =
                getAssignableCraftedItemCount(spawners, stack);
        ItemStack remainder = stack.copy();
        remainder.shrink(assignableCount);
        return splitByAssignment(
                stack,
                remainder.isEmpty() ? null : remainder,
                crafterId
        );
    }

    static ItemStack insertCraftedStack(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners,
            Player player,
            ItemStack stack
    ) {
        return insertCraftedStack(
                counters,
                spawners,
                player.getUUID(),
                stack
        );
    }

    static ItemStack insertCraftedStack(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners,
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        List<StoredStack> previewStacks =
                previewCraftedStacks(spawners, crafterId, stack);
        if (previewStacks.isEmpty()) {
            return stack.copy();
        }
        if (!hasCapacity(counters, previewStacks)) {
            return stack.copy();
        }

        ItemStack remainder = stack;
        for (CustomerSpawnerBlockEntity spawner : spawners) {
            remainder = spawner.tryAssignCraftedItem(
                    crafterId,
                    remainder
            );
            if (remainder == null) {
                break;
            }
        }
        List<StoredStack> storedStacks = splitByAssignment(
                stack,
                remainder,
                crafterId
        );
        if (!insertStoredStacksConnected(counters, storedStacks)) {
            throw new IllegalStateException(
                    "Previewed pickup counter capacity was unavailable"
            );
        }
        return remainder == null
                ? ItemStack.EMPTY
                : remainder.copy();
    }

    static ItemStack previewCraftedStack(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners,
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        List<StoredStack> previewStacks =
                previewCraftedStacks(spawners, crafterId, stack);
        if (previewStacks.isEmpty()
                || !hasCapacity(counters, previewStacks)) {
            return stack.copy();
        }
        int acceptedCount = previewStacks.stream()
                .mapToInt(stored -> stored.stack().getCount())
                .sum();
        ItemStack remainder = stack.copy();
        remainder.shrink(acceptedCount);
        return remainder;
    }

    /**
     * Previews assignable demand across nearby spawners without mutating them.
     *
     * @param spawners nearby customer spawners
     * @param stack crafted items being considered
     * @return total number of assignable items
     */
    static int getAssignableCraftedItemCount(
            List<CustomerSpawnerBlockEntity> spawners,
            ItemStack stack
    ) {
        int assignableCount = 0;
        for (CustomerSpawnerBlockEntity spawner : spawners) {
            int remainingCount = stack.getCount() - assignableCount;
            if (remainingCount == 0) {
                break;
            }
            ItemStack remaining = stack.copy();
            remaining.setCount(remainingCount);
            assignableCount +=
                    spawner.getAssignableCraftedItemCount(remaining);
        }
        return assignableCount;
    }
    /**
     * Assigns and inserts a crafted stack using connected counters and customer
     * spawners within the configured 64-block cube.
     *
     * @param player player depositing the crafted stack
     * @param stack crafted stack being deposited
     * @return an empty stack on success or the unchanged remainder on failure
     */
    public ItemStack insertCraftedStackConnected(
            Player player,
            ItemStack stack
    ) {
        List<CustomerPickupCounterBlockEntity> counters =
                level == null
                        ? List.of(this)
                        : getConnectedCounters(level, worldPosition);
        List<CustomerSpawnerBlockEntity> spawners =
                level == null
                        ? List.of()
                        : getCustomerSpawners(
                                level,
                                worldPosition,
                                CustomerScope.ALL
                        );
        return insertLiveDemandStack(
                counters,
                spawners,
                player.getUUID(),
                stack
        );
    }

    public ItemStack insertCraftedStackConnectedByOwner(
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        List<CustomerPickupCounterBlockEntity> counters =
                level == null
                        ? List.of(this)
                        : getConnectedCounters(level, worldPosition);
        List<CustomerSpawnerBlockEntity> spawners =
                level == null
                        ? List.of()
                        : getCustomerSpawners(
                                level,
                                worldPosition,
                                customerScope(crafterId)
                        );
        return insertLiveDemandStack(
                counters,
                spawners,
                crafterId,
                stack
        );
    }

    public ItemStack previewCraftedStackConnected(
            @Nullable UUID crafterId,
            ItemStack stack
    ) {
        List<CustomerPickupCounterBlockEntity> counters =
                level == null
                        ? List.of(this)
                        : getConnectedCounters(level, worldPosition);
        List<CustomerSpawnerBlockEntity> spawners =
                level == null
                        ? List.of()
                        : getCustomerSpawners(
                                level,
                                worldPosition,
                                customerScope(crafterId)
                        );
        return previewLiveDemandStack(
                counters,
                spawners,
                crafterId,
                stack
        );
    }

    public ItemInsertionTarget getItemInsertionTarget() {
        return itemInsertionTarget;
    }

    public boolean hasAssignableCraftedItemConnected(ItemStack stack) {
        if (level == null) {
            return false;
        }
        List<CustomerPickupCounterBlockEntity> counters =
                getConnectedCounters(level, worldPosition);
        List<CustomerSpawnerBlockEntity> spawners =
                getCustomerSpawners(
                        level,
                        worldPosition,
                        CustomerScope.ALL
                );
        return previewLiveDemandStack(
                counters,
                spawners,
                null,
                stack
        ).getCount() < stack.getCount();
    }
    /**
     * Finds spawners or their customers inside a 64 by 64 by 64 cube centered
     * on the pickup counter.
     *
     * @param level counter level
     * @param pos counter position
     * @return distinct customer spawner block entities
     */
    static List<CustomerSpawnerBlockEntity> findCustomerSpawners(
            Level level,
            BlockPos pos
    ) {
        List<BlockPos> spawnerPositions = SearchUtils.findBlocksInBox(
                level,
                pos,
                CUSTOMER_SPAWNER_SEARCH_SIZE,
                (candidatePos, state) ->
                        state.is(
                                CustomerSpawner
                                        .CUSTOMER_SPAWNER_BLOCK
                                        .get()
                        )
        );
        List<CustomerVillagerEntity> customers =
                SearchUtils.findEntitiesInBox(
                        level,
                        CustomerVillagerEntity.class,
                        pos,
                        CUSTOMER_SPAWNER_SEARCH_SIZE,
                        customer -> customer.getSpawnerPos() != null
                );
        return findCustomerSpawners(
                level,
                spawnerPositions,
                customers
        );
    }

    static List<CustomerSpawnerBlockEntity> findCustomerSpawners(
            Level level,
            BlockPos pos,
            CustomerScope scope
    ) {
        return filterCustomerSpawners(
                level,
                findCustomerSpawners(level, pos),
                level.getBlockState(pos).getBlock(),
                scope
        );
    }

    private List<CustomerSpawnerBlockEntity> getCustomerSpawners(
            Level level,
            BlockPos pos,
            CustomerScope scope
    ) {
        long gameTime = level.getGameTime();
        if (customerSpawnerCacheGameTime == Long.MIN_VALUE
                || gameTime - customerSpawnerCacheGameTime
                        >= CUSTOMER_SPAWNER_CACHE_TICKS) {
            customerSpawnerCacheGameTime = gameTime;
            cachedAllCustomerSpawners = findCustomerSpawners(level, pos);
            cachedCounterCustomerSpawners = filterCustomerSpawners(
                    level,
                    cachedAllCustomerSpawners,
                    level.getBlockState(pos).getBlock(),
                    CustomerScope.COUNTER_BLOCK
            );
        }
        return scope == CustomerScope.ALL
                ? cachedAllCustomerSpawners
                : cachedCounterCustomerSpawners;
    }

    static List<CustomerSpawnerBlockEntity> findCustomerSpawners(
            Level level,
            List<BlockPos> nearbySpawnerPositions,
            List<CustomerVillagerEntity> nearbyCustomers
    ) {
        Set<BlockPos> spawnerPositions =
                new LinkedHashSet<>(nearbySpawnerPositions);
        nearbyCustomers.stream()
                .map(CustomerVillagerEntity::getSpawnerPos)
                .filter(java.util.Objects::nonNull)
                .forEach(spawnerPositions::add);

        List<CustomerSpawnerBlockEntity> spawners =
                new ArrayList<>(spawnerPositions.size());
        for (BlockPos spawnerPos : spawnerPositions) {
            if (level.getBlockEntity(spawnerPos)
                    instanceof CustomerSpawnerBlockEntity spawner) {
                spawners.add(spawner);
            }
        }
        return List.copyOf(spawners);
    }

    static List<CustomerSpawnerBlockEntity> filterCustomerSpawners(
            Level level,
            List<CustomerSpawnerBlockEntity> spawners,
            Block counterBlock,
            CustomerScope scope
    ) {
        if (scope == CustomerScope.ALL) {
            return List.copyOf(spawners);
        }
        return spawners.stream()
                .filter(spawner -> {
                    BlockState state = level.getBlockState(
                            spawner.getBlockPos().above()
                    );
                    return state != null
                            && state.getBlock() == counterBlock;
                })
                .toList();
    }

    static List<CustomerVillagerEntity> findActiveCustomers(
            List<CustomerSpawnerBlockEntity> spawners
    ) {
        Set<CustomerVillagerEntity> customers =
                new LinkedHashSet<>();
        for (CustomerSpawnerBlockEntity spawner : spawners) {
            customers.addAll(spawner.getActiveCustomers());
        }
        return List.copyOf(customers);
    }

    static List<CustomerOffer> findCustomerOffers(
            List<CustomerSpawnerBlockEntity> spawners
    ) {
        List<CustomerOffer> offers = new ArrayList<>();
        for (CustomerSpawnerBlockEntity spawner : spawners) {
            for (CustomerVillagerEntity customer
                    : spawner.getActiveCustomers()) {
                for (MerchantOffer offer : customer.getOffers()) {
                    offers.add(new CustomerOffer(spawner, offer));
                }
            }
        }
        return List.copyOf(offers);
    }

    public ItemStack removeOldest() {
        return removeOldestStored().stack();
    }

    /**
     * Removes the oldest stack together with its assignment metadata.
     *
     * @return oldest stored stack, or an empty stack record
     */
    public StoredStack removeOldestStored() {
        return removeStoredStack(0);
    }
    /**
     * Takes one complete requested stack from this connected counter network.
     *
     * @param requested item and count required by the customer
     * @return the requested stack and its crafting player, or an empty record
     */
    public StoredStack takeMatchingStoredStack(ItemStack requested) {
        List<CustomerPickupCounterBlockEntity> counters =
                level == null
                        ? List.of(this)
                        : getConnectedCounters(level, worldPosition);
        return takeMatchingStoredStack(counters, requested);
    }
    /**
     * Takes one complete offer from this connected counter network.
     *
     * @param offer customer offer defining the accepted item and count
     * @return the requested stack and its crafting player, or an empty record
     */
    public StoredStack takeMatchingStoredStack(MerchantOffer offer) {
        List<CustomerPickupCounterBlockEntity> counters =
                level == null
                        ? List.of(this)
                        : getConnectedCounters(level, worldPosition);
        return takeMatchingStoredStack(counters, offer);
    }

    public List<StoredStack> takeMatchingStoredStacks(
            MerchantOffer offer
    ) {
        List<CustomerPickupCounterBlockEntity> counters =
                level == null
                        ? List.of(this)
                        : getConnectedCounters(level, worldPosition);
        return takeMatchingStoredStacks(counters, offer);
    }

    static List<StoredStack> takeMatchingStoredStacks(
            List<CustomerPickupCounterBlockEntity> counters,
            MerchantOffer offer
    ) {
        ItemStack requested = offer.getCostA();
        int available = 0;
        for (CustomerPickupCounterBlockEntity counter : counters) {
            for (int slot = 0;
                    slot < counter.inventory.getContainerSize();
                    slot++) {
                ItemStack stored =
                        counter.inventory.getItem(slot);
                if (ItemStackCUtils.matchesCost(offer.getItemCostA(), stored)) {
                    available += stored.getCount();
                }
            }
        }
        if (available < requested.getCount()) {
            return List.of();
        }

        int remaining = requested.getCount();
        List<StoredStack> takenStacks = new ArrayList<>();
        for (CustomerPickupCounterBlockEntity counter : counters) {
            int slot = 0;
            while (slot < counter.inventory.getContainerSize()
                    && remaining > 0) {
                ItemStack stored =
                        counter.inventory.getItem(slot);
                if (!ItemStackCUtils.matchesCost(offer.getItemCostA(), stored)) {
                    slot++;
                    continue;
                }

                int takenCount = Math.min(
                        remaining,
                        stored.getCount()
                );
                ItemStack taken = stored.copy();
                taken.setCount(takenCount);
                UUID crafterId = counter.crafterIds[slot];
                takenStacks.add(new StoredStack(
                        taken,
                        true,
                        crafterId
                ));
                remaining -= takenCount;

                if (takenCount == stored.getCount()) {
                    counter.removeStoredStack(slot);
                } else {
                    ItemStack storedRemainder = stored.copy();
                    storedRemainder.shrink(takenCount);
                    counter.inventory.setItem(
                            slot,
                            storedRemainder
                    );
                    slot++;
                }
            }
            if (remaining == 0) {
                break;
            }
        }
        return List.copyOf(takenStacks);
    }

    /**
     * Takes one complete requested stack from the supplied counters.
     *
     * @param counters counters to search in traversal order
     * @param requested item and count required by the customer
     * @return the requested stack and its crafting player, or an empty record
     */
    static StoredStack takeMatchingStoredStack(
            List<CustomerPickupCounterBlockEntity> counters,
            ItemStack requested
    ) {
        return takeMatchingStoredStack(
                counters,
                requested,
                stored -> ItemStackCUtils.isSameItemAndTags(
                        stored,
                        requested
                )
        );
    }

    /**
     * Takes one complete offer from the supplied counters.
     *
     * @param counters counters to search in traversal order
     * @param offer customer offer defining the accepted item and count
     * @return the requested stack and its crafting player, or an empty record
     */
    static StoredStack takeMatchingStoredStack(
            List<CustomerPickupCounterBlockEntity> counters,
            MerchantOffer offer
    ) {
        return takeMatchingStoredStack(
                counters,
                offer.getCostA(),
                stored -> ItemStackCUtils.matchesCost(offer.getItemCostA(), stored)
        );
    }

    /**
     * Takes a complete requested quantity using the supplied item matcher.
     *
     * @param counters counters to search in traversal order
     * @param requested item and count required by the customer
     * @param matchesOffer matcher for the offer's item requirements
     * @return the requested stack and its crafting player, or an empty record
     */
    private static StoredStack takeMatchingStoredStack(
            List<CustomerPickupCounterBlockEntity> counters,
            ItemStack requested,
            Predicate<ItemStack> matchesOffer
    ) {
        if (requested.isEmpty()) {
            return new StoredStack(ItemStack.EMPTY, false, null);
        }
        for (CustomerPickupCounterBlockEntity counter : counters) {
            for (int slot = 0; slot < counter.inventory.getContainerSize(); slot++) {
                ItemStack stored = counter.inventory.getItem(slot);
                UUID crafterId = counter.crafterIds[slot];
                if (stored.getCount() < requested.getCount()
                        || !matchesOffer.test(stored)) {
                    continue;
                }

                ItemStack taken = stored.copy();
                taken.setCount(requested.getCount());
                if (stored.getCount() == requested.getCount()) {
                    counter.removeStoredStack(slot);
                } else {
                    ItemStack remaining = stored.copy();
                    remaining.shrink(requested.getCount());
                    counter.inventory.setItem(slot, remaining);
                }
                return new StoredStack(taken, true, crafterId);
            }
        }
        return new StoredStack(ItemStack.EMPTY, false, null);
    }

    /**
     * Removes a specific FIFO slot and shifts every later item and its metadata
     * toward the front.
     *
     * @param removedSlot slot to remove
     * @return removed stack and metadata, or an empty stack record
     */
    StoredStack removeStoredStack(int removedSlot) {
        if (removedSlot < 0
                || removedSlot >= inventory.getContainerSize()
                || inventory.getItem(removedSlot).isEmpty()) {
            return new StoredStack(ItemStack.EMPTY, false, null);
        }
        StoredStack removed = new StoredStack(
                inventory.getItem(removedSlot),
                true,
                crafterIds[removedSlot]
        );
        for (int slot = removedSlot + 1;
                slot < inventory.getContainerSize();
                slot++) {
            inventory.setItem(
                    slot - 1,
                    inventory.getItem(slot).copy()
            );
            crafterIds[slot - 1] = crafterIds[slot];
        }
        int lastSlot = inventory.getContainerSize() - 1;
        inventory.setItem(lastSlot, ItemStack.EMPTY);
        crafterIds[lastSlot] = null;
        return removed;
    }

    private record StoredLocation(
            CustomerPickupCounterBlockEntity counter,
            int slot,
            StoredStack stored
    ) {
    }

    static List<StoredStack> revalidateStoredStacks(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners
    ) {
        List<StoredLocation> locations = new ArrayList<>();
        for (CustomerPickupCounterBlockEntity counter : counters) {
            for (int slot = 0;
                    slot < counter.inventory.getContainerSize();
                    slot++) {
                ItemStack stack =
                        counter.inventory.getItem(slot);
                if (!stack.isEmpty()) {
                    locations.add(new StoredLocation(
                            counter,
                            slot,
                            new StoredStack(
                                    stack,
                                    true,
                                    counter.crafterIds[slot]
                            )
                    ));
                }
            }
        }

        List<Integer> allocatedCounts = OfferUtils.allocate(
                findCustomerOffers(spawners).stream()
                        .map(CustomerOffer::offer)
                        .toList(),
                locations.stream()
                        .map(location -> location.stored().stack())
                        .toList()
        );
        List<StoredStack> returnedStacks = new ArrayList<>();
        for (int index = 0; index < locations.size(); index++) {
            StoredStack stored = locations.get(index).stored();
            int returnedCount =
                    stored.stack().getCount()
                            - allocatedCounts.get(index);
            if (returnedCount > 0) {
                ItemStack returned = stored.stack().copy();
                returned.setCount(returnedCount);
                returnedStacks.add(new StoredStack(
                        returned,
                        false,
                        stored.crafterId()
                ));
            }
        }

        for (int index = locations.size() - 1;
                index >= 0;
                index--) {
            StoredLocation location = locations.get(index);
            int allocatedCount = allocatedCounts.get(index);
            if (allocatedCount == 0) {
                location.counter().removeStoredStack(location.slot());
                continue;
            }
            if (allocatedCount
                    < location.stored().stack().getCount()) {
                ItemStack retained =
                        location.stored().stack().copy();
                retained.setCount(allocatedCount);
                location.counter().inventory.setItem(
                        location.slot(),
                        retained
                );
            }
        }
        return List.copyOf(returnedStacks);
    }
    static boolean shouldRevalidate(long gameTime) {
        return gameTime % 20L == 0L;
    }

    static boolean isRevalidationLeader(
            List<CustomerPickupCounterBlockEntity> counters,
            BlockPos pos
    ) {
        for (CustomerPickupCounterBlockEntity counter : counters) {
            if (counter.getBlockPos().compareTo(pos) < 0) {
                return false;
            }
        }
        return true;
    }

    static ItemStack returnToPlayer(
            @Nullable Player player,
            ItemStack stack
    ) {
        ItemStack remainder = stack.copy();
        if (player == null) {
            return remainder;
        }
        player.getInventory().add(remainder);
        if (!remainder.isEmpty()) {
            PlayerCUtils.sendActionBarMessage(
                    player,
                    Component.translatable(
                            "messages.customers.pickup_counter.items_returned"
                    )
            );
        }
        return remainder;
    }

    static Vec3 getReturnedItemDropPosition(BlockPos pos) {
        return Vec3.atCenterOf(pos.above());
    }

    static @Nullable UUID getDroppingPlayerId(
            @Nullable Entity owner
    ) {
        return owner instanceof Player player
                ? player.getUUID()
                : null;
    }

    static void acceptDroppedItem(
            CustomerPickupCounterBlockEntity counter,
            ItemEntity droppedItem
    ) {
        ItemStack offered = droppedItem.getItem();
        if (offered.isEmpty()) {
            return;
        }
        ItemStack remainder =
                counter.insertCraftedStackConnectedByOwner(
                        getDroppingPlayerId(droppedItem.getOwner()),
                        offered
                );
        if (remainder.isEmpty()) {
            droppedItem.discard();
        } else if (remainder.getCount() < offered.getCount()) {
            droppedItem.setItem(remainder);
        }
    }

    private static void acceptDroppedItems(
            Level level,
            BlockPos pos,
            CustomerPickupCounterBlockEntity counter
    ) {
        AABB collectionArea = new AABB(pos)
                .expandTowards(0.0D, 0.5D, 0.0D);
        for (ItemEntity droppedItem : level.getEntitiesOfClass(
                ItemEntity.class,
                collectionArea,
                item -> item.isAlive() && !item.getItem().isEmpty()
        )) {
            acceptDroppedItem(counter, droppedItem);
        }
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            CustomerPickupCounterBlockEntity counter
    ) {
        if (LevelCUtils.isClientSide(level)) {
            return;
        }
        if (shouldCollectItems(
                level.getGameTime(),
                counter.lastItemCollectionGameTime
        )) {
            counter.lastItemCollectionGameTime = level.getGameTime();
            acceptDroppedItems(level, pos, counter);
        }
        if (!shouldRevalidate(level.getGameTime())) {
            return;
        }
        List<CustomerPickupCounterBlockEntity> counters =
                getConnectedCounters(level, pos);
        if (!isRevalidationLeader(counters, pos)
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<CustomerSpawnerBlockEntity> spawners =
                counter.getCustomerSpawners(level, pos, CustomerScope.ALL);
        List<StoredStack> returnedStacks =
                revalidateStoredStacks(counters, spawners);
        for (StoredStack returned : returnedStacks) {
            Player player = returned.crafterId() == null
                    ? null
                    : serverLevel.getServer()
                            .getPlayerList()
                            .getPlayer(returned.crafterId());
            ItemStack remainder =
                    returnToPlayer(player, returned.stack());
            if (!remainder.isEmpty()) {
                Vec3 dropPosition =
                        getReturnedItemDropPosition(pos);
                ItemEntity droppedItem = new ItemEntity(
                        level,
                        dropPosition.x,
                        dropPosition.y,
                        dropPosition.z,
                        remainder
                );
                droppedItem.setDefaultPickUpDelay();
                level.addFreshEntity(droppedItem);
            }
        }
    }

    static boolean shouldCollectItems(
            long gameTime,
            long lastCollectionGameTime
    ) {
        return lastCollectionGameTime == Long.MIN_VALUE
                || gameTime - lastCollectionGameTime
                        >= ITEM_COLLECTION_INTERVAL_TICKS;
    }
    public ItemStack removeOldestConnected() {
        return level == null
                ? removeOldest()
                : removeOldestConnected(level, worldPosition);
    }

    public List<ItemStack> getDisplayItems() {
        return getDisplayItems(inventory);
    }

    /**
     * Writes metadata in the same FIFO order as the nonempty inventory slots.
     *
     * @param output persistence destination
     * @param inventory inventory whose entries are described
     * @param crafterIds crafting-player IDs by slot
     */
    static void writeStackMetadata(
            DataWriter output,
            Container inventory,
            UUID[] crafterIds
    ) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                DataWriter metadata =
                        output.addChild(TAG_STACK_METADATA);
                if (crafterIds[slot] != null) {
                    metadata.putUuid(
                            TAG_CRAFTER_ID,
                            crafterIds[slot]
                    );
                }
            }
        }
    }

    /**
     * Restores metadata by FIFO position, defaulting old saves to stacks
     * without a known crafter.
     *
     * @param input persistence source
     * @param inventory restored inventory
     * @param crafterIds crafting-player ID destination
     */
    static void readStackMetadata(
            DataReader input,
            Container inventory,
            UUID[] crafterIds
    ) {
        java.util.Arrays.fill(crafterIds, null);
        List<DataReader> metadata =
                input.getChildren(TAG_STACK_METADATA);
        int metadataIndex = 0;
        for (int slot = 0;
                slot < inventory.getContainerSize()
                        && metadataIndex < metadata.size();
                slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                DataReader storedMetadata =
                        metadata.get(metadataIndex++);
                crafterIds[slot] =
                        storedMetadata.getUuid(TAG_CRAFTER_ID)
                                .orElse(null);
            }
        }
    }

    static ItemStack insertStack(
            PersistedContainer inventory,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                return inventory.insertItem(slot, stack.copy(), false);
            }
        }
        return stack.copy();
    }

    static ItemStack removeOldest(Container inventory) {
        if (inventory.getItem(0).isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = inventory.getItem(0).copy();
        for (int slot = 1; slot < inventory.getContainerSize(); slot++) {
            inventory.setItem(
                    slot - 1,
                    inventory.getItem(slot).copy()
            );
        }
        inventory.setItem(
                inventory.getContainerSize() - 1,
                ItemStack.EMPTY
        );
        return removed;
    }

    static List<ItemStack> getDisplayItems(Container inventory) {
        List<ItemStack> items = new ArrayList<>(inventory.getContainerSize());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
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
