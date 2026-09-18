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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.common.events.InternalEvent;

public final class CustomerInternalEvents {
    private CustomerInternalEvents() {}

    public static class CustomerEvent extends InternalEvent {
        private final ServerLevel level;
        private final BlockPos spawnerPosition;
        private final CustomerSpawnerMode spawnerMode;

        public CustomerEvent(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode
        ) {
            this.level = level;
            this.spawnerPosition = spawnerPosition == null ? null : spawnerPosition.immutable();
            this.spawnerMode = spawnerMode;
        }

        public ServerLevel level() { return level; }

        public @Nullable BlockPos spawnerPosition() { return spawnerPosition; }

        public @Nullable CustomerSpawnerMode spawnerMode() { return spawnerMode; }
    }

    public abstract static class ServedEvent extends CustomerEvent {
        private final UUID playerId;
        private final UUID customerId;
        private final ResourceLocation customerProfession;
        private final ItemStack servedItem;
        private final ItemStack costItem;
        private final boolean isPetItem;

        protected ServedEvent(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                @Nullable UUID playerId,
                UUID customerId,
                ResourceLocation customerProfession,
                ItemStack servedItem,
                ItemStack costItem,
                boolean isPetItem
        ) {
            super(level, spawnerPosition, spawnerMode);
            this.playerId = playerId;
            this.customerId = customerId;
            this.customerProfession = customerProfession;
            this.servedItem = servedItem.copy();
            this.costItem = costItem.copy();
            this.isPetItem = isPetItem;
        }

        public @Nullable UUID playerId() { return playerId; }

        public UUID customerId() { return customerId; }

        public ResourceLocation customerProfession() { return customerProfession; }

        public ItemStack servedItem() { return servedItem.copy(); }
        public ItemStack costItem() { return costItem.copy(); }

        public boolean isPetItem() { return isPetItem; }
    }

    public static final class ItemServed extends ServedEvent {
        public ItemServed(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                @Nullable UUID playerId,
                UUID customerId,
                ResourceLocation customerProfession,
                ItemStack servedItem,
                ItemStack costItem,
                boolean isPetItem
        ) {
            super(
                    level,
                    spawnerPosition,
                    spawnerMode,
                    playerId,
                    customerId,
                    customerProfession,
                    servedItem,
                    costItem,
                    isPetItem
            );
        }
    }

    public static final class CustomerServed extends ServedEvent {
        public CustomerServed(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                @Nullable UUID playerId,
                UUID customerId,
                ResourceLocation customerProfession,
                ItemStack servedItem,
                ItemStack costItem,
                boolean isPetItem
        ) {
            super(
                    level,
                    spawnerPosition,
                    spawnerMode,
                    playerId,
                    customerId,
                    customerProfession,
                    servedItem,
                    costItem,
                    isPetItem
            );
        }
    }

    public static final class ShiftFinished extends CustomerEvent {
        private final int activeLevel;
        private final float percentage;
        private final int totalCustomers;
        private final int numCustomersServed;
        private final int numCustomersGaveUp;
        private final int totalItemsWanted;
        private final Map<UUID, Integer> playerItemsCrafted;
        private final Map<UUID, Integer> playerItemsServed;
        private final int automatedItemsCrafted;
        private final int automatedItemsServed;

        public ShiftFinished(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                int activeLevel,
                float percentage,
                int totalCustomers,
                int numCustomersServed,
                int numCustomersGaveUp,
                int totalItemsWanted,
                Map<UUID, Integer> playerItemsCrafted,
                Map<UUID, Integer> playerItemsServed,
                int automatedItemsCrafted,
                int automatedItemsServed
        ) {
            super(level, spawnerPosition, spawnerMode);
            this.activeLevel = activeLevel;
            this.percentage = percentage;
            this.totalCustomers = totalCustomers;
            this.numCustomersServed = numCustomersServed;
            this.numCustomersGaveUp = numCustomersGaveUp;
            this.totalItemsWanted = totalItemsWanted;
            this.playerItemsCrafted = playerItemsCrafted;
            this.playerItemsServed = playerItemsServed;
            this.automatedItemsCrafted = automatedItemsCrafted;
            this.automatedItemsServed = automatedItemsServed;
        }

        public int activeLevel() { return activeLevel; }

        public float percentage() { return percentage; }

        public int totalCustomers() { return totalCustomers; }
        public int numCustomersServed() { return numCustomersServed; }
        public int numCustomersGaveUp() { return numCustomersGaveUp; }

        public int totalItemsWanted() { return totalItemsWanted; }
        public Map<UUID, Integer> playerItemsCrafted() { return playerItemsCrafted; }
        public Map<UUID, Integer> playerItemsServed() { return playerItemsServed; }

