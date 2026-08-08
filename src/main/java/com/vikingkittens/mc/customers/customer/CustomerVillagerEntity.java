package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
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
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.registries.datamaps.builtin.BiomeVillagerType;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.CustomersVillager;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearance;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearancePersistence;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerType;
import com.vikingkittens.mc.customers.common.MobUtils;
import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.compatability.InteractionCUtils;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.compatability.VillagerCUtils;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.customer.ai.*;

public class CustomerVillagerEntity extends Villager implements CustomersVillager {
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
    private static final String TAG_OFFERS_CRAFTED = "OffersCrafted";
    private static final String TAG_TRADED_PLAYER_UUID = "UUID";
    private static final String TAG_APPEARANCE_SPAWNER_MODE =
            "CustomersAppearanceSpawnerMode";
    private static final String TAG_APPEARANCE_SPECIAL =
            "CustomersAppearanceSpecial";
    private static final EntityDataAccessor<Integer> DATA_CUSTOMER_STATE = SynchedEntityData.defineId(CustomerVillagerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_APPEARANCE =
            SynchedEntityData.defineId(
                    CustomerVillagerEntity.class,
                    EntityDataSerializers.STRING
            );
    private static final EntityDataAccessor<Float> DATA_VARIATION_SEED =
            SynchedEntityData.defineId(
                    CustomerVillagerEntity.class,
                    EntityDataSerializers.FLOAT
            );
    private static final EntityDataAccessor<Integer> DATA_APPEARANCE_SPAWNER_MODE =
            SynchedEntityData.defineId(
                    CustomerVillagerEntity.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Boolean> DATA_APPEARANCE_SPECIAL =
            SynchedEntityData.defineId(
                    CustomerVillagerEntity.class,
                    EntityDataSerializers.BOOLEAN
            );
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
            return BuiltInRegistries.VILLAGER_TYPE.getResourceKey(mapData.type()).orElseThrow();
        }
        return BuiltInRegistries.VILLAGER_TYPE.getResourceKey(VillagerType.PLAINS).orElseThrow();
    }

