package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerSpawnerLevelSettingsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void clampsRequiredStarsToHalfStarStepsAndValidRange() {
        assertEquals(0.5F, CustomerSpawnerLevelSettings.clampRequiredStars(0.0F));
        assertEquals(2.5F, CustomerSpawnerLevelSettings.clampRequiredStars(2.6F));
        assertEquals(5.0F, CustomerSpawnerLevelSettings.clampRequiredStars(6.0F));
    }

    @Test
    void clampsPetPercentageToValidRange() {
        assertEquals(0.0F, CustomerSpawnerLevelSettings.clampPetPercentage(-0.1F));
        assertEquals(0.35F, CustomerSpawnerLevelSettings.clampPetPercentage(0.35F));
        assertEquals(1.0F, CustomerSpawnerLevelSettings.clampPetPercentage(1.1F));
    }

    @Test
    void convertsPetPercentageToAndFromWholePercentValues() {
        assertEquals(0, CustomerSpawnerLevelSettings.petPercentageToPercent(-0.1F));
        assertEquals(35, CustomerSpawnerLevelSettings.petPercentageToPercent(0.35F));
        assertEquals(100, CustomerSpawnerLevelSettings.petPercentageToPercent(1.1F));
        assertEquals(0.0F, CustomerSpawnerLevelSettings.percentToPetPercentage(-1));
        assertEquals(0.35F, CustomerSpawnerLevelSettings.percentToPetPercentage(35));
        assertEquals(1.0F, CustomerSpawnerLevelSettings.percentToPetPercentage(101));
    }

    @Test
    void defaultsToAnEmptyInventoryAndDefaultCustomerSettings() {
        CustomerSpawnerLevelSettings settings = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);

        assertEquals(
                3.0F,
                settings.getRequiredStars()
        );
        assertEquals(4, settings.getMaxCustomers());
        assertEquals(0.0F, settings.getPetPercentage());
        assertEquals(
                List.of("minecraft:cat", "minecraft:wolf"),
                settings.getEnabledPetTypes(List.of("minecraft:cat", "minecraft:wolf"))
        );
        assertEquals(54, settings.getInventory().getContainerSize());
        assertEquals(
                List.of(CustomersVillagerAppearances.DEFAULT),
                settings.getEnabledAppearanceIds()
        );
    }

    @Test
    void keepsInventoryAndSettingsIndependentForEachLevel() {
        CustomerSpawnerLevelSettings first = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);
        CustomerSpawnerLevelSettings second = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);

        first.getInventory().setItem(0, Items.BREAD.getDefaultInstance());
        first.setMaxCustomers(7);
        first.setRequiredStars(4.5F);
        first.setPetPercentage(0.25F);
        first.setPetTypeEnabled("minecraft:cat", false, List.of("minecraft:cat", "minecraft:wolf"));

        assertEquals(Items.BREAD, first.getInventory().getItem(0).getItem());
        assertEquals(7, first.getMaxCustomers());
        assertEquals(4.5F, first.getRequiredStars());
        assertEquals(0.25F, first.getPetPercentage());
        assertEquals(List.of("minecraft:wolf"), first.getEnabledPetTypes(List.of("minecraft:cat", "minecraft:wolf")));
        assertEquals(0, second.getInventory().getItem(0).getCount());
        assertEquals(4, second.getMaxCustomers());
        assertEquals(3.0F, second.getRequiredStars());
        assertEquals(0.0F, second.getPetPercentage());
    }

    @Test
    void persistsEveryLevelSettingAndInventorySlot() {
        CustomerSpawnerLevelSettings saved = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);
        saved.setRequiredStars(2.5F);
        saved.setMaxCustomers(7);
        saved.setPetPercentage(0.35F);
        saved.setPetTypeEnabled("minecraft:cat", false, List.of("minecraft:cat", "minecraft:wolf"));
        saved.getInventory().setItem(0, new ItemStack(Items.BREAD, 2));
        saved.getInventory().setItem(52, new ItemStack(Items.CARROT, 3));

        CompoundTag tag = new CompoundTag();
        saved.write(PersistenceCUtils.writer(tag, RegistryAccess.EMPTY));

        CustomerSpawnerLevelSettings loaded = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);
        loaded.read(PersistenceCUtils.reader(tag, RegistryAccess.EMPTY));

        assertEquals(2.5F, loaded.getRequiredStars());
        assertEquals(7, loaded.getMaxCustomers());
        assertEquals(0.35F, loaded.getPetPercentage());
        assertEquals(List.of("minecraft:wolf"), loaded.getEnabledPetTypes(List.of("minecraft:cat", "minecraft:wolf")));
        assertEquals(Items.BREAD, loaded.getInventory().getItem(0).getItem());
        assertEquals(2, loaded.getInventory().getItem(0).getCount());
        assertEquals(Items.CARROT, loaded.getInventory().getItem(52).getItem());
        assertEquals(3, loaded.getInventory().getItem(52).getCount());
    }

    @Test
    void persistsCustomizedEmptyPetTypesAsNoPetTypesEnabled() {
        CustomerSpawnerLevelSettings saved = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);
        saved.setPetTypeEnabled("minecraft:cat", false, List.of("minecraft:cat"));

        CompoundTag tag = new CompoundTag();
        saved.write(PersistenceCUtils.writer(tag, RegistryAccess.EMPTY));

        CustomerSpawnerLevelSettings loaded = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);
        loaded.read(PersistenceCUtils.reader(tag, RegistryAccess.EMPTY));

        assertEquals(List.of(), loaded.getEnabledPetTypes(List.of("minecraft:cat")));
    }
}
