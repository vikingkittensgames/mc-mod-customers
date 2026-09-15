package com.vikingkittens.mc.customers.customer;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.common.events.InternalEvent;

public final class CustomerInternalEvents {
    private CustomerInternalEvents() {}

    public static final class ItemServed extends InternalEvent {
        private final ServerLevel level;
        private final BlockPos spawnerPosition;
        private final CustomerSpawnerMode spawnerMode;
        private final UUID playerId;
        private final UUID customerId;
        private final ResourceLocation customerProfession;
        private final ItemStack servedItem;
        private final ItemStack costItem;

        public ItemServed(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                @Nullable CustomerSpawnerMode spawnerMode,
                @Nullable UUID playerId,
                UUID customerId,
                ResourceLocation customerProfession,
                ItemStack servedItem,
                ItemStack costItem
        ) {
            this.level = level;
            this.spawnerPosition = spawnerPosition == null ? null : spawnerPosition.immutable();
            this.spawnerMode = spawnerMode;
            this.playerId = playerId;
            this.customerId = customerId;
            this.customerProfession = customerProfession;
            this.servedItem = servedItem.copy();
            this.costItem = costItem.copy();
        }

        public ServerLevel level() { return level; }

        public @Nullable BlockPos spawnerPosition() { return spawnerPosition; }

        public @Nullable CustomerSpawnerMode spawnerMode() { return spawnerMode; }

        public @Nullable UUID playerId() { return playerId; }

        public UUID customerId() { return customerId; }

        public ResourceLocation customerProfession() { return customerProfession; }

        public ItemStack servedItem() { return servedItem.copy(); }

        public ItemStack costItem() { return costItem.copy(); }
    }
}
