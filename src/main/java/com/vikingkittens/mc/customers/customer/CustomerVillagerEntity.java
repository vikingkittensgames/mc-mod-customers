package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.compatability.InteractionCUtils;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.VillagerCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;
import com.vikingkittens.mc.customers.customer.ai.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CustomerVillagerEntity extends Villager {
    @Override
    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        if (vehicle instanceof CustomerSeatEntity) {
            return CustomerSeatLogic.getCustomerVehicleAttachmentPoint();
        }
        return super.getVehicleAttachmentPoint(vehicle);
    }
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TAG_STATE = "CustomerState";
    private static final String TAG_SPAWNER_POS = "SpawnerPos";
    private static final String TAG_SPAWN_POS = "SpawnPos";
    private static final String TAG_COUNTER_TARGET_BLOCK_POS = "CounterTargetBlockPos";
    private static final String TAG_COUNTER_BLOCK_STATE = "CounterBlockState";
    private static final String TAG_AVOID_BLOCK_STATE = "AvoidBlockState";
    private static final String TAG_TRADED_WITH_PLAYERS = "TradedWithPlayers";
    private static final String TAG_TRADED_PLAYER_UUID = "UUID";
    private static final int MAX_SYNCED_DISPLAY_OFFERS = 3;
    private static final EntityDataAccessor<Integer> DATA_CUSTOMER_STATE = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CUSTOMER_SITTING = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.BOOLEAN);
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

    private static ResourceKey<VillagerType> getVillagerTypeForLocation(Level level, BlockPos pos) {
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
        if (!LevelCUtils.isClientSide(level)) {
            ServerLevel serverLevel = (ServerLevel)level;
            CustomerVillagerEntity customer = EntityCUtils.create(
                    customerType,
                    level
            );
            if (customer != null) {
                BlockPos safePos = MobUtils.getRandomSpawnPos(level, spawnerPos, 5, 3);
                if (safePos != null) {
                    EntityCUtils.snapTo(customer, safePos, 0, 0);
                    customer.setOnGround(true);

                    VillagerData data = customer.getVillagerData();
                    ResourceKey<VillagerProfession> profession = Customer.CUSTOMER_PROFESSION.getKey();
                    float professionVariantPercentage = level.random.nextFloat();
                    if (professionVariantPercentage < 0.20F) {
                        profession = Customer.CUSTOMER_IMPATIENT_PROFESSION.getKey();
                    } else if (professionVariantPercentage < 0.50F) {
                        profession = Customer.CUSTOMER_CASUAL_PROFESSION.getKey();
                    }
                    customer.setVillagerData(
                            VillagerCUtils.withTypeAndProfession(
                                    data,
                                    level.registryAccess(),
                                    getVillagerTypeForLocation(
                                            level,
                                            spawnerPos
                                    ),
                                    profession
                            )
                    );

                    customer.setSpawnerPos(spawnerPos);
                    customer.setSpawnPos(safePos);
                    customer.setOffers(offers);
                    customer.setCounterBlockState(counterBlockState);
                    customer.setAvoidBlockState(avoidBlockState);

                    customer.setState(CustomerState.INITIALIZING);

                    customer.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnerPos), EntitySpawnReason.COMMAND, null);

                    serverLevel.addFreshEntity(customer);

                    return customer;
                }
            }
        }
        return null;
    }

    public static CustomerVillagerEntity getActiveCustomer(Level level, UUID customerId) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(customerId);
        if (entity instanceof CustomerVillagerEntity customer) {
            if (
                    customer.isAlive() &&
                    !customer.isRemoved() &&
                    customer.getState() != null &&
                    customer.getState().compareTo(CustomerState.DONE) < 0
            ) {
                return customer;
            }
        }
        return null;
    }

    public static boolean isActiveCustomer(Level level, UUID customerId) {
        return getActiveCustomer(level, customerId) != null;
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
    protected void doPush(Entity entity) {
        if (entity instanceof CustomerVillagerEntity otherCustomer && !isPassenger() && !entity.isPassenger()) {
            CustomerState state = getState();
            if (state != null && state.canPushCustomer(otherCustomer.getState())) {
                super.doPush(entity);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CUSTOMER_STATE, -1);
        builder.define(DATA_CUSTOMER_SITTING, false);
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

    public boolean isCustomerSitting() {
        return entityData.get(DATA_CUSTOMER_SITTING);
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

    public CustomerSpawnerBlockEntity getSpawner() {
        try {
            if (
                    getSpawnerPos() != null &&
                    level().getBlockEntity(getSpawnerPos()) instanceof CustomerSpawnerBlockEntity spawner
            ) {
                return spawner;
            }
        } catch (Throwable t) {
            LOGGER.error("Unable to find spawner because of error", t);
        }
        LOGGER.warn("Removing customer {} because spawner at {} lost", getUUID(), getSpawnerPos());
        discard();
        return null;
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
        this.counterTargetBlockPos = counterBlockPos;
    }

    public long getTicksSinceTrade() {
        return ticksSinceTrade;
    }

    public Set<UUID> getTradedWithPlayers() {
        return tradedWithPlayers;
    }

    public boolean tryQuickSell(Player player) {
        if (
                LevelCUtils.isClientSide(level()) ||
                getState() != CustomerState.BUYING ||
                getTradingPlayer() != null
        ) {
            return false;
        }

        ItemStack heldStack = player.getMainHandItem();
        MerchantOffer offer = findQuickSellOffer(getOffers(), heldStack);
        if (offer == null) {
            return false;
        }

        setTradingPlayer(player);
        try {
            if (!offer.take(heldStack, ItemStack.EMPTY)) {
                return false;
            }

            ItemStack result = offer.assemble();
            ItemStackCUtils.onCraftedBy(
                    result,
                    player,
                    result.getCount()
            );
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }

            notifyTrade(offer);
            player.awardStat(Stats.TRADED_WITH_VILLAGER);
            overrideXp(getVillagerXp() + offer.getXp());
            playSound(getNotifyTradeSound(), 1.0F, 1.0F);
            return true;
        } finally {
            setTradingPlayer(null);
        }
    }

    static MerchantOffer findQuickSellOffer(
            List<MerchantOffer> offers,
            ItemStack heldStack
    ) {
        return CustomerQuickSellOfferSelector.find(
                offers,
                offer -> !offer.isOutOfStock(),
                offer -> offer.satisfiedBy(heldStack, ItemStack.EMPTY)
        );
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
        if (!LevelCUtils.isClientSide(level())) {
            updateOfferDisplayItems(getOffers());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        readCustomerData(PersistenceCUtils.reader(input));
    }

    void readCustomerData(DataReader input) {
        input.getString(TAG_STATE).ifPresent(stateName -> {
            try {
                setState(CustomerState.valueOf(stateName));
            } catch (IllegalArgumentException exception) {
                LOGGER.warn("Ignoring unknown customer state while loading: {}", stateName);
            }
        });
        input.getBlockPos(TAG_SPAWNER_POS).ifPresent(this::setSpawnerPos);
        input.getBlockPos(TAG_SPAWN_POS).ifPresent(this::setSpawnPos);
        readCounterTargetBlockPos(input).ifPresent(this::setCounterTargetBlockPos);
        input.getBlockState(TAG_COUNTER_BLOCK_STATE)
                .ifPresent(state -> counterBlockState = state);
        input.getBlockState(TAG_AVOID_BLOCK_STATE)
                .ifPresent(state -> avoidBlockState = state);

        tradedWithPlayers.clear();
        input.getChildren(TAG_TRADED_WITH_PLAYERS).forEach(
                tradedPlayerInput -> tradedPlayerInput
                        .getUuid(TAG_TRADED_PLAYER_UUID)
                        .ifPresent(tradedWithPlayers::add)
        );

        if (!LevelCUtils.isClientSide(level())) {
            updateOfferDisplayItems(getOffers());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        writeCustomerData(PersistenceCUtils.writer(output));
    }

    void writeCustomerData(DataWriter output) {
        if (state != null) {
            output.putString(TAG_STATE, state.name());
        }
        if (spawnerPos != null) {
            output.putBlockPos(TAG_SPAWNER_POS, spawnerPos);
        }
        if (spawnPos != null) {
            output.putBlockPos(TAG_SPAWN_POS, spawnPos);
        }
        if (counterTargetBlockPos != null) {
            saveCounterTargetBlockPos(output, counterTargetBlockPos);
        }
        if (counterBlockState != null) {
            output.putBlockState(TAG_COUNTER_BLOCK_STATE, counterBlockState);
        }
        if (avoidBlockState != null) {
            output.putBlockState(TAG_AVOID_BLOCK_STATE, avoidBlockState);
        }
        for (UUID playerUuid : tradedWithPlayers) {
            output.addChild(TAG_TRADED_WITH_PLAYERS)
                    .putUuid(TAG_TRADED_PLAYER_UUID, playerUuid);
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
    public boolean causeFallDamage(
            double fallDistance,
            float multiplier,
            DamageSource source
    ) {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);

        goalSelector.addGoal(0, new FloatGoal(this));

        goalSelector.addGoal(0, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8));

        goalSelector.addGoal(0, new CustomerMoveToCounterGoal(this, 0.5));
        goalSelector.addGoal(0, new CustomerLineUpGoal(this, 0.5));
        goalSelector.addGoal(0, new CustomerWaitOnLeaderGoal(this));
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
        if (!LevelCUtils.isClientSide(level())) {
            if (spawnerPos != null && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
                spawner.addPlayer(player.getUUID());
            }
        }
        if (getState() != CustomerState.BUYING) {
            return InteractionCUtils.sidedSuccess(
                    LevelCUtils.isClientSide(level())
            );
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        super.rewardTradeXp(offer);

        if (!LevelCUtils.isClientSide(level())) {
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
        ItemStack craftingRemainder =
                ItemStackCUtils.getCraftingRemainder(soldStack);
        if (!craftingRemainder.isEmpty()) {
            return craftingRemainder;
        }

        Item fallbackItem = TRADE_REMAINDER_FALLBACKS.get(soldStack.getItem());
        return fallbackItem == null ? ItemStack.EMPTY : new ItemStack(fallbackItem);
    }

    public void playHappy() {
        if (!LevelCUtils.isClientSide(level())) {
            level().broadcastEntityEvent(this, (byte) 14);
        }
    }

    public void playLove() {
        if (!LevelCUtils.isClientSide(level())) {
            level().broadcastEntityEvent(this, (byte) 12);
        }
    }

    public void playAngry() {
        if (!LevelCUtils.isClientSide(level())) {
            level().broadcastEntityEvent(this, (byte) 13);
        }
    }

    public void sentPlayersMessage(Component message) {
        if (!LevelCUtils.isClientSide(level()) && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.sentPlayersMessage(message);
        }
    }

    public void sentPlayersChat(Component message) {
        if (!LevelCUtils.isClientSide(level()) && level().getBlockEntity(spawnerPos) instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.sentPlayersChat(message);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!LevelCUtils.isClientSide(level())) {
            entityData.set(
                    DATA_CUSTOMER_SITTING,
                    isPassenger() && getVehicle() instanceof CustomerSeatEntity
            );
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

    static Optional<BlockPos> readCounterTargetBlockPos(DataReader input) {
        return input.getBlockPos(TAG_COUNTER_TARGET_BLOCK_POS);
    }

    static void saveCounterTargetBlockPos(
            DataWriter output,
            BlockPos counterTargetBlockPos
    ) {
        output.putBlockPos(TAG_COUNTER_TARGET_BLOCK_POS, counterTargetBlockPos);
    }

}
