package com.vikingkittens.mc.customers.customer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.client.common.PlayerProfileUtils;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

public class CustomerLeaderboardBlockEntity extends BlockEntity {
    public static final String NAME = "customer_leaderboard_block_entity";

    private final CustomerLeaderboardScores scores = new CustomerLeaderboardScores();

    public CustomerLeaderboardBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    public boolean shouldConfirmBreak() {
        return !scores.isEmpty();
    }

    public void addScore(
            BlockPos spawnerPosition,
            CustomerSpawnerMode spawnerMode,
            int level,
            UUID playerId,
            float score
    ) {
        if (scores.add(new CustomerLeaderboardScores.Key(
                spawnerPosition,
                spawnerMode,
                level,
                playerId
        ), score)) {
            setChanged();
        }
    }

    public float getScore(
            BlockPos spawnerPosition,
            CustomerSpawnerMode spawnerMode,
            int level,
            UUID playerId
    ) {
        return scores.get(new CustomerLeaderboardScores.Key(
                spawnerPosition,
                spawnerMode,
                level,
                playerId
        ));
    }

    public static CustomerLeaderboardBlockEntity findClosest(
            Level level,
            BlockPos position,
            int maxDistance
    ) {
        return SearchUtils.findBlocksInBox(
                level,
                position,
                maxDistance * 2 + 1,
                        (ignored, state) -> state.is(CustomerLeaderboard.BLOCK.get())
                ).stream()
                .map(level::getBlockEntity)
                .filter(CustomerLeaderboardBlockEntity.class::isInstance)
                .map(CustomerLeaderboardBlockEntity.class::cast)
                .min(Comparator.comparingDouble(leaderboard -> leaderboard.worldPosition.distSqr(position)))
                .orElse(null);
    }

    public void open(ServerPlayer player) {
        /*
        if (scores.isEmpty()) {
            addFakeScores();
        }
        */
        Map<CustomerLeaderboardScores.Key, CustomerLeaderboardOpenPayload.Score> payloadScores = new HashMap<>();
        scores.scores().forEach((key, score) -> {
            boolean levelPassed = false;
            BlockEntity blockEntity = level.getBlockEntity(key.spawnerPosition());
            if (blockEntity instanceof CustomerSpawnerBlockEntity spawner) {
                levelPassed = score >= spawner.getLevelRequiredScore(key.level() - 1);
            } else if (key.playerId().getMostSignificantBits() == 0L
                    && key.playerId().getLeastSignificantBits() >= 0L
                    && key.playerId().getLeastSignificantBits() < 7L) {
                levelPassed = score >= 0.6F;
            }
            payloadScores.put(key, new CustomerLeaderboardOpenPayload.Score(score, levelPassed));
        });
        CustomersServices.network().sendToPlayer(player, new CustomerLeaderboardOpenPayload(worldPosition, payloadScores));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        scores.write(PersistenceCUtils.writer(tag));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        scores.read(PersistenceCUtils.reader(tag));
    }

    private void addFakeScores() {
        RandomSource random = RandomSource.create();
        addFakeScores(CustomerSpawnerMode.BREAKFAST, worldPosition.offset(-32, 0, 0), 3, random);
        addFakeScores(CustomerSpawnerMode.LUNCH, worldPosition.offset(-16, 0, 0), 2, random);
        addFakeScores(CustomerSpawnerMode.DINNER, worldPosition, 1, random);
        addFakeScores(CustomerSpawnerMode.DINNER, worldPosition.offset(16, 0, 0), 1, random);
        setChanged();
    }

    private void addFakeScores(
            CustomerSpawnerMode spawnerMode,
            BlockPos spawnerPosition,
            int levels,
            RandomSource random
    ) {
        for (int level = 1; level <= levels; level++) {
            Set<Integer> playerIndexes = new HashSet<>();
            int playerCount = random.nextIntBetweenInclusive(1, 4);
            while (playerIndexes.size() < playerCount) {
                playerIndexes.add(random.nextInt(PlayerProfileUtils.FakePlayers.size()));
            }
            for (int playerIndex : playerIndexes) {
                addScore(
                        spawnerPosition,
                        spawnerMode,
                        level,
                        PlayerProfileUtils.FakePlayers.getId(playerIndex),
                        random.nextFloat()
                );
            }
        }
    }
}
