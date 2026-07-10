package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.customer.ai.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CustomerVillagerEntity extends Villager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TAG_STATE = "CustomerState";
    private static final String TAG_SPAWNER_POS = "SpawnerPos";
    private static final String TAG_SPAWN_POS = "SpawnPos";
    private static final String TAG_COUNTER_BLOCK_STATE = "CounterBlockState";
    private static final String TAG_AVOID_BLOCK_STATE = "AvoidBlockState";
    private static final String TAG_TRADED_WITH_PLAYERS = "TradedWithPlayers";
    private static final String TAG_TRADED_PLAYER_UUID = "UUID";
    private static final int MAX_SYNCED_DISPLAY_OFFERS = 3;
    private static final EntityDataAccessor<Integer> DATA_CUSTOMER_STATE = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_OFFER_DISPLAY_ITEM_0 = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_OFFER_DISPLAY_ITEM_1 = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_OFFER_DISPLAY_ITEM_2 = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final Map<Item, Item> TRADE_REMAINDER_FALLBACKS = Map.of(
            Items.MUSHROOM_STEW, Items.BOWL,
            Items.RABBIT_STEW, Items.BOWL,
            Items.BEETROOT_SOUP, Items.BOWL,
            Items.SUSPICIOUS_STEW, Items.BOWL,
            Items.POTION, Items.GLASS_BOTTLE
    );

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
            BlockPos spawnerPos,
            MerchantOffers offers,
            BlockState counterBlockState,
            BlockState avoidBlockState
    ) {
        return spawn(Customer.CUSTOMER_VILLAGER.get(), level, spawnerPos, offers, counterBlockState, avoidBlockState);
    }

    public static CustomerVillagerEntity spawn(
            EntityType<? extends CustomerVillagerEntity> customerType,
            Level level,
            BlockPos spawnerPos,
            MerchantOffers offers,
            BlockState counterBlockState,
            BlockState avoidBlockState
    ) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel)level;
            CustomerVillagerEntity customer = customerType.create(level);
            if (customer != null) {
                BlockPos safePos = MobUtils.getRandomSpawnPos(level, spawnerPos, 5, 3);
                if (safePos != null) {
                    customer.moveTo(safePos, 0, 0);

                    VillagerData data = customer.getVillagerData();
                    customer.setVillagerData(new VillagerData(
                            getVillagerTypeForLocation(level, spawnerPos),
                            Customer.CUSTOMER_PROFESSION.get(),
                            data.getLevel()
                    ));

                    customer.setSpawnerPos(spawnerPos);
                    customer.setSpawnPos(safePos);
                    customer.setOffers(offers);
                    customer.setCounterBlockState(counterBlockState);
                    customer.setAvoidBlockState(avoidBlockState);

                    customer.setState(CustomerState.INITIALIZING);

                    // Finalize spawn logic (sets default items, resets AI brain, etc.)
                    customer.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnerPos), MobSpawnType.COMMAND, null);

                    // Spawn the entity in the world
                    serverLevel.addFreshEntity(customer);

                    return customer;
                }
            }
        }
        return null;
    }

    private CustomerState state;
    private long ticksInState = 0;
    private BlockPos spawnerPos;
    private BlockPos spawnPos;
    private BlockState counterBlockState;
    private BlockState avoidBlockState;
    private BlockPos counterTargetBlockPos;
    private Set<UUID> tradedWithPlayers = new HashSet<>();
    private long ticksSinceTrade = 0;
    private long ticksSincePlayerScan = 0;

    public CustomerVillagerEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CUSTOMER_STATE, -1);
        builder.define(DATA_OFFER_DISPLAY_ITEM_0, ItemStack.EMPTY);
        builder.define(DATA_OFFER_DISPLAY_ITEM_1, ItemStack.EMPTY);
        builder.define(DATA_OFFER_DISPLAY_ITEM_2, ItemStack.EMPTY);
    }

    public CustomerState getState() {
        int syncedState = entityData.get(DATA_CUSTOMER_STATE);
        CustomerState[] states = CustomerState.values();
        if (syncedState >= 0 && syncedState < states.length) {
            return states[syncedState];
        }
        return state;
    }

    public void setState(CustomerState state) {
        this.state = state;
        entityData.set(DATA_CUSTOMER_STATE, state == null ? -1 : state.ordinal());
        ticksInState = 0;
        ticksSinceTrade = 0;
    }

    public long getTicksInState() {
        return ticksInState;
    }

    public BlockPos getSpawnerPos() {
        return spawnerPos;
    }

    public void setSpawnerPos(BlockPos spawnerPos) {
        this.spawnerPos = spawnerPos;
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

    public BlockState getAvoidBlockState() {
        return avoidBlockState;
    }

    public void setAvoidBlockState(BlockState avoidBlockState) {
        this.avoidBlockState = avoidBlockState;
    }

    public BlockPos getCounterTargetBlockPos() {
        return counterTargetBlockPos;
    }

    public void setCounterTargetBlockPos(BlockPos counterBlockPos) {
        this.counterTargetBlockPos = counterTargetBlockPos;
    }

    public long getTicksSinceTrade() {
        return ticksSinceTrade;
    }

    public Set<UUID> getTradedWithPlayers() {
        return tradedWithPlayers;
    }

    @Override
    public void setOffers(MerchantOffers offers) {
        super.setOffers(offers);
        updateOfferDisplayItems(offers);
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        super.overrideOffers(offers);
        updateOfferDisplayItems(offers);
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        super.notifyTrade(offer);
        if (!level().isClientSide()) {
            updateOfferDisplayItems(getOffers());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_STATE)) {
            String stateName = compound.getString(TAG_STATE);
            try {
                setState(CustomerState.valueOf(stateName));
            } catch (IllegalArgumentException exception) {
                LOGGER.warn("Ignoring unknown customer state while loading: {}", stateName);
            }
        }
        NbtUtils.readBlockPos(compound, TAG_SPAWNER_POS).ifPresent(this::setSpawnerPos);
        NbtUtils.readBlockPos(compound, TAG_SPAWN_POS).ifPresent(this::setSpawnPos);
        if (compound.contains(TAG_COUNTER_BLOCK_STATE)) {
            counterBlockState = NbtUtils.readBlockState(
                    registryAccess().lookupOrThrow(Registries.BLOCK),
                    compound.getCompound(TAG_COUNTER_BLOCK_STATE)
            );
        }
        if (compound.contains(TAG_AVOID_BLOCK_STATE)) {
            avoidBlockState = NbtUtils.readBlockState(
                    registryAccess().lookupOrThrow(Registries.BLOCK),
                    compound.getCompound(TAG_AVOID_BLOCK_STATE)
            );
        }
        tradedWithPlayers.clear();
        if (compound.contains(TAG_TRADED_WITH_PLAYERS, Tag.TAG_LIST)) {
            ListTag tradedPlayerTags = compound.getList(TAG_TRADED_WITH_PLAYERS, Tag.TAG_COMPOUND);
            for (int i = 0; i < tradedPlayerTags.size(); i++) {
                CompoundTag tradedPlayerTag = tradedPlayerTags.getCompound(i);
                if (tradedPlayerTag.hasUUID(TAG_TRADED_PLAYER_UUID)) {
                    tradedWithPlayers.add(tradedPlayerTag.getUUID(TAG_TRADED_PLAYER_UUID));
                }
            }
        }
        if (!level().isClientSide()) {
            updateOfferDisplayItems(getOffers());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (state != null) {
            compound.putString(TAG_STATE, state.name());
        }
        if (spawnerPos != null) {
            compound.put(TAG_SPAWNER_POS, NbtUtils.writeBlockPos(spawnerPos));
        }
        if (spawnPos != null) {
            compound.put(TAG_SPAWN_POS, NbtUtils.writeBlockPos(spawnPos));
        }
        if (counterBlockState != null) {
            compound.put(TAG_COUNTER_BLOCK_STATE, NbtUtils.writeBlockState(counterBlockState));
        }
        if (avoidBlockState != null) {
            compound.put(TAG_AVOID_BLOCK_STATE, NbtUtils.writeBlockState(avoidBlockState));
        }
        if (!tradedWithPlayers.isEmpty()) {
            ListTag tradedPlayerTags = new ListTag();
            for (UUID playerUuid : tradedWithPlayers) {
                CompoundTag tradedPlayerTag = new CompoundTag();
                tradedPlayerTag.putUUID(TAG_TRADED_PLAYER_UUID, playerUuid);
                tradedPlayerTags.add(tradedPlayerTag);
            }
            compound.put(TAG_TRADED_WITH_PLAYERS, tradedPlayerTags);
        }
    }

    public List<ItemStack> getOfferDisplayItems() {
        List<ItemStack> items = new ArrayList<>(MAX_SYNCED_DISPLAY_OFFERS);
        addOfferDisplayItem(items, entityData.get(DATA_OFFER_DISPLAY_ITEM_0));
        addOfferDisplayItem(items, entityData.get(DATA_OFFER_DISPLAY_ITEM_1));
        addOfferDisplayItem(items, entityData.get(DATA_OFFER_DISPLAY_ITEM_2));
        return items;
    }

    private void updateOfferDisplayItems(MerchantOffers offers) {
        ItemStack[] displayItems = new ItemStack[MAX_SYNCED_DISPLAY_OFFERS];
        for (int i = 0; i < displayItems.length; i++) {
            displayItems[i] = ItemStack.EMPTY;
        }

        int displayIndex = 0;
        for (MerchantOffer offer : offers) {
            if (!offer.isOutOfStock()) {
                displayItems[displayIndex] = offer.getBaseCostA().copy();
                displayIndex++;
                if (displayIndex >= MAX_SYNCED_DISPLAY_OFFERS) {
                    break;
                }
            }
        }

        entityData.set(DATA_OFFER_DISPLAY_ITEM_0, displayItems[0]);
        entityData.set(DATA_OFFER_DISPLAY_ITEM_1, displayItems[1]);
        entityData.set(DATA_OFFER_DISPLAY_ITEM_2, displayItems[2]);
    }

    private static void addOfferDisplayItem(List<ItemStack> items, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            items.add(itemStack);
        }
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
        goalSelector.removeAllGoals(goal -> true);
        // Remove the standard targets
        targetSelector.removeAllGoals(goal -> true);

        // Start with looking at the player
        goalSelector.addGoal(0, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8));

        // Customer specific goals
        goalSelector.addGoal(0, new CustomerMoveToCounterGoal(this, 0.5));
        goalSelector.addGoal(0, new CustomerThankGoal(this));
        goalSelector.addGoal(0, new CustomerGiveUpGoal(this));
        goalSelector.addGoal(0, new CustomerMoveToSpawnGoal(this, 0.5));
        goalSelector.addGoal(0, new CustomerLeaveGoal(this, 0.5));
    }

    @Override
    @NotNull
    public net.minecraft.network.chat.Component getDisplayName() {
        if (getCustomName() != null) {
            return getCustomName();
        }
        return Component.translatable("entity.customers.customer_villager");
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // Add all interacting players to the spawner
        if (!level().isClientSide()) {
            if (spawnerPos != null && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
                spawner.addPlayer(player.getUUID());
            }
        }
        if (getState() != CustomerState.BUYING) {
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        super.rewardTradeXp(offer);

        if (!level().isClientSide()) {
            ticksSinceTrade = 0;
            Player tradingPlayer = getTradingPlayer();
            if (tradingPlayer != null) {
                giveTradeRemainderItems(tradingPlayer, offer.getCostA());
                tradedWithPlayers.add(tradingPlayer.getUUID());
                playHappy();
                if (level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
                    spawner.scoreboardAddItemServed(tradingPlayer.getUUID());
                }
            }
        }
    }

    private static void giveTradeRemainderItems(Player player, ItemStack soldStack) {
        ItemStack remainder = getTradeRemainderItem(soldStack);
        if (remainder.isEmpty()) {
            return;
        }

        remainder.setCount(soldStack.getCount());
        if (!player.getInventory().add(remainder)) {
            player.drop(remainder, false);
        }
    }

    private static ItemStack getTradeRemainderItem(ItemStack soldStack) {
        if (soldStack.hasCraftingRemainingItem()) {
            return soldStack.getCraftingRemainingItem();
        }

        Item fallbackItem = TRADE_REMAINDER_FALLBACKS.get(soldStack.getItem());
        return fallbackItem == null ? ItemStack.EMPTY : new ItemStack(fallbackItem);
    }

    public void playHappy() {
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 14);
        }
    }

    public void playLove() {
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 12);
        }
    }

    public void playAngry() {
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, (byte) 13);
        }
    }

    public void sentPlayersMessage(Component message) {
        if (!level().isClientSide() && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.sentPlayersMessage(message);
        }
    }

    public void sentPlayersChat(Component message) {
        if (!level().isClientSide() && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.sentPlayersChat(message);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (getState() == CustomerState.BUYING) {
                MerchantOffers currentOffers = getOffers();
                Player tradingPlayer = getTradingPlayer();
                long numRemaining = currentOffers.stream().filter(offer -> !offer.isOutOfStock()).count();
                if (tradingPlayer == null || numRemaining == 0) {
                    if (numRemaining == 0) {
                        if (tradingPlayer != null) {
                            tradingPlayer.closeContainer();
                            setTradingPlayer(null);
                        }
                    }
                    boolean removedAny = currentOffers.removeIf(MerchantOffer::isOutOfStock);
                    if (removedAny || numRemaining == 0) {
                        setOffers(currentOffers);
                    }
                }
                if (getOffers().isEmpty()) {
                    playLove();
                }
            }
            ticksInState++;
            ticksSinceTrade++;

            // Look for near by players and add them to the spawner
            if (ticksSincePlayerScan == 0 || ticksSincePlayerScan > 20) {
                ticksSincePlayerScan = 0;
                if (spawnerPos != null && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
                    List<Player> nearbyPlayers = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(5));
                    for (Player player : nearbyPlayers) {
                        spawner.addPlayer(player.getUUID());
                    }
                }
            }
            ticksSincePlayerScan++;
        }
    }
}



