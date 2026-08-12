package com.vikingkittens.mc.customers.customer.data;

import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;

public final class CustomerPaymentBoxBlockLootSubProvider extends BlockLootSubProvider {
    public CustomerPaymentBoxBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return CustomerPaymentBox.BLOCKS.values().stream().map(holder -> (Block) holder.get()).toList();
    }
}
