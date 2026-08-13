package com.vikingkittens.mc.customers.customer.data;

import java.util.Set;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

public final class CustomerPickupCounterBlockLootSubProvider extends BlockLootSubProvider {
    public CustomerPickupCounterBlockLootSubProvider() {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return CustomerPickupCounter.BLOCKS.values().stream().map(holder -> (Block) holder.get()).toList();
    }
}
