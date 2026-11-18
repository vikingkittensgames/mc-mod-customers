package com.vikingkittens.mc.customers.customer.pets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;
import com.vikingkittens.mc.customers.customer.pets.mixin.CustomerPetMobAccessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CustomerPetTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void discoversTenAnimalsPerTickWithoutMissingOrDuplicatingPetsOrFoods() {
        Level level = mock(Level.class);
        List<EntityType<?>> animalTypes = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            EntityType<Animal> type = mock(EntityType.class);
            Animal animal = mock(Animal.class);
            when(type.create(level)).thenReturn(animal);
            when(type.getDescription()).thenReturn(Component.literal("Pet " + index));
            when(animal.isFood(any(ItemStack.class))).thenReturn(true);
            animalTypes.add(type);
        }
        CustomerPet.PetDiscovery discovery = new CustomerPet.PetDiscovery(
                level,
                animalTypes,
                List.of(Items.CARROT, Items.APPLE),
                type -> ResourceLocationCUtils.parse("test_pet_" + animalTypes.indexOf(type)),
                item -> item == Items.CARROT ? 0 : 1
        );

        discovery.advance(10, 500);
        assertFalse(discovery.isComplete());
        assertEquals(10, discovery.result().size());
        discovery.advance(10, 500);

        assertTrue(discovery.isComplete());
        assertEquals(
                java.util.stream.IntStream.range(0, 12).mapToObj(index -> "minecraft:test_pet_" + index).toList(),
                discovery.result().stream().map(pet -> pet.entityId).toList()
        );
        discovery.result().forEach(pet -> assertEquals(
                List.of(Items.CARROT, Items.APPLE),
                pet.foods.stream().map(ItemStack::getItem).toList()
        ));
    }

    @Test
    void resumesFoodSearchAcrossTicksWithoutMissingOrDuplicatingFoods() {
        Level level = mock(Level.class);
        List<Item> items = BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .limit(501)
                .toList();
        List<EntityType<?>> animalTypes = new ArrayList<>();
        for (int index = 0; index < 2; index++) {
            EntityType<Animal> type = mock(EntityType.class);
            Animal animal = mock(Animal.class);
            when(type.create(level)).thenReturn(animal);
            when(type.getDescription()).thenReturn(Component.literal("Pet " + index));
            when(animal.isFood(any(ItemStack.class))).thenAnswer(invocation -> {
                Item item = invocation.getArgument(0, ItemStack.class).getItem();
                return item == items.get(0) || item == items.get(500);
            });
            animalTypes.add(type);
        }
        CustomerPet.PetDiscovery discovery = new CustomerPet.PetDiscovery(
                level,
                animalTypes,
                items,
                type -> ResourceLocationCUtils.parse("test_pet_" + animalTypes.indexOf(type)),
                items::indexOf
        );

        discovery.advance(10, 500);
        assertFalse(discovery.isComplete());
        assertTrue(discovery.result().isEmpty());
        discovery.advance(10, 500);
        assertEquals(1, discovery.result().size());
        discovery.advance(10, 500);

        assertTrue(discovery.isComplete());
        assertEquals(2, discovery.result().size());
        discovery.result().forEach(pet -> assertEquals(
                List.of(items.get(0), items.get(500)),
                pet.foods.stream().map(ItemStack::getItem).toList()
        ));
    }

    @Test
    void discoversAnimalsWithAllAcceptedFoodItems() {
        Level level = mock(Level.class);
        EntityType<Entity> nonAnimalType = mock(EntityType.class);
        EntityType<Animal> animalType = mock(EntityType.class);
        Entity nonAnimal = mock(Entity.class);
        Animal animal = mock(Animal.class);
        when(nonAnimalType.create(level)).thenReturn(nonAnimal);
        when(animalType.create(level)).thenReturn(animal);
        when(animalType.getDescription()).thenReturn(Component.literal("Test Pet"));
        when(animal.isFood(any(ItemStack.class))).thenAnswer(invocation -> {
            ItemStack food = invocation.getArgument(0, ItemStack.class);
            return food.is(Items.CARROT) || food.is(Items.APPLE);
        });

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                List.of(nonAnimalType, animalType),
                List.of(Items.WHEAT, Items.CARROT, Items.APPLE),
                entityType -> entityType == animalType
                        ? ResourceLocationCUtils.parse("test_pet")
                        : ResourceLocationCUtils.parse("not_a_pet"),
                item -> 0
        );

        assertEquals(1, pets.size());
        assertEquals("minecraft:test_pet", pets.get(0).entityId);
        assertEquals(List.of(Items.CARROT, Items.APPLE), pets.get(0).foods.stream().map(ItemStack::getItem).toList());
    }

    @Test
    void skipsEntityTypesInCanNotBePetTag() {
        Level level = mock(Level.class);
        EntityType<Animal> animalType = mock(EntityType.class);
        when(animalType.is(CustomerPet.CAN_NOT_BE_PET)).thenReturn(true);

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                List.of(animalType),
                List.of(Items.CARROT),
                entityType -> ResourceLocationCUtils.parse("test_pet"),
                item -> 0
        );

        assertTrue(pets.isEmpty());
        verify(animalType, never()).create(level);
    }

    @Test
    void printsDiscoveredPetsAndFoodFromRegistries() {
        Level level = mock(Level.class);
        when(level.getProfilerSupplier()).thenReturn(() -> InactiveProfiler.INSTANCE);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                BuiltInRegistries.ENTITY_TYPE,
                BuiltInRegistries.ITEM,
                BuiltInRegistries.ENTITY_TYPE::getKey,
                BuiltInRegistries.ITEM::getId
        );

        pets.forEach(pet -> System.out.println(pet.entityId + " -> " + pet.foods.stream()
                .map(food -> BuiltInRegistries.ITEM.getKey(food.getItem()))
                .toList()));
    }

    @Test
    void choosesTheLowestRegistryIdFromAcceptedFoodItems() {
        Level level = mock(Level.class);
        EntityType<Animal> animalType = mock(EntityType.class);
        Animal animal = mock(Animal.class);
        when(animalType.create(level)).thenReturn(animal);
        when(animalType.getDescription()).thenReturn(Component.literal("Test Pet"));
        when(animal.isFood(any(ItemStack.class))).thenReturn(true);

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                List.of(animalType),
                List.of(Items.APPLE, Items.CARROT),
                entityType -> ResourceLocationCUtils.parse("test_pet"),
                item -> item == Items.CARROT ? 10 : 20
        );

        assertSame(Items.CARROT, pets.get(0).foods.get(0).getItem());
    }

    @Test
    void discoversParrotFoodForFlyingAnimalsWithoutNormalFood() {
        Level level = mock(Level.class);
        EntityType<Animal> animalType = mock(EntityType.class);
        Animal animal = mock(Animal.class, withSettings().extraInterfaces(FlyingAnimal.class));
        when(animalType.create(level)).thenReturn(animal);
        when(animalType.getDescription()).thenReturn(Component.literal("Test Flying Pet"));

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                List.of(animalType),
                List.of(Items.WHEAT_SEEDS),
                entityType -> ResourceLocationCUtils.parse("test_flying_pet"),
                item -> 0
        );

        assertTrue(animal instanceof FlyingAnimal);
        assertEquals(1, pets.size());
        assertEquals(1, pets.get(0).foods.size());
        assertSame(Items.WHEAT_SEEDS, pets.get(0).foods.get(0).getItem());
    }

    @Test
    void discardsTrackedAnimalPets() {
        ServerLevel level = mock(ServerLevel.class);
        UUID petId = UUID.randomUUID();
        Animal pet = mock(Animal.class);
        when(level.getEntity(petId)).thenReturn(pet);

        CustomerPet.discard(level, petId);

        verify(pet).discard();
    }

    @Test
    void updatesTrackedPetInvulnerability() {
        ServerLevel level = mock(ServerLevel.class);
        UUID petId = UUID.randomUUID();
        Animal pet = mock(Animal.class);
        when(level.getEntity(petId)).thenReturn(pet);

        assertTrue(CustomerPet.updateInvulnerability(level, petId, true));
        assertTrue(CustomerPet.updateInvulnerability(level, petId, false));

        verify(pet).setInvulnerable(true);
        verify(pet).setInvulnerable(false);
    }

    @Test
    void setsUpGoalsThroughRemappedMixinAccessor() {
        ServerLevel level = mock(ServerLevel.class);
        UUID petId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Animal pet = mock(
                Animal.class,
                withSettings().extraInterfaces(CustomerPetMobAccessor.class)
        );
        GoalSelector goalSelector = mock(GoalSelector.class);
        GoalSelector targetSelector = mock(GoalSelector.class);
        Brain<?> brain = mock(Brain.class);
        when(level.getEntity(petId)).thenReturn(pet);
        when(pet.level()).thenReturn(level);
        when(pet.getUUID()).thenReturn(petId);
        when(pet.getNavigation()).thenReturn(mock(PathNavigation.class));
        doReturn(brain).when(pet).getBrain();
        when(((CustomerPetMobAccessor)pet).customers$getGoalSelector()).thenReturn(goalSelector);
        when(((CustomerPetMobAccessor)pet).customers$getTargetSelector()).thenReturn(targetSelector);

        assertTrue(CustomerPet.setupGoals(level, petId, customerId));
    }
}
