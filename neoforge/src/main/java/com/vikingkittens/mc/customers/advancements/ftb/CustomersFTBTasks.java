package com.vikingkittens.mc.customers.advancements.ftb;

import java.util.Optional;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
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
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;

public final class CustomersFTBTasks {
    public static final String CUSTOMERS_TASK_NAME = "customers_task";

    public static final TaskType CUSTOMERS_TASK = TaskTypes.register(
            ResourceLocation.fromNamespaceAndPath(Customers.MODID, CUSTOMERS_TASK_NAME),
            CustomersTask::new,
            () -> Icon.getIcon("minecraft:item/emerald")
    ).setDisplayName(Component.translatable("ftbquests.task.customers.customers_task"));

    private CustomersFTBTasks() {}

    public static void initialize() {}

    public static CustomersTask createTask(Quest quest, CustomersTaskKind taskKind) {
        return new CustomersTask(0L, quest, taskKind);
    }

    public enum CustomersTaskKind {
        PET_ITEMS_SERVED("pet_items_served", "minecraft:item/bone"),
        ITEM_SERVED("item_served", "minecraft:item/cooked_chicken");

        private static final NameMap<CustomersTaskKind> NAME_MAP = NameMap.of(PET_ITEMS_SERVED, values())
                .id(CustomersTaskKind::serializedName)
                .baseNameKey("ftbquests.task.customers.customers_task")
                .create();

        private final String serializedName;
        private final String icon;

        CustomersTaskKind(String serializedName, String icon) {
            this.serializedName = serializedName;
            this.icon = icon;
        }

        public String serializedName() { return serializedName; }

        public Component displayName() { return NAME_MAP.getDisplayName(this); }

        public Icon icon() { return Icon.getIcon(icon); }
    }

    public static final class CustomersTask extends Task {
        private CustomersTaskKind taskKind = CustomersTaskKind.PET_ITEMS_SERVED;
        private int requiredEvents = 1;
        private CustomersTriggerItemServed.Instance trigger = CustomersTriggerItemServed.Instance.ANY;

        public CustomersTask(long id, Quest quest) {
            super(id, quest);
        }

        private CustomersTask(long id, Quest quest, CustomersTaskKind taskKind) {
            this(id, quest);
            this.taskKind = taskKind;
        }

        @Override
        public TaskType getType() {
            return CUSTOMERS_TASK;
        }

        @Override
        public long getMaxProgress() {
            return requiredEvents;
        }

        void recordProgress(TeamData teamData, CustomerInternalEvents.ItemServed event) {
            if (teamData.isCompleted(this)
                    || !teamData.canStartTasks(getQuest())
                    || !checkTaskSequence(teamData)
                    || !matches(event)) {
                return;
            }
            teamData.addProgress(this, 1L);
        }

        private boolean matches(CustomerInternalEvents.ItemServed event) {
            CustomersTriggerItemServed.Instance matchingTrigger = taskKind == CustomersTaskKind.PET_ITEMS_SERVED
                    ? CustomersTriggerItemServed.SCHEMA.with(
                            trigger,
                            CustomersTriggerItemServed.SCHEMA.property("is_pet_item").orElseThrow(),
                            Optional.of(true)
                    )
                    : trigger;
            return matchingTrigger.matchesEvent(event);
        }

        @Override
        public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
            super.writeData(tag, provider);
            tag.putString("customers_task", taskKind.serializedName());
            tag.putInt("count", requiredEvents);
            CustomersFTBTriggerSchema.writeData(tag, provider, CustomersTriggerItemServed.SCHEMA, trigger);
        }

        @Override
        public void readData(CompoundTag tag, HolderLookup.Provider provider) {
            super.readData(tag, provider);
            taskKind = CustomersTaskKind.NAME_MAP.get(tag.getString("customers_task"));
            requiredEvents = Math.max(1, tag.getInt("count"));
            trigger = CustomersFTBTriggerSchema.readData(
                    tag,
                    provider,
                    CustomersTriggerItemServed.SCHEMA,
                    CustomersTriggerItemServed.Instance.ANY
            );
        }

        @Override
        public void writeNetData(RegistryFriendlyByteBuf buffer) {
            super.writeNetData(buffer);
            CustomersTaskKind.NAME_MAP.write(buffer, taskKind);
            buffer.writeVarInt(requiredEvents);
            CustomersFTBTriggerSchema.writeNetData(buffer, CustomersTriggerItemServed.SCHEMA, trigger);
        }

        @Override
        public void readNetData(RegistryFriendlyByteBuf buffer) {
            super.readNetData(buffer);
            taskKind = CustomersTaskKind.NAME_MAP.read(buffer);
            requiredEvents = Math.max(1, buffer.readVarInt());
            trigger = CustomersFTBTriggerSchema.readNetData(buffer, CustomersTriggerItemServed.SCHEMA);
        }

        @Override
        public void fillConfigGroup(ConfigGroup config) {
            super.fillConfigGroup(config);
            config.addInt("count", requiredEvents, value -> requiredEvents = value, 1, 1, Integer.MAX_VALUE)
                    .setNameKey("ftbquests.task.customers.customers_task.count");
            if (taskKind != CustomersTaskKind.ITEM_SERVED) {
                return;
            }
            CustomersFTBTriggerSchema.fillConfigGroup(
                    config,
                    CustomersTriggerItemServed.SCHEMA,
                    () -> trigger,
                    value -> trigger = value
            );
        }
    }
}
