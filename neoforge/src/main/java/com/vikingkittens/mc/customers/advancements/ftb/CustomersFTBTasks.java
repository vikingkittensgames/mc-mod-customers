package com.vikingkittens.mc.customers.advancements.ftb;

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
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerShiftFinished;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;

public final class CustomersFTBTasks {
    public static final String CUSTOMERS_TASK_NAME = "customers_task";
    private static final int REQUIRED_EVENTS_CONFIG_ORDER = 100;

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
        ITEM_SERVED(
                "item_served",
                "minecraft:item/cooked_chicken",
                CustomerInternalEvents.ItemServed.class
        ),
        CUSTOMER_SERVED(
                "customer_served",
                "customers:textures/item/advancement_icon_customer.png",
                CustomerInternalEvents.CustomerServed.class
        ),
        SHIFT_FINISHED(
                "shift_finished",
                "customers:textures/item/advancement_icon_star.png",
                CustomerInternalEvents.ShiftFinished.class
        );

        private static final NameMap<CustomersTaskKind> NAME_MAP = NameMap.of(ITEM_SERVED, values())
                .id(CustomersTaskKind::serializedName)
                .baseNameKey("ftbquests.task.customers.customers_task")
                .create();

        private final String serializedName;
        private final String icon;
        private final Class<? extends CustomerInternalEvents.CustomerEvent> eventType;

        CustomersTaskKind(
                String serializedName,
                String icon,
                Class<? extends CustomerInternalEvents.CustomerEvent> eventType
        ) {
            this.serializedName = serializedName;
            this.icon = icon;
            this.eventType = eventType;
        }

        public String serializedName() { return serializedName; }

        public Component displayName() { return NAME_MAP.getDisplayName(this); }

        public Icon icon() { return Icon.getIcon(icon); }

        String iconResource() { return icon; }

