package com.vikingkittens.mc.customers.customer.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CustomerLootTableProvider
        extends LootTableProvider {
    public CustomerLootTableProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(
                output,
                Set.of(),
                List.of(
                        new SubProviderEntry(
                                CustomerPickupCounterBlockLootSubProvider::new,
                                LootContextParamSets.BLOCK
                        ),
                        new SubProviderEntry(
                                CustomerPaymentBoxBlockLootSubProvider::new,
                                LootContextParamSets.BLOCK
                        ),
                        new SubProviderEntry(
                                CustomerLeaderboardBlockLootSubProvider::new,
                                LootContextParamSets.BLOCK
                        )
                ),
                registries
        );
    }
}
