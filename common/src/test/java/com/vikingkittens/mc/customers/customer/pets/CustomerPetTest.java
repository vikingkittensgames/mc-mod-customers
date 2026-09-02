package com.vikingkittens.mc.customers.customer.pets;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
                        ? ResourceLocation.withDefaultNamespace("test_pet")
                        : ResourceLocation.withDefaultNamespace("not_a_pet"),
                item -> 0
        );

        assertEquals(1, pets.size());
        assertEquals("minecraft:test_pet", pets.getFirst().entityId);
        assertEquals(List.of(Items.CARROT, Items.APPLE), pets.getFirst().foods.stream().map(ItemStack::getItem).toList());
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
                entityType -> ResourceLocation.withDefaultNamespace("test_pet"),
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
                entityType -> ResourceLocation.withDefaultNamespace("test_pet"),
                item -> item == Items.CARROT ? 10 : 20
        );

        assertSame(Items.CARROT, pets.getFirst().foods.getFirst().getItem());
    }

    @Test
    void discoversParrotFoodForFlyingAnimalsWithoutNormalFood() {
        Level level = mock(Level.class);
        EntityType<Animal> animalType = mock(EntityType.class);
        Animal animal = mock(Animal.class, withSettings().extraInterfaces(FlyingAnimal.class));
        Item parrotFoodItem = mock(Item.class);
        ItemStack parrotFoodStack = mock(ItemStack.class);
        when(animalType.create(level)).thenReturn(animal);
        when(animalType.getDescription()).thenReturn(Component.literal("Test Flying Pet"));
        when(parrotFoodItem.getDefaultInstance()).thenReturn(parrotFoodStack);
        when(parrotFoodStack.is(ItemTags.PARROT_FOOD)).thenReturn(true);
        when(parrotFoodStack.copy()).thenReturn(parrotFoodStack);

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                List.of(animalType),
                List.of(parrotFoodItem),
                entityType -> ResourceLocation.withDefaultNamespace("test_flying_pet"),
                item -> 0
        );

        assertTrue(animal instanceof FlyingAnimal);
        assertEquals(1, pets.size());
        assertEquals(List.of(parrotFoodStack), pets.getFirst().foods);
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
}