        boolean handles(CustomerInternalEvents.CustomerEvent event) { return eventType.isInstance(event); }
    }

    public static final class CustomersTask extends Task {
        private CustomersTaskKind taskKind = CustomersTaskKind.ITEM_SERVED;
        private int requiredEvents = 1;
        private CustomersTriggerItemServed.Instance itemServedTrigger = CustomersTriggerItemServed.Instance.ANY;
        private CustomersTriggerCustomerServed.Instance customerServedTrigger =
                CustomersTriggerCustomerServed.Instance.ANY;
        private CustomersTriggerShiftFinished.Instance shiftFinishedTrigger = CustomersTriggerShiftFinished.Instance.ANY;

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
            if (matches(event)) {
                recordProgress(teamData);
            }
        }

        void recordProgress(TeamData teamData, CustomerInternalEvents.CustomerServed event) {
            if (matches(event)) {
                recordProgress(teamData);
            }
        }

        void recordProgress(TeamData teamData, CustomerInternalEvents.ShiftFinished event) {
            if (matches(event)) {
                recordProgress(teamData);
            }
        }

        private void recordProgress(TeamData teamData) {
            if (teamData.isCompleted(this)
                    || !teamData.canStartTasks(getQuest())
                    || !checkTaskSequence(teamData)) {
                return;
            }
            teamData.addProgress(this, 1L);
        }

        boolean matches(CustomerInternalEvents.ItemServed event) {
            return taskKind.handles(event) && itemServedTrigger.matchesEvent(event);
        }

        boolean matches(CustomerInternalEvents.CustomerServed event) {
            return taskKind.handles(event) && customerServedTrigger.matchesEvent(event);
        }

        boolean matches(CustomerInternalEvents.ShiftFinished event) {
            return taskKind.handles(event) && shiftFinishedTrigger.matchesEvent(event);
        }

        @Override
        public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
            super.writeData(tag, provider);
            tag.putString("customers_task", taskKind.serializedName());
            tag.putInt("count", requiredEvents);
            switch (taskKind) {
                case ITEM_SERVED -> CustomersFTBTriggerSchema.writeData(
                        tag,
                        provider,
                        CustomersTriggerItemServed.SCHEMA,
                        itemServedTrigger
                );
                case CUSTOMER_SERVED -> CustomersFTBTriggerSchema.writeData(
                        tag,
                        provider,
                        CustomersTriggerCustomerServed.SCHEMA,
                        customerServedTrigger
                );
                case SHIFT_FINISHED -> CustomersFTBTriggerSchema.writeData(
                        tag,
                        provider,
                        CustomersTriggerShiftFinished.SCHEMA,
                        shiftFinishedTrigger
                );
            }
        }

        @Override
        public void readData(CompoundTag tag, HolderLookup.Provider provider) {
            super.readData(tag, provider);
            taskKind = CustomersTaskKind.NAME_MAP.get(tag.getString("customers_task"));
            requiredEvents = Math.max(1, tag.getInt("count"));
            switch (taskKind) {
                case ITEM_SERVED -> itemServedTrigger = CustomersFTBTriggerSchema.readData(
                        tag,
                        provider,
                        CustomersTriggerItemServed.SCHEMA,
                        CustomersTriggerItemServed.Instance.ANY
                );
                case CUSTOMER_SERVED -> customerServedTrigger = CustomersFTBTriggerSchema.readData(
                        tag,
                        provider,
                        CustomersTriggerCustomerServed.SCHEMA,
                        CustomersTriggerCustomerServed.Instance.ANY
                );
                case SHIFT_FINISHED -> shiftFinishedTrigger = CustomersFTBTriggerSchema.readData(
                        tag,
                        provider,
                        CustomersTriggerShiftFinished.SCHEMA,
                        CustomersTriggerShiftFinished.Instance.ANY
                );
            }
        }

        @Override
        public void writeNetData(RegistryFriendlyByteBuf buffer) {
            super.writeNetData(buffer);
            CustomersTaskKind.NAME_MAP.write(buffer, taskKind);
            buffer.writeVarInt(requiredEvents);
            switch (taskKind) {
                case ITEM_SERVED -> CustomersFTBTriggerSchema.writeNetData(
                        buffer,
                        CustomersTriggerItemServed.SCHEMA,
                        itemServedTrigger
                );
                case CUSTOMER_SERVED -> CustomersFTBTriggerSchema.writeNetData(
                        buffer,
                        CustomersTriggerCustomerServed.SCHEMA,
                        customerServedTrigger
                );
                case SHIFT_FINISHED -> CustomersFTBTriggerSchema.writeNetData(
                        buffer,
                        CustomersTriggerShiftFinished.SCHEMA,
                        shiftFinishedTrigger
                );
            }
        }

        @Override
        public void readNetData(RegistryFriendlyByteBuf buffer) {
            super.readNetData(buffer);
            taskKind = CustomersTaskKind.NAME_MAP.read(buffer);
            requiredEvents = Math.max(1, buffer.readVarInt());
            switch (taskKind) {
                case ITEM_SERVED -> itemServedTrigger =
                        CustomersFTBTriggerSchema.readNetData(buffer, CustomersTriggerItemServed.SCHEMA);
                case CUSTOMER_SERVED -> customerServedTrigger =
                        CustomersFTBTriggerSchema.readNetData(buffer, CustomersTriggerCustomerServed.SCHEMA);
                case SHIFT_FINISHED -> shiftFinishedTrigger =
                        CustomersFTBTriggerSchema.readNetData(buffer, CustomersTriggerShiftFinished.SCHEMA);
            }
        }

        @Override
        public void fillConfigGroup(ConfigGroup config) {
            super.fillConfigGroup(config);
            config.addInt("count", requiredEvents, value -> requiredEvents = value, 1, 1, Integer.MAX_VALUE)
                    .setNameKey("ftbquests.task.customers.customers_task.count")
                    .setOrder(REQUIRED_EVENTS_CONFIG_ORDER);
            switch (taskKind) {
                case ITEM_SERVED -> CustomersFTBTriggerSchema.fillConfigGroup(
                        config,
                        CustomersTriggerItemServed.SCHEMA,
                        () -> itemServedTrigger,
                        value -> itemServedTrigger = value
                );
                case CUSTOMER_SERVED -> CustomersFTBTriggerSchema.fillConfigGroup(
                        config,
                        CustomersTriggerCustomerServed.SCHEMA,
                        () -> customerServedTrigger,
                        value -> customerServedTrigger = value
                );
                case SHIFT_FINISHED -> CustomersFTBTriggerSchema.fillConfigGroup(
                        config,
                        CustomersTriggerShiftFinished.SCHEMA,
                        () -> shiftFinishedTrigger,
                        value -> shiftFinishedTrigger = value
                );
            }
        }
    }
}
