package com.vikingkittens.mc.customers.customer;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

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

    public static final class ItemServed extends CustomerEvent {
        private final UUID playerId;
        private final UUID customerId;
        private final ResourceLocation customerProfession;
        private final ItemStack servedItem;
        private final ItemStack costItem;
        private final boolean isPetItem;

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
}
