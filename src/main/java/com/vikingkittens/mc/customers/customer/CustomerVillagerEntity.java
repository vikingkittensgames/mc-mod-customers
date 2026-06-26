package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.customer.ai.CustomerLeaveGoal;
import com.vikingkittens.mc.customers.customer.ai.CustomerMoveToCounterGoal;
import com.vikingkittens.mc.customers.customer.ai.CustomerMoveToSpawnGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class CustomerVillagerEntity extends Villager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "customer_villager";

    private static VillagerType getVillagerTypeForLocation(Level level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        BiomeVillagerType mapData = biomeHolder.getData(NeoForgeDataMaps.VILLAGER_TYPES);
        if (mapData != null) {
            return mapData.type();
        }
        return VillagerType.PLAINS;
    }

    public static CustomerVillagerEntity spawn(
            Level level,
            BlockPos pos,
            MerchantOffers offers,
            BlockState counterBlockState
    ) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel)level;
            CustomerVillagerEntity customer = Customer.CUSTOMER_VILLAGER.get().create(level);
            if (customer != null) {
                // Find a random, safe position to spawn the customer around the passed in position
                BlockPos.MutableBlockPos safePos = new BlockPos.MutableBlockPos();
                boolean foundSafePos = false;
                int radius = 5;
                for (int attempt = 0; attempt < 10; attempt++) {
                    int randomX = pos.getX() + level.getRandom().nextIntBetweenInclusive(-radius, radius);
                    int randomZ = pos.getZ() + level.getRandom().nextIntBetweenInclusive(-radius, radius);
                    int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomX, randomZ);
                    safePos.set(randomX, groundY, randomZ);
                    int airCount = 0;
                    for (int yOffset = 0; yOffset <= 2; yOffset++) {
                        BlockPos checkPos = safePos.above(yOffset);
                        if (level.getBlockState(checkPos).isAir()) {
                            airCount++;
                        }
                    }
                    LOGGER.debug("Finding Safe Pos: attempt={}, target={}, pos={}, airCount={}", attempt, pos, safePos, airCount);
                    if (airCount >= 3) {
                        foundSafePos = true;
                        break;
                    }
                }
                if (foundSafePos) {
                    customer.moveTo(safePos, 0, 0);

                    VillagerData data = customer.getVillagerData();
                    customer.setVillagerData(new VillagerData(
                            getVillagerTypeForLocation(level, pos),
                            Customer.CUSTOMER_PROFESSION.get(),
                            data.getLevel()
                    ));

                    customer.setSpawnPos(safePos);
                    customer.setOffers(offers);
                    customer.setCounterBlockState(counterBlockState);

                    // Finalize spawn logic (sets default items, resets AI brain, etc.)
                    customer.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);

                    // Spawn the entity in the world
                    serverLevel.addFreshEntity(customer);

                    return customer;
                }
            }
        }
        return null;
    }

    private CustomerState state;
    private BlockPos spawnPos;
    private BlockState counterBlockState;

    public CustomerVillagerEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public CustomerState getState() {
        return state;
    }

    public void setState(CustomerState state) {
        this.state = state;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public void setSpawnPos(BlockPos spawnPos) {
        this.spawnPos = spawnPos;
    }

    public BlockState getCounterBlockState() {
        return counterBlockState;
    }

    public void setCounterBlockState(BlockState counterBlockState) {
        this.counterBlockState = counterBlockState;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        // No behavior-based AI
    }

    @Override
    protected void registerGoals() {
        // Setup the goal system
        super.registerGoals();
        // Remove the standard goals
        this.goalSelector.removeAllGoals(goal -> true);
        // Remove the standard targets
        this.targetSelector.removeAllGoals(goal -> true);

        // Start with looking at the player
        this.goalSelector.addGoal(0, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8));

        // Customer specific goals
        this.goalSelector.addGoal(0, new CustomerMoveToCounterGoal(this, counterBlockState, 1));
        this.goalSelector.addGoal(0, new CustomerMoveToSpawnGoal(this, 1));
        this.goalSelector.addGoal(0, new CustomerLeaveGoal(this, 1));
    }

    @Override
    @NotNull
    public net.minecraft.network.chat.Component getDisplayName() {
        return Component.translatable("entity.customers.customer_villager");
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (state != CustomerState.BUYING) {
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    private MerchantOffers previousOffers;

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            MerchantOffers currentOffers = this.getOffers();
            if (previousOffers == null) {
                previousOffers = currentOffers;
            }

            // this.addParticlesAroundSelf(ParticleTypes.HEART);

            Player tradingPlayer = this.getTradingPlayer();
            long numRemaining = currentOffers.stream().filter(offer -> !offer.isOutOfStock()).count();
            if (numRemaining == 0) {
                if (tradingPlayer != null) {
                    tradingPlayer.closeContainer();
                    setTradingPlayer(null);
                }
            } else if (tradingPlayer == null) {
                boolean removedAny = currentOffers.removeIf(MerchantOffer::isOutOfStock);
                if (removedAny) {
                    this.setOffers(currentOffers);
                }
            }
            if (currentOffers.isEmpty()) {
                this.discard();
            }
        }
    }
}
