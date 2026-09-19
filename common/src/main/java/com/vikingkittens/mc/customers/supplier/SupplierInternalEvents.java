package com.vikingkittens.mc.customers.supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.common.events.InternalEvent;

public final class SupplierInternalEvents {
    private SupplierInternalEvents() {}

    public static class SupplierEvent extends InternalEvent {
        private final ServerLevel level;
        private final BlockPos spawnerPosition;

        public SupplierEvent(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition
        ) {
            this.level = level;
            this.spawnerPosition = spawnerPosition == null ? null : spawnerPosition.immutable();
        }

        public ServerLevel level() { return level; }

        public @Nullable BlockPos spawnerPosition() { return spawnerPosition; }
    }

    public static final class SuppliesPurchased extends SupplierEvent {
        private final UUID playerId;
        private final ItemStack supplyItem;
        private final ItemStack costItem;

        public SuppliesPurchased(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                UUID playerId,
                ItemStack supplyItem,
                ItemStack costItem
        ) {
            super(level, spawnerPosition);
            this.playerId = playerId;
            this.supplyItem = supplyItem.copy();
            this.costItem = costItem.copy();
        }

        public @Nullable UUID playerId() { return playerId; }

        public ItemStack supplyItem() { return supplyItem.copy(); }
        public ItemStack costItem() { return costItem.copy(); }
    }

    public record Offer(ItemStack item, ItemStack cost) {}

    public static final class SupplierSpawnerConfigChanged extends SupplierInternalEvents.SupplierEvent {
        private final UUID playerId;
        private final List<Offer> offers;
        private final boolean autoCost;
        private final List<String> enabledAppearances;

        private final int numSellItems;
        private final int numCostItems;
        private final int numAppearances;

        public SupplierSpawnerConfigChanged(
                ServerLevel level,
                @Nullable BlockPos spawnerPosition,
                final UUID playerId,
                List<Offer> offers,
                boolean autoCost,
                List<String> enabledAppearances
        ) {
            super(level, spawnerPosition);
            this.playerId = playerId;
            this.offers = copyOffers(offers);
            this.autoCost = autoCost;
            this.enabledAppearances = List.copyOf(enabledAppearances);

            int numSellItems = 0;
            int numCostItems = 0;
            for (Offer offer : offers) {
                if (offer.item != null && !offer.item.isEmpty()) {
                    numSellItems++;
                    if (offer.cost != null && !offer.cost.isEmpty()) {
                        numCostItems++;
                    }
                }
            }
            this.numSellItems = numSellItems;
            this.numCostItems = numCostItems;

            this.numAppearances = (int)(enabledAppearances.stream()
                    .filter(appearanceId -> !appearanceId.isEmpty() && !appearanceId.equals("default"))
                    .count());
        }

        public UUID playerId() { return playerId; }

        public List<Offer> offers() { return copyOffers(offers); }
        public int numSellItems() { return numSellItems; }
        public int numCostItems() { return numCostItems; }

        public boolean autoCost() { return autoCost; }

        public List<String> enabledAppearances() { return enabledAppearances; }
        public int numAppearances() { return numAppearances; }

        public boolean hasSameConfiguration(@Nullable SupplierSpawnerConfigChanged other) {
            return other != null
                    && Objects.equals(spawnerPosition(), other.spawnerPosition())
                    && sameOffers(offers, other.offers)
                    && autoCost == other.autoCost
                    && enabledAppearances.equals(other.enabledAppearances);
        }

        private static List<Offer> copyOffers(List<Offer> offers) {
            List<Offer> result = new ArrayList<>(offers.size());
            for (Offer offer : offers) {
                result.add(new Offer(copyStack(offer.item()), copyStack(offer.cost())));
            }
            return List.copyOf(result);
        }

        private static boolean sameOffers(List<Offer> first, List<Offer> second) {
            if (first.size() != second.size()) {
                return false;
            }
            for (int index = 0; index < first.size(); index++) {
                Offer firstOffer = first.get(index);
                Offer secondOffer = second.get(index);
                if (!sameStack(firstOffer.item(), secondOffer.item())
                        || !sameStack(firstOffer.cost(), secondOffer.cost())) {
                    return false;
                }
            }
            return true;
        }

        private static ItemStack copyStack(@Nullable ItemStack stack) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }

        private static boolean sameStack(ItemStack first, ItemStack second) {
            return first.getCount() == second.getCount() && ItemStack.isSameItemSameComponents(first, second);
        }
    }
}
