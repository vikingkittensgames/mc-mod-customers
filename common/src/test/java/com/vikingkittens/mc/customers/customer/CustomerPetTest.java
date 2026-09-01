package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.flag.FeatureFlags;
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

class CustomerPetTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void discoversAnimalsWithTheFirstAcceptedFoodItem() {
        Level level = mock(Level.class);
        EntityType<Entity> nonAnimalType = mock(EntityType.class);
        EntityType<Animal> animalType = mock(EntityType.class);
        Entity nonAnimal = mock(Entity.class);
        Animal animal = mock(Animal.class);
        when(nonAnimalType.create(level)).thenReturn(nonAnimal);
        when(animalType.create(level)).thenReturn(animal);
        when(animalType.getDescription()).thenReturn(Component.literal("Test Pet"));
        when(animal.isFood(any(ItemStack.class))).thenAnswer(invocation ->
                invocation.getArgument(0, ItemStack.class).is(Items.CARROT)
        );

        List<CustomerPet.Pet> pets = CustomerPet.discoverPets(
                level,
                List.of(nonAnimalType, animalType),
                List.of(Items.WHEAT, Items.CARROT, Items.APPLE),
                entityType -> entityType == animalType
                        ? ResourceLocation.withDefaultNamespace("test_pet")
                        : ResourceLocation.withDefaultNamespace("not_a_pet")
        );

        assertEquals(1, pets.size());
        assertEquals("minecraft:test_pet", pets.getFirst().entityId);
        assertSame(Items.CARROT, pets.getFirst().food.getItem());
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
                entityType -> ResourceLocation.withDefaultNamespace("test_pet")
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
                BuiltInRegistries.ENTITY_TYPE::getKey
        );

        pets.forEach(pet -> System.out.println(pet.entityId + " -> " + BuiltInRegistries.ITEM.getKey(pet.food.getItem())));
    }
}
