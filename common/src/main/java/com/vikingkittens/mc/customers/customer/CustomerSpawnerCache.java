package com.vikingkittens.mc.customers.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.common.events.InternalEvents;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomerSpawnerCache {
    public record Key(
            Level level,
            BlockPos spawnerPosition
    ) {}

    public record Value(
            BlockPos spawnerPosition,
            BlockState counterBlockState
    ) {}

    private static final Map<Key, Value> CACHE = new HashMap<>();

    private CustomerSpawnerCache() {}

    public static void update(Level level, BlockPos spawnerPosition) {
        if (level.getBlockEntity(spawnerPosition) instanceof CustomerSpawnerBlockEntity) {
            update(level, spawnerPosition, level.getBlockState(spawnerPosition.above()));
        } else {
            remove(level, spawnerPosition);
        }
    }

    static void update(Level level, BlockPos spawnerPosition, BlockState counterBlockState) {
        BlockPos immutablePosition = spawnerPosition.immutable();
        CACHE.put(
                new Key(level, immutablePosition),
                new Value(immutablePosition, counterBlockState)
        );
    }

    public static void remove(Level level, BlockPos spawnerPosition) {
        CACHE.remove(new Key(level, spawnerPosition));
    }

    public static List<Value> getValuesNearPosition(Level level, BlockPos blockPosition) {
        return getValuesNearPosition(
                level,
                blockPosition,
                CustomersServices.config().maxCounterDistance()
        );
    }

    static List<Value> getValuesNearPosition(Level level, BlockPos blockPosition, int maxDistance) {
        double maxDistanceSquared = (double)maxDistance * maxDistance;
        return CACHE.entrySet().stream()
                .filter(entry -> entry.getKey().level() == level)
                .map(Map.Entry::getValue)
                .filter(value -> value.spawnerPosition().distSqr(blockPosition) <= maxDistanceSquared)
                .toList();
    }

    public static void onBlockPlaced(
            Level level,
            BlockPos blockPosition,
            BlockState blockState,
            Player player
    ) {
        onBlockPlaced(
                level,
                blockPosition,
                blockState,
                player,
                CustomersServices.config().maxCounterDistance(),
                InternalEvents::emit
        );
    }

    static void onBlockPlaced(
            Level level,
            BlockPos blockPosition,
            BlockState blockState,
            Player player,
            int maxDistance,
            Consumer<CustomerInternalEvents.CounterBlockPlaced> eventConsumer
    ) {
        if (blockState.getBlock() instanceof CustomerSpawnerBlock) {
            update(level, blockPosition);
        }

        for (Value value : getValuesNearPosition(level, blockPosition, maxDistance)) {
            if (blockPosition.equals(value.spawnerPosition().above())) {
                update(level, value.spawnerPosition(), blockState);
            } else if (player != null
                    && level instanceof ServerLevel serverLevel
                    && blockState.is(value.counterBlockState().getBlock())
                    && level.getBlockEntity(value.spawnerPosition()) instanceof CustomerSpawnerBlockEntity spawner) {
                eventConsumer.accept(new CustomerInternalEvents.CounterBlockPlaced(
                        serverLevel,
                        value.spawnerPosition(),
                        spawner.getSpawnerMode(),
                        player.getUUID(),
                        blockPosition,
                        blockState
                ));
            }
        }
    }

    public static void onBlockBroken(
            Level level,
            BlockPos blockPosition,
            BlockState blockState
    ) {
        onBlockBroken(
                level,
                blockPosition,
                blockState,
                CustomersServices.config().maxCounterDistance()
        );
    }

    static void onBlockBroken(
            Level level,
            BlockPos blockPosition,
            BlockState blockState,
            int maxDistance
    ) {
        if (blockState.getBlock() instanceof CustomerSpawnerBlock) {
            remove(level, blockPosition);
            return;
        }

        for (Value value : getValuesNearPosition(level, blockPosition, maxDistance)) {
            if (blockPosition.equals(value.spawnerPosition().above())) {
                update(level, value.spawnerPosition(), Blocks.AIR.defaultBlockState());
            }
        }
    }

    static void clear() {
        CACHE.clear();
    }
}
