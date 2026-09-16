package com.vikingkittens.mc.customers.advancements.ftb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSchema;

final class CustomersFTBTriggerSchema {
    private static final String TRIGGER_DATA_KEY = "trigger";
    private static final Logger LOGGER = LogUtils.getLogger();

    private CustomersFTBTriggerSchema() {}

    static <T> void writeData(
            CompoundTag tag,
            HolderLookup.Provider provider,
            CustomersTriggerSchema<T> schema,
            T trigger
    ) {
        schema.codec()
                .encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), trigger)
                .resultOrPartial(message -> LOGGER.error("Unable to encode FTB trigger task: {}", message))
                .ifPresent(value -> tag.put(TRIGGER_DATA_KEY, value));
    }

    static <T> T readData(
            CompoundTag tag,
            HolderLookup.Provider provider,
            CustomersTriggerSchema<T> schema,
            T defaultTrigger
    ) {
        Tag triggerTag = tag.get(TRIGGER_DATA_KEY);
        if (triggerTag == null) {
            return defaultTrigger;
        }
        return schema.codec()
                .parse(provider.createSerializationContext(NbtOps.INSTANCE), triggerTag)
                .resultOrPartial(message -> LOGGER.error("Unable to decode FTB trigger task: {}", message))
                .orElse(defaultTrigger);
    }

    static <T> void writeNetData(RegistryFriendlyByteBuf buffer, CustomersTriggerSchema<T> schema, T trigger) {
        ByteBufCodecs.fromCodecWithRegistries(schema.codec()).encode(buffer, trigger);
    }

    static <T> T readNetData(RegistryFriendlyByteBuf buffer, CustomersTriggerSchema<T> schema) {
        return ByteBufCodecs.fromCodecWithRegistries(schema.codec()).decode(buffer);
    }

    static <T> void fillConfigGroup(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        for (CustomersTriggerSchema.Property<T> property : schema.properties()) {
            if (!property.taskEditable()) {
                continue;
            }
            switch (property.editor()) {
                case OPTIONAL_BOOLEAN -> addBoolean(config, schema, property, trigger, update);
                case OPTIONAL_ENUM -> addEnum(config, schema, property, trigger, update);
                case OPTIONAL_INT_RANGE -> addIntRange(config, schema, property, trigger, update);
                case OPTIONAL_ITEM_PREDICATE -> addItemPredicate(config, schema, property, trigger, update);
                case OPTIONAL_RESOURCE_LOCATION -> addResourceLocation(config, schema, property, trigger, update);
                case HIDDEN -> throw new IllegalStateException("Editable trigger properties cannot use a hidden editor");
            }
        }
    }

    private static <T> void addBoolean(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        OptionalBoolean value = OptionalBoolean.from(value(schema, trigger.get(), property));
        config.addEnum(
                        property.serializedName(),
                        value,
                        changed -> set(schema, trigger, update, property, changed.value()),
                        OptionalBoolean.nameMap(translationKey(property))
                )
                .setNameKey(translationKey(property));
    }

    private static <T> void addEnum(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        List<OptionalEnumValue> values = OptionalEnumValue.values(property.editorValues());
        OptionalEnumValue any = values.get(0);
        NameMap<OptionalEnumValue> nameMap = NameMap.of(any, values)
                .id(OptionalEnumValue::id)
                .baseNameKey(translationKey(property))
                .create();
        OptionalEnumValue selected = OptionalEnumValue.from(value(schema, trigger.get(), property), values);
        config.addEnum(
                        property.serializedName(),
                        selected,
                        changed -> set(schema, trigger, update, property, changed.value()),
                        nameMap
                )
                .setNameKey(translationKey(property));
    }

    private static <T> void addIntRange(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        Optional<MinMaxBounds.Ints> range = value(schema, trigger.get(), property);
        int minimum = range.flatMap(MinMaxBounds.Ints::min).orElse(0);
        int maximum = range.flatMap(MinMaxBounds.Ints::max).orElse(0);
        config.addInt(
                        property.serializedName() + "_min",
                        minimum,
                        changed -> setIntRange(
                                schema,
                                trigger,
                                update,
                                property,
                                changed,
                                rangeMax(schema, trigger.get(), property)
                        ),
                        0,
                        0,
                        Integer.MAX_VALUE
                )
                .setNameKey(translationKey(property) + ".min");
        config.addInt(
                        property.serializedName() + "_max",
                        maximum,
                        changed -> setIntRange(
                                schema,
                                trigger,
                                update,
                                property,
                                rangeMin(schema, trigger.get(), property),
                                changed
                        ),
                        0,
                        0,
                        Integer.MAX_VALUE
                )
                .setNameKey(translationKey(property) + ".max");
    }

    private static <T> void addItemPredicate(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        String itemReference = itemReference(value(schema, trigger.get(), property));
        config.addString(property.serializedName(), itemReference, changed -> {
                    Optional<Optional<ItemPredicate>> predicate = itemPredicate(changed);
                    predicate.ifPresent(value -> set(schema, trigger, update, property, value));
                }, "")
                .setNameKey(translationKey(property));
    }

    private static <T> void addResourceLocation(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        String resourceLocation = value(schema, trigger.get(), property).map(Object::toString).orElse("");
        config.addString(property.serializedName(), resourceLocation, changed -> {
                    if (changed.isBlank()) {
                        set(schema, trigger, update, property, Optional.empty());
                        return;
                    }
                    Optional.ofNullable(ResourceLocation.tryParse(changed))
                            .ifPresent(value -> set(schema, trigger, update, property, Optional.of(value)));
                }, "")
                .setNameKey(translationKey(property));
    }

    private static <T> void setIntRange(
            CustomersTriggerSchema<T> schema,
            Supplier<T> trigger,
            Consumer<T> update,
            CustomersTriggerSchema.Property<T> property,
            int minimum,
            int maximum
    ) {
        Optional<MinMaxBounds.Ints> range;
        if (minimum == 0 && maximum == 0) {
            range = Optional.empty();
        } else if (minimum == 0) {
            range = Optional.of(MinMaxBounds.Ints.atMost(maximum));
        } else if (maximum == 0) {
            range = Optional.of(MinMaxBounds.Ints.atLeast(minimum));
        } else {
            range = Optional.of(MinMaxBounds.Ints.between(minimum, maximum));
        }
        set(schema, trigger, update, property, range);
    }

    private static <T> int rangeMin(
            CustomersTriggerSchema<T> schema,
            T trigger,
            CustomersTriggerSchema.Property<T> property
    ) {
        Optional<MinMaxBounds.Ints> range = value(schema, trigger, property);
        return range.flatMap(MinMaxBounds.Ints::min).orElse(0);
    }

    private static <T> int rangeMax(
            CustomersTriggerSchema<T> schema,
            T trigger,
            CustomersTriggerSchema.Property<T> property
    ) {
        Optional<MinMaxBounds.Ints> range = value(schema, trigger, property);
        return range.flatMap(MinMaxBounds.Ints::max).orElse(0);
    }

    @SuppressWarnings("unchecked")
    private static <T, V> Optional<V> value(
            CustomersTriggerSchema<T> schema,
            T trigger,
            CustomersTriggerSchema.Property<T> property
    ) {
        return (Optional<V>) schema.value(trigger, property);
    }

    private static <T> void set(
            CustomersTriggerSchema<T> schema,
            Supplier<T> trigger,
            Consumer<T> update,
            CustomersTriggerSchema.Property<T> property,
            Object value
    ) {
        update.accept(schema.with(trigger.get(), property, value));
    }

    private static String translationKey(CustomersTriggerSchema.Property<?> property) {
        return CustomersTriggerSchema.PROPERTY_TRANSLATION_PREFIX + property.serializedName();
    }

    private static Optional<Optional<ItemPredicate>> itemPredicate(String value) {
        if (value.isBlank()) {
            return Optional.of(Optional.empty());
        }
        boolean tag = value.startsWith("#");
        String idValue = tag ? value.substring(1) : value;
        ResourceLocation id = ResourceLocation.tryParse(idValue);
        if (id == null) {
            return Optional.empty();
        }
        if (tag) {
            return Optional.of(Optional.of(
                    ItemPredicate.Builder.item().of(TagKey.create(Registries.ITEM, id)).build()
            ));
        }
        return BuiltInRegistries.ITEM.getOptional(id)
                .map(item -> Optional.of(Optional.of(ItemPredicate.Builder.item().of(item).build())))
                .orElseGet(Optional::empty);
    }

    private static String itemReference(Optional<ItemPredicate> predicate) {
        if (predicate.isEmpty() || predicate.get().items().isEmpty()) {
            return "";
        }
        HolderSet<Item> items = predicate.get().items().orElseThrow();
        Optional<TagKey<Item>> tag = items.unwrapKey();
        if (tag.isPresent()) {
            return "#" + tag.get().location();
        }
        if (items.size() == 1) {
            Holder<Item> item = items.get(0);
            return item.unwrapKey()
                    .map(key -> key.location().toString())
                    .orElseGet(() -> BuiltInRegistries.ITEM.getKey(item.value()).toString());
        }
        return "";
    }

    private enum OptionalBoolean {
        ANY(Optional.empty()),
        TRUE(Optional.of(true)),
        FALSE(Optional.of(false));

        private final Optional<Boolean> value;

        OptionalBoolean(Optional<Boolean> value) {
            this.value = value;
        }

        private static OptionalBoolean from(Optional<Boolean> value) {
            return value.map(current -> current ? TRUE : FALSE).orElse(ANY);
        }

        private static NameMap<OptionalBoolean> nameMap(String baseNameKey) {
            return NameMap.of(ANY, values())
                    .id(value -> value.name().toLowerCase(Locale.ROOT))
                    .baseNameKey(baseNameKey)
                    .create();
        }

        private Optional<Boolean> value() {
            return value;
        }
    }

    private record OptionalEnumValue(String id, Optional<Object> value) {
        private static List<OptionalEnumValue> values(List<?> editorValues) {
            List<OptionalEnumValue> values = new ArrayList<>();
            values.add(new OptionalEnumValue("any", Optional.empty()));
            for (Object value : editorValues) {
                String id = value instanceof StringRepresentable named
                        ? named.getSerializedName()
                        : value.toString().toLowerCase(Locale.ROOT);
                values.add(new OptionalEnumValue(id, Optional.of(value)));
            }
            return List.copyOf(values);
        }

        private static OptionalEnumValue from(Optional<?> value, List<OptionalEnumValue> values) {
            return values.stream().filter(current -> current.value().equals(value)).findFirst().orElse(values.get(0));
        }
    }
}
