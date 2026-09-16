package com.vikingkittens.mc.customers.advancements.ftb;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.Customers;

public final class CustomersFTBTasks {
    public static final String PET_ITEMS_SERVED_NAME = "pet_items_served";

    public static final TaskType PET_ITEMS_SERVED = TaskTypes.register(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, PET_ITEMS_SERVED_NAME),
            PetItemsServedTask::new,
            () -> Icon.getIcon("minecraft:item/bone")
    ).setDisplayName(Component.translatable("ftbquests.task.customers.pet_items_served"));

    private CustomersFTBTasks() {}

    public static void initialize() {}

    public static final class PetItemsServedTask extends Task {
        private int requiredPetItems = 1;

        public PetItemsServedTask(long id, Quest quest) {
            super(id, quest);
        }

        @Override
        public TaskType getType() {
            return PET_ITEMS_SERVED;
        }

        @Override
        public long getMaxProgress() {
            return requiredPetItems;
        }

        void recordProgress(TeamData teamData) {
            if (teamData.isCompleted(this)
                    || !teamData.canStartTasks(getQuest())
                    || !checkTaskSequence(teamData)) {
                return;
            }
            teamData.addProgress(this, 1L);
        }

        @Override
        public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
            super.writeData(tag, provider);
            tag.putInt("count", requiredPetItems);
        }

        @Override
        public void readData(CompoundTag tag, HolderLookup.Provider provider) {
            super.readData(tag, provider);
            requiredPetItems = Math.max(1, tag.getInt("count"));
        }

        @Override
        public void writeNetData(RegistryFriendlyByteBuf buffer) {
            super.writeNetData(buffer);
            buffer.writeVarInt(requiredPetItems);
        }

        @Override
        public void readNetData(RegistryFriendlyByteBuf buffer) {
            super.readNetData(buffer);
            requiredPetItems = Math.max(1, buffer.readVarInt());
        }

        @Override
        public void fillConfigGroup(ConfigGroup config) {
            super.fillConfigGroup(config);
            config.addInt(
                    "count",
                    requiredPetItems,
                    value -> requiredPetItems = value,
                    1,
                    1,
                    Integer.MAX_VALUE
            ).setNameKey("ftbquests.task.customers.pet_items_served.count");
        }
    }
}
