package com.vikingkittens.mc.customers.customer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public final class CustomerPet {
    public static final TagKey<EntityType<?>> CAN_NOT_BE_PET = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, "can_not_be_pet")
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte ANIMAL_HEARTS_EVENT = 18;
    private static final float CUSTOMER_HEIGHT = 1.95F;
    private static final float MAX_PET_HEIGHT = CUSTOMER_HEIGHT / 2.0F;
    private static final double FOLLOW_SPEED = 0.75D;
    private static final float FOLLOW_START_DISTANCE = 5.0F;
    private static final float FOLLOW_STOP_DISTANCE = 2.0F;
    private static final double TELEPORT_DISTANCE_SQR = 144.0D;
    private static final Field GOAL_SELECTOR_FIELD = getSelectorField("goalSelector");
    private static final Field TARGET_SELECTOR_FIELD = getSelectorField("targetSelector");

    private static List<Pet> pets;

    private CustomerPet() {
    }

    public static @Nullable UUID spawnRandomPetFor(Level level, UUID customerId) {
        return spawnRandomPetFor(level, customerId, null);
    }

    public static @Nullable UUID spawnRandomPetFor(
            Level level,
            UUID customerId,
            @Nullable Collection<String> enabledPetIds
    ) {
        findAllPossiblePets(level);
        List<Pet> candidatePets = pets.stream()
                .filter(pet -> enabledPetIds == null || enabledPetIds.contains(pet.entityId))
                .toList();
        if (LevelCUtils.isClientSide(level) || !(level instanceof ServerLevel serverLevel) || candidatePets.isEmpty()) {
            return null;
        }

        CustomerVillagerEntity customer = CustomerVillagerEntity.getActiveCustomer(level, customerId);
        if (customer == null) {
            return null;
        }

        Pet petType = candidatePets.get(level.getRandom().nextInt(candidatePets.size()));
        Animal pet = petType.supplier.apply(level);
        if (pet == null) {
            return null;
        }

        EntityCUtils.snapTo(pet, customer.position(), customer.getYRot(), customer.getXRot());
        if (preparePet(pet, customerId)) {
            serverLevel.addFreshEntity(pet);
            return pet.getUUID();
        }
        return null;
    }

    public static boolean setupGoals(Level level, UUID petId, UUID customerId) {
        if (!(level instanceof ServerLevel serverLevel) ||
                !(serverLevel.getEntity(petId) instanceof Animal pet)) {
            return false;
        }
        return preparePet(pet, customerId);
    }

    public static void playLove(Level level, UUID petId) {
        if (level instanceof ServerLevel serverLevel &&
                serverLevel.getEntity(petId) instanceof Animal pet) {
            level.broadcastEntityEvent(pet, ANIMAL_HEARTS_EVENT);
        }
    }

    public static ItemStack getFoodForPet(Level level, UUID petId) {
        findAllPossiblePets(level);
        if (!(level instanceof ServerLevel serverLevel)) {
            return ItemStack.EMPTY;
        }

        Entity entity = serverLevel.getEntity(petId);
        if (!(entity instanceof Animal)) {
            return ItemStack.EMPTY;
        }

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return pets.stream()
                .filter(pet -> pet.entityId.equals(entityId))
                .findFirst()
                .map(pet -> pet.food.copy())
                .orElse(ItemStack.EMPTY);
    }

    public static List<PetType> getAvailablePetTypes(Level level) {
        findAllPossiblePets(level);
        return pets.stream()
                .map(pet -> new PetType(pet.entityId, pet.name))
                .toList();
    }

    public static List<String> getAvailablePetTypeIds(Level level) {
        return getAvailablePetTypes(level).stream()
                .map(PetType::entityId)
                .toList();
    }

    private static void findAllPossiblePets(Level level) {
        if (pets != null) {
            return;
        }
        pets = discoverPets(
                level,
                BuiltInRegistries.ENTITY_TYPE,
                BuiltInRegistries.ITEM,
                BuiltInRegistries.ENTITY_TYPE::getKey
        );
        pets.forEach(pet -> LOGGER.info(
                "Discovered customer pet {} with food {}",
                pet.entityId,
                BuiltInRegistries.ITEM.getKey(pet.food.getItem())
        ));
    }

    static List<Pet> discoverPets(
            Level level,
            Iterable<EntityType<?>> entityTypes,
            Iterable<Item> items,
            Function<EntityType<?>, ResourceLocation> getEntityId
    ) {
        List<Pet> discoveredPets = new ArrayList<>();
        for (EntityType<?> entityType : entityTypes) {
            if (entityType.is(CAN_NOT_BE_PET)) {
                continue;
            }
            Animal animal = createAnimalForDiscovery(entityType, level);
            if (animal == null) {
                continue;
            }
            Optional<ItemStack> food = findFood(animal, items);
            food.ifPresent(stack -> discoveredPets.add(new Pet(
                    getEntityId.apply(entityType).toString(),
                    entityType.getDescription(),
                    petLevel -> createConfiguredAnimal(entityType, petLevel),
                    stack
            )));
        }
        return List.copyOf(discoveredPets);
    }

    private static Animal createAnimalForDiscovery(EntityType<?> entityType, Level level) {
        try {
            return createAnimal(entityType, level);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static Optional<ItemStack> findFood(Animal animal, Iterable<Item> items) {
        for (Item item : items) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isEmpty() && animal.isFood(stack)) {
                return Optional.of(stack.copy());
            }
        }
        return Optional.empty();
    }

    private static Animal createConfiguredAnimal(EntityType<?> entityType, Level level) {
        Animal animal = createAnimal(entityType, level);
        if (animal != null) {
            configurePetSize(animal);
        }
        return animal;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Animal createAnimal(EntityType<?> entityType, Level level) {
        Entity entity = EntityCUtils.create((EntityType)entityType, level);
        return entity instanceof Animal animal ? animal : null;
    }

    private static void configurePetSize(Animal pet) {
        if (pet.getBbHeight() <= MAX_PET_HEIGHT) {
            return;
        }
        if (pet instanceof AgeableMob ageablePet) {
            ageablePet.setBaby(true);
            return;
        }

        AttributeInstance scale = pet.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(MAX_PET_HEIGHT / pet.getBbHeight());
            pet.refreshDimensions();
        }
    }

    private static boolean preparePet(Animal pet, UUID customerId) {
        GoalSelector goalSelector = getSelector(pet, GOAL_SELECTOR_FIELD);
        GoalSelector targetSelector = getSelector(pet, TARGET_SELECTOR_FIELD);
        if (goalSelector == null || targetSelector == null) {
            return false;
        }
        pet.setInvisible(true);
        pet.setNoAi(false);
        pet.noPhysics = true;
        pet.setTarget(null);
        pet.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        pet.getBrain().removeAllBehaviors();
        pet.getBrain().clearMemories();
        if (pet instanceof TamableAnimal tamablePet) {
            tamablePet.setTame(true, false);
            tamablePet.setOwnerUUID(customerId);
            tamablePet.setOrderedToSit(false);
        }
        goalSelector.addGoal(0, new FloatGoal(pet));
        goalSelector.addGoal(1, new LookAtPlayerGoal(pet, Player.class, 8.0F));
        goalSelector.addGoal(2, new FollowCustomerGoal(pet, customerId));
        return true;
    }

    private static Field getSelectorField(String name) {
        try {
            Field field = Mob.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static GoalSelector getSelector(Animal pet, Field field) {
        if (field == null) {
            return null;
        }
        try {
            return (GoalSelector)field.get(pet);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    static final class Pet {
        final String entityId;
        final Component name;
        final Function<Level, Animal> supplier;
        final ItemStack food;

        private Pet(String entityId, Component name, Function<Level, Animal> supplier, ItemStack food) {
            this.entityId = entityId;
            this.name = name;
            this.supplier = supplier;
            this.food = food.copy();
        }
    }

    public record PetType(String entityId, Component name) {}

    private static final class FollowCustomerGoal extends Goal {
        private final Animal pet;
        private final UUID customerId;
        private LivingEntity customer;
        private int timeToRecalculatePath;

        private FollowCustomerGoal(Animal pet, UUID customerId) {
            this.pet = pet;
            this.customerId = customerId;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return hasLivingCustomerOrDiscardPet() &&
                    pet.distanceToSqr(customer) >= FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE;
        }

        @Override
        public boolean canContinueToUse() {
            return hasLivingCustomerOrDiscardPet() &&
                    pet.distanceToSqr(customer) > FOLLOW_STOP_DISTANCE * FOLLOW_STOP_DISTANCE;
        }

        @Override
        public void stop() {
            customer = null;
            pet.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (!hasLivingCustomerOrDiscardPet()) {
                return;
            }
            pet.getLookControl().setLookAt(customer, 10.0F, pet.getMaxHeadXRot());
            if (pet.distanceToSqr(customer) >= TELEPORT_DISTANCE_SQR) {
                EntityCUtils.snapTo(pet, customer.position(), customer.getYRot(), customer.getXRot());
                pet.getNavigation().stop();
            } else if (--timeToRecalculatePath <= 0) {
                timeToRecalculatePath = adjustedTickDelay(10);
                pet.getNavigation().moveTo(customer, FOLLOW_SPEED);
            }
        }

        private boolean hasLivingCustomerOrDiscardPet() {
            customer = findCustomer();
            if (customer != null) {
                return true;
            }
            pet.discard();
            return false;
        }

        private LivingEntity findCustomer() {
            if (pet.level() instanceof ServerLevel serverLevel &&
                    serverLevel.getEntity(customerId) instanceof CustomerVillagerEntity customer &&
                    customer.isAlive()) {
                return customer;
            }
            return null;
        }
    }
}
