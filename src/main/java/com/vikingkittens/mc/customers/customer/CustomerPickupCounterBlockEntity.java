package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.ItemStackHandler;

import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.PlayerCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

public class CustomerPickupCounterBlockEntity extends BlockEntity {
    public static final String NAME = "customer_pickup_counter";
    public static final int INVENTORY_SIZE = 9;
    private static final int CUSTOMER_SPAWNER_SEARCH_SIZE = 64;
    private static final String TAG_INVENTORY = "inventory";
    private static final String TAG_STACK_METADATA = "stackMetadata";
    private static final String TAG_ASSIGNED = "assigned";
    private static final String TAG_CRAFTER_ID = "crafterId";
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
    private final boolean[] assignedSlots =
            new boolean[INVENTORY_SIZE];
    private final UUID[] crafterIds = new UUID[INVENTORY_SIZE];

    /**
     * Associates a pickup-counter item stack with its assignment and original
     * crafting-player information.
     *
     * @param stack stored item stack
     * @param assigned whether customer demand has been assigned to the stack
     * @param crafterId original crafting player, or null for legacy items
     */
    public record StoredStack(
            ItemStack stack,
            boolean assigned,
            @Nullable UUID crafterId
    ) {
        public StoredStack {
            stack = stack.copy();
        }
    }

