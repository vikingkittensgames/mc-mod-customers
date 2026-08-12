package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.BlockPos;

public record CustomerCounterMarker(
        BlockPos position,
        CustomerSpawnerMode spawnerMode
) {
}