    public static CustomerVillagerEntity spawn(
            Level level,
            BlockPos spawnerPos,
            MerchantOffers offers,
            BlockState counterBlockState,
            BlockState avoidBlockState
    ) {
        if (!LevelCUtils.isClientSide(level)) {
            ServerLevel serverLevel = (ServerLevel)level;
            CustomerVillagerEntity customer =
                    EntityCUtils.create(
                            Customer.CUSTOMER_VILLAGER.get(),
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
                                    getVillagerTypeForLocation(level, spawnerPos),
                                    profession
                            )
                    );

                    customer.setSpawnerPos(spawnerPos);
                    customer.setSpawnPos(safePos);
                    customer.setOffers(offers);
                    customer.setCounterBlockState(counterBlockState);
                    customer.setAvoidBlockState(avoidBlockState);

                    customer.setState(CustomerState.INITIALIZING);

                    customer.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnerPos), MobSpawnType.COMMAND, null);

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
    private final List<ItemStack> offersCrafted = new ArrayList<>();
    private long ticksSinceTrade = 0;
    private long ticksSincePlayerScan = 0;

    /**
     * Assigns as much of a crafted stack as possible to this customer's
     * outstanding offers.
     *
     * @param stack crafted items available for assignment
     * @return null when fully assigned, the unassigned remainder when partially
     *         assigned, or the original stack when nothing matched
     */
    public @Nullable ItemStack tryAssignCraftedOffer(ItemStack stack) {
        return tryAssignCraftedOffer(
                getOffers(),
                offersCrafted,
                stack
        );
    }

    public int releaseCraftedOfferAssignment(ItemStack stack) {
        return releaseCraftedOfferAssignment(offersCrafted, stack);
    }

    /**
     * Completes an offer taken from a pickup counter and credits its crafting
     * player with serving the items.
     *
     * @param offer completed customer offer
     * @param crafterId player who deposited the stored stack
     * @param counterPosition pickup counter supplying the item
     */
    public void completePickupCounterOffer(
            MerchantOffer offer,
            UUID crafterId,
            BlockPos counterPosition
    ) {
        int servedCount = completePickupCounterOffer(
                offer,
                offersCrafted,
                tradedWithPlayers,
                crafterId
        );
        ticksSinceTrade = 0;
        playHappy();
        if (level() instanceof ServerLevel serverLevel) {
            Player crafter = serverLevel.getServer()
                    .getPlayerList()
                    .getPlayer(crafterId);
            if (crafter != null) {
                giveTradeRemainderItems(crafter, offer.getCostA());
            } else {
                ItemStack remainder =
                        getTradeRemainderStack(offer.getCostA());
                if (!remainder.isEmpty()) {
                    Containers.dropItemStack(
                            level(),
                            counterPosition.getX() + 0.5D,
                            counterPosition.getY() + 1.5D,
                            counterPosition.getZ() + 0.5D,
                            remainder
                    );
                }
            }
        }
        if (spawnerPos != null
                && level().getBlockEntity(spawnerPos)
                        instanceof CustomerSpawnerBlockEntity spawner) {
            spawner.scoreboardAddItemsServed(crafterId, servedCount);
        }
    }

    /**
     * Applies pickup-counter fulfillment to customer-owned offer state.
     *
     * @param offer completed customer offer
     * @param craftedStacks reserved crafted-item stacks
     * @param tradedPlayers players credited with serving this customer
     * @param crafterId player who deposited the stored stack
     * @return number of items served
     */
    static int completePickupCounterOffer(
            MerchantOffer offer,
            List<ItemStack> craftedStacks,
            Set<UUID> tradedPlayers,
            UUID crafterId
    ) {
        ItemStack cost = offer.getCostA();
        releaseCraftedOfferAssignment(craftedStacks, cost);
        offer.increaseUses();
        tradedPlayers.add(crafterId);
        return cost.getCount();
    }

    static int releaseCraftedOfferAssignment(
            List<ItemStack> craftedStacks,
            ItemStack stack
    ) {
        int remainingCount = stack.getCount();
        Iterator<ItemStack> iterator = craftedStacks.iterator();
        while (iterator.hasNext() && remainingCount > 0) {
            ItemStack craftedStack = iterator.next();
            if (!ItemStack.isSameItemSameComponents(
                    craftedStack,
                    stack
            )) {
                continue;
            }
            int releasedCount = Math.min(
                    remainingCount,
                    craftedStack.getCount()
            );
            craftedStack.shrink(releasedCount);
            remainingCount -= releasedCount;
            if (craftedStack.isEmpty()) {
                iterator.remove();
            }
        }
        return stack.getCount() - remainingCount;
    }
    /**
     * Returns how many items could be assigned without changing customer state.
     *
     * @param stack items being considered
     * @return number of items that could be assigned
     */
    public int getAssignableCraftedItemCount(ItemStack stack) {
        return getAssignableCraftedItemCount(
                getOffers(),
                offersCrafted,
                stack
        );
    }

    /**
     * Calculates assignable demand without changing the crafted-item list.
     *
     * @param offers outstanding customer offers
     * @param craftedStacks quantities already assigned
     * @param stack items being considered
     * @return number of items that could be assigned
     */
    static int getAssignableCraftedItemCount(
            List<MerchantOffer> offers,
            List<ItemStack> craftedStacks,
            ItemStack stack
    ) {
        int wantedCount = 0;
        for (MerchantOffer offer : offers) {
            ItemStack cost = offer.getCostA();
            if (!offer.isOutOfStock()
                    && offer.getItemCostA().test(stack)) {
                wantedCount += cost.getCount();
            }
        }

        int craftedCount = 0;
        for (ItemStack craftedStack : craftedStacks) {
            if (ItemStack.isSameItemSameComponents(
                    craftedStack,
                    stack
            )) {
                craftedCount += craftedStack.getCount();
            }
        }
        return Math.min(
                stack.getCount(),
                Math.max(0, wantedCount - craftedCount)
        );
    }

    /**
     * Assigns crafted items against outstanding offers while accounting for
     * quantities already assigned to matching offers.
     *
     * @param offers outstanding customer offers
     * @param craftedStacks quantities already assigned
     * @param stack crafted items available for assignment
     * @return null when fully assigned, the unassigned remainder when partially
     *         assigned, or the original stack when nothing matched
     */
    static @Nullable ItemStack tryAssignCraftedOffer(
            List<MerchantOffer> offers,
            List<ItemStack> craftedStacks,
            ItemStack stack
    ) {
        int assignCount = getAssignableCraftedItemCount(
                offers,
                craftedStacks,
                stack
        );
        if (assignCount == 0) {
            return stack;
        }

        ItemStack matchingCraftedStack = craftedStacks.stream()
                .filter(craftedStack ->
                        ItemStack.isSameItemSameComponents(
                                craftedStack,
                                stack
                        ))
                .findFirst()
                .orElse(null);
        if (matchingCraftedStack == null) {
            matchingCraftedStack = stack.copy();
            matchingCraftedStack.setCount(assignCount);
            craftedStacks.add(matchingCraftedStack);
        } else {
            matchingCraftedStack.grow(assignCount);
        }

        if (assignCount == stack.getCount()) {
            return null;
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(assignCount);
        return remainder;
    }
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
        builder.define(
                DATA_APPEARANCE,
                CustomersVillagerAppearances.DEFAULT.toString()
        );
        builder.define(DATA_VARIATION_SEED, 0.0F);
        builder.define(DATA_APPEARANCE_SPAWNER_MODE, -1);
        builder.define(DATA_APPEARANCE_SPECIAL, false);
    }

    @Override
    public CustomersVillagerType getCustomersVillagerType() {
        return switch (getSnapshotType()) {
            case NORMAL -> CustomersVillagerType.CUSTOMER_NORMAL;
            case IMPATIENT -> CustomersVillagerType.CUSTOMER_IMPATIENT;
            case CASUAL -> CustomersVillagerType.CUSTOMER_CASUAL;
        };
    }

    @Override
    public Optional<CustomerSpawnerMode> getSpawnerMode() {
        int modeIndex = entityData.get(DATA_APPEARANCE_SPAWNER_MODE);
        CustomerSpawnerMode[] modes = CustomerSpawnerMode.values();
        return modeIndex >= 0 && modeIndex < modes.length
                ? Optional.of(modes[modeIndex])
                : Optional.empty();
    }

    public void setSpawnerMode(CustomerSpawnerMode spawnerMode) {
        entityData.set(
                DATA_APPEARANCE_SPAWNER_MODE,
                spawnerMode == null ? -1 : spawnerMode.ordinal()
        );
    }

    @Override
    public boolean isSpecial() {
        return entityData.get(DATA_APPEARANCE_SPECIAL);
    }

    public void setSpecial(boolean special) {
        entityData.set(DATA_APPEARANCE_SPECIAL, special);
    }

    @Override
    public ResourceLocation getAppearanceId() {
        return ResourceLocation.parse(entityData.get(DATA_APPEARANCE));
    }

    @Override
    public void setAppearanceId(ResourceLocation appearanceId) {
        entityData.set(DATA_APPEARANCE, appearanceId.toString());
    }

    @Override
    public float getVariationSeed() {
        return entityData.get(DATA_VARIATION_SEED);
    }

    @Override
    public void setVariationSeed(float variationSeed) {
        entityData.set(DATA_VARIATION_SEED, variationSeed);
    }

    @Override
    public boolean isSitting() {
        return isPassenger();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        CustomersVillagerAppearance appearance =
                CustomersVillagerAppearances.get(this);
        if (appearance != null) {
            SoundEvent sound = appearance.getAmbientSound(this);
            if (sound != null) {
                return sound;
            }
        }
        return super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        CustomersVillagerAppearance appearance =
                CustomersVillagerAppearances.get(this);
        if (appearance != null) {
            SoundEvent sound = appearance.getHurtSound(this);
            if (sound != null) {
                return sound;
            }
        }
        return super.getHurtSound(damageSource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        CustomersVillagerAppearance appearance =
                CustomersVillagerAppearances.get(this);
        if (appearance != null) {
            SoundEvent sound = appearance.getDeathSound(this);
            if (sound != null) {
                return sound;
            }
        }
        return super.getDeathSound();
    }

    @Override
    protected void playStepSound(
            BlockPos position,
            BlockState blockState
    ) {
        CustomersVillagerAppearance appearance =
                CustomersVillagerAppearances.get(this);
        if (appearance != null) {
            SoundEvent sound = appearance.getStepSound(this);
            if (sound != null) {
                playSound(sound, 0.15F, 1.0F);
                return;
            }
        }
        super.playStepSound(position, blockState);
    }

    public void setAppearanceContext(
            ResourceLocation appearanceId,
            float variationSeed,
            CustomerSpawnerMode spawnerMode,
            boolean special
    ) {
        setAppearanceId(appearanceId);
        setVariationSeed(variationSeed);
        setSpawnerMode(spawnerMode);
        setSpecial(special);
    }

    public CustomerState getState() {
        int syncedState = entityData.get(DATA_CUSTOMER_STATE);
        CustomerState[] states = CustomerState.values();
        if (syncedState >= 0 && syncedState < states.length) {
            return states[syncedState];
        }
        return state;
    }

    public CustomerSpawnerSnapshot.Customer.Type getSnapshotType() {
        VillagerData data = getVillagerData();
        if (VillagerCUtils.hasProfession(
                data,
                Customer.CUSTOMER_IMPATIENT_PROFESSION.getKey()
        )) {
            return CustomerSpawnerSnapshot.Customer.Type.IMPATIENT;
        }
        if (VillagerCUtils.hasProfession(
                data,
                Customer.CUSTOMER_CASUAL_PROFESSION.getKey()
        )) {
            return CustomerSpawnerSnapshot.Customer.Type.CASUAL;
        }
        return CustomerSpawnerSnapshot.Customer.Type.NORMAL;
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
            ItemStackCUtils.onCraftedBy(result, player, result.getCount());
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
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readCustomerData(PersistenceCUtils.reader(
                compound,
                registryAccess()
        ));
    }
    void readCustomerData(DataReader input) {
        readAppearanceData(input);
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
                .ifPresent(savedState -> counterBlockState = savedState);
        input.getBlockState(TAG_AVOID_BLOCK_STATE)
                .ifPresent(savedState -> avoidBlockState = savedState);
        tradedWithPlayers.clear();
        input.getChildren(TAG_TRADED_WITH_PLAYERS).forEach(
                tradedPlayerInput -> tradedPlayerInput
                        .getUuid(TAG_TRADED_PLAYER_UUID)
                        .ifPresent(tradedWithPlayers::add)
        );
        offersCrafted.clear();
        offersCrafted.addAll(input.getItemStacks(TAG_OFFERS_CRAFTED));
    }

    void readAppearanceData(DataReader input) {
        CustomersVillagerAppearancePersistence.read(input, this);
        input.getString(TAG_APPEARANCE_SPAWNER_MODE)
                .ifPresent(modeName -> {
                    try {
                        setSpawnerMode(CustomerSpawnerMode.valueOf(modeName));
                    } catch (IllegalArgumentException exception) {
                        LOGGER.warn(
                                "Ignoring unknown appearance spawner mode while loading: {}",
                                modeName
                        );
                    }
                });
        setSpecial(input.getBoolean(TAG_APPEARANCE_SPECIAL));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        writeCustomerData(PersistenceCUtils.writer(
                compound,
                registryAccess()
        ));
    }
    void writeCustomerData(DataWriter output) {
        CustomersVillagerAppearancePersistence.write(output, this);
        getSpawnerMode().ifPresent(spawnerMode ->
                output.putString(
                        TAG_APPEARANCE_SPAWNER_MODE,
                        spawnerMode.name()
                )
        );
        output.putBoolean(TAG_APPEARANCE_SPECIAL, isSpecial());
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
        output.putItemStacks(TAG_OFFERS_CRAFTED, offersCrafted);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean causeFallDamage(
            float fallDistance,
            float multiplier,
            DamageSource source
    ) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);

        goalSelector.addGoal(0, new FloatGoal(this));

        goalSelector.addGoal(0, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8));

        CustomerMoveToCounterGoal moveToCounterGoal =
                new CustomerMoveToCounterGoal(this, 0.5);
        goalSelector.addGoal(0, moveToCounterGoal);
        goalSelector.addGoal(
                0,
                new CustomerTakePickupItemGoal(this, moveToCounterGoal)
        );
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
            return InteractionCUtils.sidedSuccess(LevelCUtils.isClientSide(level()));
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
                    spawner.scoreboardAddItemsServed(
                            tradingPlayer.getUUID(),
                            offer.getCostA().getCount()
                    );
                }
            }
        }
    }

    private static void giveTradeRemainderItems(Player player, ItemStack soldStack) {
        ItemStack remainder = getTradeRemainderStack(soldStack);
        if (remainder.isEmpty()) {
            return;
        }

        if (!player.getInventory().add(remainder)) {
            player.drop(remainder, false);
        }
    }

    /**
     * Creates the empty containers returned after consuming a sold stack.
     *
     * @param soldStack consumed items
     * @return corresponding container stack, or an empty stack
     */
    static ItemStack getTradeRemainderStack(ItemStack soldStack) {
        ItemStack craftingRemainder =
                ItemStackCUtils.getCraftingRemainder(soldStack);
        if (!craftingRemainder.isEmpty()) {
            craftingRemainder.setCount(soldStack.getCount());
            return craftingRemainder;
        }

        Item fallbackItem = TRADE_REMAINDER_FALLBACKS.get(soldStack.getItem());
        return fallbackItem == null
                ? ItemStack.EMPTY
                : new ItemStack(fallbackItem, soldStack.getCount());
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

    public long getGiveUpTicks() {
        long giveUpTicks = 20L * Config.CUSTOMER_GIVE_UP_SECONDS.get();
        if (VillagerCUtils.hasProfession(
                getVillagerData(),
                Customer.CUSTOMER_IMPATIENT_PROFESSION.getKey()
        )) {
            giveUpTicks = Math.max(1, giveUpTicks / 2);
        } else if (VillagerCUtils.hasProfession(
                getVillagerData(),
                Customer.CUSTOMER_CASUAL_PROFESSION.getKey()
        )) {
            giveUpTicks = 0;
        }
        return giveUpTicks;
    }

    @Override
    public void tick() {
        super.tick();

        if (!LevelCUtils.isClientSide(level())) {
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