        public int automatedItemsCrafted() { return automatedItemsCrafted; }
        public int automatedItemsServed() { return automatedItemsServed; }
    }

    public static final class LeaderboardChanged extends CustomerEvent {
        private final BlockPos leaderboardPosition;
        private final int activeLevel;
        private final Map<UUID, Float> scores;
        private final Map<UUID, Float> previousScores;

        private final UUID leader;
        private final UUID previousLeader;
        private final boolean leaderChanged;

        public LeaderboardChanged(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                BlockPos leaderboardPosition,
                int activeLevel,
                Map<UUID, Float> scores,
                @Nullable Map<UUID, Float> previousScores
        ) {
            super(level, spawnerPosition, spawnerMode);
            this.leaderboardPosition = leaderboardPosition.immutable();
            this.activeLevel = activeLevel;
            this.scores = scores == null ? Map.of() : Map.copyOf(scores);
            this.previousScores = previousScores == null ? Map.of() : Map.copyOf(previousScores);

            if (this.scores.size() > 1) {
                this.leader = this.scores.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
            } else {
                this.leader = null;
            }
            if (this.previousScores.size() > 1) {
                this.previousLeader = this.previousScores.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
            } else {
                this.previousLeader = null;
            }
            if (this.leader != null && (this.previousLeader == null || !this.leader.equals(this.previousLeader))) {
                this.leaderChanged = true;
            } else {
                this.leaderChanged = false;
            }
        }

        public BlockPos leaderboardPosition() { return leaderboardPosition; }

        public int activeLevel() { return activeLevel; }

        public Map<UUID, Float> scores() { return scores; }
        public Map<UUID, Float> previousScores() { return previousScores; }

        public Set<UUID> changedPlayerIds() {
            Set<UUID> changedPlayerIds = new HashSet<>(scores.keySet());
            changedPlayerIds.addAll(previousScores.keySet());
            changedPlayerIds.removeIf(playerId -> Objects.equals(scores.get(playerId), previousScores.get(playerId)));
            return Set.copyOf(changedPlayerIds);
        }

        public Set<UUID> affectedPlayerIds() {
            Set<UUID> affectedPlayerIds = new HashSet<>(changedPlayerIds());
            if (leader != null) {
                affectedPlayerIds.add(leader);
            }
            if (previousLeader != null) {
                affectedPlayerIds.add(previousLeader);
            }
            return Set.copyOf(affectedPlayerIds);
        }

        public UUID leader() { return leader; }
        public UUID previousLeader() { return previousLeader; }
        public boolean leaderChanged() { return leaderChanged; }
    }

    public static final class CustomerSpawnerConfigChanged extends CustomerEvent {
        private final UUID playerId;
        private final int activeLevel;
        private final List<List<ItemStack>> rowSellItems;
        private final List<ItemStack> rowCostItems;
        private final float requiredStars;
        private final int maxCustomers;
        private final float petPercentage;
        private final boolean petTypesCustomized;
        private final LinkedHashSet<String> enabledPetTypes;
        private final LinkedHashMap<String, ItemStack> petFoods;
        private final boolean autoCost;
        private final List<String> enabledAppearances;

        private final int numSellItems;
        private final int numCostItems;
        private final int numAppearances;

        public CustomerSpawnerConfigChanged(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                final UUID playerId,
                int activeLevel,
                List<List<ItemStack>> rowSellItems,
                List<ItemStack> rowCostItems,
                float requiredStars,
                int maxCustomers,
                float petPercentage,
                boolean petTypesCustomized,
                LinkedHashSet<String> enabledPetTypes,
                LinkedHashMap<String, ItemStack> petFoods,
                boolean autoCost,
                List<String> enabledAppearances
        ) {
            super(level, spawnerPosition, spawnerMode);
            this.playerId = playerId;
            this.activeLevel = activeLevel;
            this.rowSellItems = copyRows(rowSellItems);
            this.rowCostItems = copyStacks(rowCostItems);
            this.requiredStars = requiredStars;
            this.maxCustomers = maxCustomers;
            this.petPercentage = petPercentage;
            this.petTypesCustomized = petTypesCustomized;
            this.enabledPetTypes = new LinkedHashSet<>(enabledPetTypes);
            this.petFoods = copyStacks(petFoods);
            this.autoCost = autoCost;
            this.enabledAppearances = List.copyOf(enabledAppearances);

            int numSellItems = 0;
            for (List<ItemStack> row : this.rowSellItems) {
                for (ItemStack item : row) {
                    if (item != null && !item.isEmpty()) {
                        numSellItems++;
                    }
                }
            }
            this.numSellItems = numSellItems;

            int numCostItems = 0;
            for (ItemStack item : this.rowCostItems) {
                if (item != null && !item.isEmpty()) {
                    numCostItems++;
                }
            }
            this.numCostItems = numCostItems;

            this.numAppearances = (int)(enabledAppearances.stream()
                    .filter(appearanceId -> !appearanceId.isEmpty() && !appearanceId.equals("default"))
                    .count());
        }

