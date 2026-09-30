package com.vikingkittens.mc.customers.customer;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class CustomerCounterCache {
    private Level level;
    private BlockPos spawnerPosition;
    private Block counterBlock;
    private int maxDistance;
    private Block spawnerBlock;
    private List<BlockPos> positions = List.of();
    private boolean initialized;

    public List<BlockPos> getPositions(
            Level level,
            BlockPos spawnerPosition,
            BlockState counterState,
            int maxDistance,
            Block spawnerBlock
    ) {
        Block counterBlock = counterState.getBlock();
        if (!initialized
                || this.level != level
                || !spawnerPosition.equals(this.spawnerPosition)
                || this.counterBlock != counterBlock
                || this.maxDistance != maxDistance
                || this.spawnerBlock != spawnerBlock) {
            this.level = level;
            this.spawnerPosition = spawnerPosition.immutable();
            this.counterBlock = counterBlock;
            this.maxDistance = maxDistance;
            this.spawnerBlock = spawnerBlock;
            positions = counterState.isAir()
                    ? List.of()
                    : List.copyOf(CustomerCounter.findCounterPositions(
                            level,
                            spawnerPosition,
                            counterState,
                            maxDistance,
                            () -> spawnerBlock
                    ));
            initialized = true;
        }
        return positions;
    }

    public void invalidate() {
        initialized = false;
    }
}
