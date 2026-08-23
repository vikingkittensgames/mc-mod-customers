package com.vikingkittens.mc.customers.customer.data;

import java.util.Set;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;

public final class CustomerLeaderboardBlockLootSubProvider extends BlockLootSubProvider {
    public CustomerLeaderboardBlockLootSubProvider() {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(CustomerLeaderboard.BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(CustomerLeaderboard.BLOCK.get());
    }
}