        public UUID playerId() { return playerId; }

        public int activeLevel() { return activeLevel; }

        public List<List<ItemStack>> rowSellItems() { return copyRows(rowSellItems); }
        public List<ItemStack> rowCostItems() { return copyStacks(rowCostItems); }
        public int numSellItems() { return numSellItems; }
        public int numCostItems() { return numCostItems; }

        public float requiredStars() { return requiredStars; }

        public int maxCustomers() { return maxCustomers; }

        public float petPercentage() { return petPercentage; }
        public boolean petTypesCustomized() { return petTypesCustomized; }
        public LinkedHashSet<String> enabledPetTypes() { return new LinkedHashSet<>(enabledPetTypes); }
        public LinkedHashMap<String, ItemStack> petFoods() { return copyStacks(petFoods); }

        public boolean autoCost() { return autoCost; }

        public List<String> enabledAppearances() { return enabledAppearances; }
        public int numAppearances() { return numAppearances; }

        public boolean hasSameConfiguration(@Nullable CustomerSpawnerConfigChanged other) {
            return other != null
                    && Objects.equals(spawnerPosition(), other.spawnerPosition())
                    && spawnerMode() == other.spawnerMode()
                    && activeLevel == other.activeLevel
                    && sameRows(rowSellItems, other.rowSellItems)
                    && sameStacks(rowCostItems, other.rowCostItems)
                    && Float.compare(requiredStars, other.requiredStars) == 0
                    && maxCustomers == other.maxCustomers
                    && Float.compare(petPercentage, other.petPercentage) == 0
                    && petTypesCustomized == other.petTypesCustomized
                    && enabledPetTypes.equals(other.enabledPetTypes)
                    && sameStacks(petFoods, other.petFoods)
                    && autoCost == other.autoCost
                    && enabledAppearances.equals(other.enabledAppearances);
        }

        private static List<List<ItemStack>> copyRows(List<List<ItemStack>> rows) {
            List<List<ItemStack>> result = new ArrayList<>(rows.size());
            for (List<ItemStack> row : rows) {
                result.add(copyStacks(row));
            }
            return List.copyOf(result);
        }

        private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
            return stacks.stream().map(CustomerSpawnerConfigChanged::copyStack).toList();
        }

        private static LinkedHashMap<String, ItemStack> copyStacks(Map<String, ItemStack> stacks) {
            LinkedHashMap<String, ItemStack> result = new LinkedHashMap<>();
            stacks.forEach((key, value) -> result.put(key, copyStack(value)));
            return result;
        }

        private static ItemStack copyStack(@Nullable ItemStack stack) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }

        private static boolean sameRows(List<List<ItemStack>> first, List<List<ItemStack>> second) {
            if (first.size() != second.size()) {
                return false;
            }
            for (int index = 0; index < first.size(); index++) {
                if (!sameStacks(first.get(index), second.get(index))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean sameStacks(List<ItemStack> first, List<ItemStack> second) {
            if (first.size() != second.size()) {
                return false;
            }
            for (int index = 0; index < first.size(); index++) {
                if (!sameStack(first.get(index), second.get(index))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean sameStacks(Map<String, ItemStack> first, Map<String, ItemStack> second) {
            if (!first.keySet().equals(second.keySet())) {
                return false;
            }
            return first.entrySet().stream().allMatch(entry -> sameStack(entry.getValue(), second.get(entry.getKey())));
        }

        private static boolean sameStack(ItemStack first, ItemStack second) {
            return first.getCount() == second.getCount() && ItemStack.isSameItemSameComponents(first, second);
        }
    }

    public static final class CounterBlockPlaced extends CustomerEvent {
        private final UUID playerId;
        private final BlockPos counterBlockPosition;
        private final BlockState counterBlockState;

        public CounterBlockPlaced(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                final UUID playerId,
                BlockPos counterBlockPosition,
                BlockState counterBlockState
        ) {
            super(level, spawnerPosition, spawnerMode);
            this.playerId = playerId;
            this.counterBlockPosition = counterBlockPosition.immutable();
            this.counterBlockState = counterBlockState;
        }

        public UUID playerId() { return playerId; }

        public BlockPos counterBlockPosition() { return counterBlockPosition; }
        public BlockState counterBlockState() { return counterBlockState; }
    }
}
