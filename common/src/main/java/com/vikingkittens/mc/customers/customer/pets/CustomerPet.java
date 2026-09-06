package com.vikingkittens.mc.customers.customer.pets;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.EntityCUtils;
import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import com.vikingkittens.mc.customers.customer.pets.ai.CustomerPetFollowCustomerGoal;
import com.vikingkittens.mc.customers.customer.pets.ai.CustomerPetSitNextToCustomerGoal;
import com.vikingkittens.mc.customers.customer.pets.ai.CustomerPetSitWhenOrderedToGoal;

public final class CustomerPet {
    public static final TagKey<EntityType<?>> CAN_NOT_BE_PET = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Customers.MODID, "can_not_be_pet")
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte ANIMAL_HEARTS_EVENT = 18;
    private static final float CUSTOMER_HEIGHT = 1.95F;
    private static final float MAX_PET_HEIGHT = CUSTOMER_HEIGHT * 0.5F;
    private static final Field GOAL_SELECTOR_FIELD = getSelectorField("goalSelector");
    private static final Field TARGET_SELECTOR_FIELD = getSelectorField("targetSelector");

    private static List<Pet> pets;

    private CustomerPet() {
    }

    public static @Nullable SpawnedPet spawnRandomPetFor(Level level, UUID customerId) {
        return spawnRandomPetFor(level, customerId, null);
    }

    public static @Nullable SpawnedPet spawnRandomPetFor(
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
            return new SpawnedPet(pet.getUUID(), petType.entityId);
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

    public static void discard(Level level, UUID petId) {
        if (level instanceof ServerLevel serverLevel &&
                serverLevel.getEntity(petId) instanceof Animal pet) {
            pet.discard();
        }
    }

    public static @Nullable String getPetTypeId(Level level, UUID petId) {
        findAllPossiblePets(level);
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(petId);
        if (!(entity instanceof Animal)) {
            return null;
        }

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return pets.stream().anyMatch(pet -> pet.entityId.equals(entityId)) ? entityId : null;
    }

    public static List<PetType> getAvailablePetTypes(Level level) {
        findAllPossiblePets(level);
        return pets.stream()
                .map(pet -> new PetType(pet.entityId, pet.name, pet.foods))
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
                BuiltInRegistries.ENTITY_TYPE::getKey,
                BuiltInRegistries.ITEM::getId
        );
        pets.forEach(pet -> LOGGER.info(
                "Discovered customer pet {} with foods {}",
                pet.entityId,
                pet.foods.stream().map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem())).toList()
        ));
    }

    static List<Pet> discoverPets(
            Level level,
            Iterable<EntityType<?>> entityTypes,
            Iterable<Item> items,
            Function<EntityType<?>, Identifier> getEntityId,
            ToIntFunction<Item> getItemId
    ) {
        List<Pet> discoveredPets = new ArrayList<>();
        List<Item> sortedItems = StreamSupport.stream(items.spliterator(), false)
                .sorted(Comparator.comparingInt(getItemId))
                .toList();
        for (EntityType<?> entityType : entityTypes) {
            if (entityType.is(CAN_NOT_BE_PET)) {
                continue;
            }
            Animal animal = createAnimalForDiscovery(entityType, level);
            if (animal == null) {
                continue;
            }
            List<ItemStack> foods = findFoods(animal, sortedItems);
            if (!foods.isEmpty()) {
                discoveredPets.add(new Pet(
                        getEntityId.apply(entityType).toString(),
                        entityType.getDescription(),
                        petLevel -> createAnimal(entityType, petLevel),
                        foods
                ));
            }
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

    private static List<ItemStack> findFoods(Animal animal, List<Item> items) {
        List<ItemStack> foods = new ArrayList<>();
        for (Item item : items) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isEmpty() && animal.isFood(stack)) {
                foods.add(stack.copy());
            }
        }
        if (foods.isEmpty() && animal instanceof FlyingAnimal) {
            for (Item item : items) {
                ItemStack stack = item.getDefaultInstance();
                if (!stack.isEmpty() && stack.is(ItemTags.PARROT_FOOD)) {
                    foods.add(stack.copy());
                }
            }
        }
        return List.copyOf(foods);
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
        if (pet.level() instanceof ServerLevel serverLevel && serverLevel.getEntity(pet.getUUID()) != pet) {
            pet.finalizeSpawn(
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(pet.blockPosition()),
                    EntitySpawnReason.EVENT,
                    null
            );
            configurePetSize(pet);
        }
        pet.setInvulnerable(true);
        pet.setNoAi(false);
        pet.noPhysics = false;
        pet.setTarget(null);
        pet.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        pet.getBrain().removeAllBehaviors();
        pet.getBrain().clearMemories();
        if (pet instanceof TamableAnimal tamablePet) {
            tamablePet.setTame(true, false);
            tamablePet.setOwnerReference(EntityReference.of(customerId));
            tamablePet.setOrderedToSit(false);
        }
        goalSelector.addGoal(0, new FloatGoal(pet));
        goalSelector.addGoal(1, new CustomerPetSitNextToCustomerGoal(pet, customerId));
        if (pet instanceof TamableAnimal tamablePet) {
            goalSelector.addGoal(2, new CustomerPetSitWhenOrderedToGoal(tamablePet, customerId));
        }
        goalSelector.addGoal(3, new CustomerPetFollowCustomerGoal(pet, customerId));
        goalSelector.addGoal(4, new LookAtPlayerGoal(pet, Player.class, 8.0F));
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
        final List<ItemStack> foods;

        private Pet(String entityId, Component name, Function<Level, Animal> supplier, List<ItemStack> foods) {
            this.entityId = entityId;
            this.name = name;
            this.supplier = supplier;
            this.foods = foods.stream().map(ItemStack::copy).toList();
        }
    }

    public record PetType(String entityId, Component name, List<ItemStack> foods) {
        public PetType {
            foods = foods.stream().map(ItemStack::copy).toList();
        }

        public ItemStack getFood(@Nullable ItemStack selectedFood) {
            return foods.stream()
                    .filter(food -> selectedFood != null && ItemStackCUtils.isSameItemAndTags(food, selectedFood))
                    .findFirst()
                    .map(ItemStack::copy)
                    .orElseGet(() -> foods.getFirst().copy());
        }

        @Override public List<ItemStack> foods() { return foods.stream().map(ItemStack::copy).toList(); }
    }

    public record SpawnedPet(UUID id, String petTypeId) {}

}