    static List<StoredStack> splitByAssignment(
            ItemStack offered,
            @Nullable ItemStack remainder,
            UUID crafterId
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child(TAG_INVENTORY));
        writeStackMetadata(
                PersistenceCUtils.writer(output),
                inventory,
                assignedSlots,
                crafterIds
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input.childOrEmpty(TAG_INVENTORY));
        readStackMetadata(
                PersistenceCUtils.reader(input),
                inventory,
                assignedSlots,
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
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                assignedSlots[slot] = storedStack.assigned();
                crafterIds[slot] = storedStack.crafterId();
                ItemStack remainder = inventory.insertItem(
                        slot,
                        storedStack.stack(),
                        false
                );
                if (remainder.getCount()
                        == storedStack.stack().getCount()) {
                    assignedSlots[slot] = false;
                    crafterIds[slot] = null;
                }
                return remainder;
            }
        }
        return storedStack.stack().copy();
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
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
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
                                storedStack.assigned(),
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
        int requiredSlots = stacks.stream()
                .mapToInt(storedStack -> {
                    int maximum = storedStack.stack()
                            .getMaxStackSize();
                    return (storedStack.stack().getCount()
                            + maximum - 1) / maximum;
                })
                .sum();
        int freeSlots = counters.stream()
                .mapToInt(CustomerPickupCounterBlockEntity
                        ::getFreeSlotCount)
                .sum()
                + additionalFreeSlots;
        return freeSlots >= requiredSlots;
    }

    static List<StoredStack> previewCraftedStacks(
            List<CustomerSpawnerBlockEntity> spawners,
            UUID crafterId,
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
            UUID crafterId,
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
                        : findCustomerSpawners(level, worldPosition);
        return insertCraftedStack(
                counters,
                spawners,
                player.getUUID(),
                stack
        );
    }

    public boolean hasAssignableCraftedItemConnected(ItemStack stack) {
        if (level == null) {
            return false;
        }
        return getAssignableCraftedItemCount(
                findCustomerSpawners(level, worldPosition),
                stack
        ) > 0;
    }
    /**
     * Finds customer spawner block entities inside a 64 by 64 by 64 cube
     * centered on the pickup counter.
     *
     * @param level counter level
     * @param pos counter position
     * @return nearby customer spawner block entities
     */
    static List<CustomerSpawnerBlockEntity> findCustomerSpawners(
            Level level,
            BlockPos pos
    ) {
        List<BlockPos> positions = SearchUtils.findBlocksInBox(
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
        List<CustomerSpawnerBlockEntity> spawners =
                new ArrayList<>(positions.size());
        for (BlockPos spawnerPos : positions) {
            if (level.getBlockEntity(spawnerPos)
                    instanceof CustomerSpawnerBlockEntity spawner) {
                spawners.add(spawner);
            }
        }
        return List.copyOf(spawners);
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
                stored -> ItemStack.isSameItemSameComponents(
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
                offer.getItemCostA()::test
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
            for (int slot = 0; slot < counter.inventory.getSlots(); slot++) {
                ItemStack stored = counter.inventory.getStackInSlot(slot);
                UUID crafterId = counter.crafterIds[slot];
                if (!counter.assignedSlots[slot]
                        || crafterId == null
                        || stored.getCount() < requested.getCount()
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
                    counter.inventory.setStackInSlot(slot, remaining);
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
                || removedSlot >= inventory.getSlots()
                || inventory.getStackInSlot(removedSlot).isEmpty()) {
            return new StoredStack(ItemStack.EMPTY, false, null);
        }
        StoredStack removed = new StoredStack(
                inventory.getStackInSlot(removedSlot),
                assignedSlots[removedSlot],
                crafterIds[removedSlot]
        );
        for (int slot = removedSlot + 1;
                slot < inventory.getSlots();
                slot++) {
            inventory.setStackInSlot(
                    slot - 1,
                    inventory.getStackInSlot(slot).copy()
            );
            assignedSlots[slot - 1] = assignedSlots[slot];
            crafterIds[slot - 1] = crafterIds[slot];
        }
        int lastSlot = inventory.getSlots() - 1;
        inventory.setStackInSlot(lastSlot, ItemStack.EMPTY);
        assignedSlots[lastSlot] = false;
        crafterIds[lastSlot] = null;
        return removed;
    }

    static List<StoredStack> revalidateStoredStacks(
            List<CustomerPickupCounterBlockEntity> counters,
            List<CustomerSpawnerBlockEntity> spawners
    ) {
        List<StoredStack> returnedStacks = new ArrayList<>();
        for (CustomerPickupCounterBlockEntity counter : counters) {
            int slot = 0;
            while (slot < counter.inventory.getSlots()) {
                ItemStack stored =
                        counter.inventory.getStackInSlot(slot);
                if (stored.isEmpty()) {
                    slot++;
                    continue;
                }

                UUID crafterId = counter.crafterIds[slot];
                if (crafterId == null) {
                    StoredStack removed =
                            counter.removeStoredStack(slot);
                    returnedStacks.add(new StoredStack(
                            removed.stack(),
                            false,
                            null
                    ));
                    continue;
                }

                ItemStack offered = stored.copy();
                if (counter.assignedSlots[slot]) {
                    int releasedCount = 0;
                    for (CustomerSpawnerBlockEntity spawner : spawners) {
                        int remainingCount =
                                offered.getCount() - releasedCount;
                        if (remainingCount == 0) {
                            break;
                        }
                        ItemStack remaining = offered.copy();
                        remaining.setCount(remainingCount);
                        releasedCount +=
                                spawner.releaseCraftedItemAssignment(
                                        remaining
                                );
                    }
                }

                ItemStack remainder = offered;
                for (CustomerSpawnerBlockEntity spawner : spawners) {
                    remainder = spawner.tryReserveCraftedItem(remainder);
                    if (remainder == null) {
                        break;
                    }
                }

                int remainderCount =
                        remainder == null ? 0 : remainder.getCount();
                int reservedCount =
                        offered.getCount() - remainderCount;
                if (reservedCount == 0) {
                    counter.removeStoredStack(slot);
                    returnedStacks.add(new StoredStack(
                            offered,
                            false,
                            crafterId
                    ));
                    continue;
                }

                if (remainderCount > 0) {
                    ItemStack reserved = offered.copy();
                    reserved.setCount(reservedCount);
                    counter.inventory.setStackInSlot(slot, reserved);
                    returnedStacks.add(new StoredStack(
                            remainder,
                            false,
                            crafterId
                    ));
                }
                counter.assignedSlots[slot] = true;
                slot++;
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
    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            CustomerPickupCounterBlockEntity counter
    ) {
        if (LevelCUtils.isClientSide(level)
                || !shouldRevalidate(level.getGameTime())) {
            return;
        }
        List<CustomerPickupCounterBlockEntity> counters =
                getConnectedCounters(level, pos);
        if (!isRevalidationLeader(counters, pos)
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<CustomerSpawnerBlockEntity> spawners =
                findCustomerSpawners(level, pos);
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
     * @param assignedSlots assignment state by slot
     * @param crafterIds crafting-player IDs by slot
     */
    static void writeStackMetadata(
            DataWriter output,
            ItemStackHandler inventory,
            boolean[] assignedSlots,
            UUID[] crafterIds
    ) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                DataWriter metadata =
                        output.addChild(TAG_STACK_METADATA);
                metadata.putBoolean(
                        TAG_ASSIGNED,
                        assignedSlots[slot]
                );
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
     * Restores metadata by FIFO position, defaulting old saves to unassigned
     * stacks without a known crafter.
     *
     * @param input persistence source
     * @param inventory restored inventory
     * @param assignedSlots assignment state destination
     * @param crafterIds crafting-player ID destination
     */
    static void readStackMetadata(
            DataReader input,
            ItemStackHandler inventory,
            boolean[] assignedSlots,
            UUID[] crafterIds
    ) {
        Arrays.fill(assignedSlots, false);
        Arrays.fill(crafterIds, null);
        List<DataReader> metadata =
                input.getChildren(TAG_STACK_METADATA);
        int metadataIndex = 0;
        for (int slot = 0;
                slot < inventory.getSlots()
                        && metadataIndex < metadata.size();
                slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                DataReader storedMetadata =
                        metadata.get(metadataIndex++);
                assignedSlots[slot] =
                        storedMetadata.getBoolean(TAG_ASSIGNED);
                crafterIds[slot] =
                        storedMetadata.getUuid(TAG_CRAFTER_ID)
                                .orElse(null);
            }
        }
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
