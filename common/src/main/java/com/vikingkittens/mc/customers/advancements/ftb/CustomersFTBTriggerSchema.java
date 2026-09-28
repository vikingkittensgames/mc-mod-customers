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
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.advancements.triggers.CustomersLocationPredicate;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSchema;

final class CustomersFTBTriggerSchema {
    private static final String TRIGGER_DATA_KEY = "trigger";
    private static final int FIRST_TRIGGER_CONFIG_ORDER = 110;
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
                case OPTIONAL_DOUBLE_RANGE -> addDoubleRange(config, schema, property, trigger, update);
                case OPTIONAL_ENUM -> addEnum(config, schema, property, trigger, update);
                case OPTIONAL_INT_RANGE -> addIntRange(config, schema, property, trigger, update);
                case OPTIONAL_ITEM_PREDICATE -> addItemPredicate(config, schema, property, trigger, update);
                case OPTIONAL_LOCATION -> addLocation(config, schema, property, trigger, update);
                case OPTIONAL_RESOURCE_LOCATION -> addResourceLocation(config, schema, property, trigger, update);
                case HIDDEN -> throw new IllegalStateException("Editable trigger properties cannot use a hidden editor");
            }
        }
        int order = FIRST_TRIGGER_CONFIG_ORDER;
        for (String configId : configPropertyOrder(schema)) {
            config.getValues().stream()
                    .filter(value -> value.id.equals(configId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing FTB task config property " + configId))
                    .setOrder(order++);
        }
    }

    static List<String> configPropertyOrder(CustomersTriggerSchema<?> schema) {
        List<String> configIds = new ArrayList<>();
        for (CustomersTriggerSchema.Property<?> property : schema.properties()) {
            if (!property.taskEditable()) {
                continue;
            }
            switch (property.editor()) {
                case OPTIONAL_DOUBLE_RANGE, OPTIONAL_INT_RANGE -> {
                    configIds.add(property.serializedName() + "_min");
                    configIds.add(property.serializedName() + "_max");
                }
                case OPTIONAL_ITEM_PREDICATE -> {
                    configIds.add(property.serializedName());
                    configIds.add(property.serializedName() + "_tag");
                }
                case OPTIONAL_LOCATION -> {
                    configIds.add(property.serializedName() + "_enabled");
                    configIds.add(property.serializedName() + "_dimension");
                    configIds.add(property.serializedName() + "_x");
                    configIds.add(property.serializedName() + "_y");
                    configIds.add(property.serializedName() + "_z");
                }
                case OPTIONAL_BOOLEAN, OPTIONAL_ENUM, OPTIONAL_RESOURCE_LOCATION ->
                        configIds.add(property.serializedName());
                case HIDDEN -> throw new IllegalStateException("Editable trigger properties cannot use a hidden editor");
            }
        }
        return List.copyOf(configIds);
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

    private static <T> void addDoubleRange(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        Optional<MinMaxBounds.Doubles> range = value(schema, trigger.get(), property);
        double minimum = range.flatMap(MinMaxBounds.Doubles::min).orElse(0.0D);
        double maximum = range.flatMap(MinMaxBounds.Doubles::max).orElse(0.0D);
        config.addDouble(
                        property.serializedName() + "_min",
                        minimum,
                        changed -> setDoubleRange(
                                schema,
                                trigger,
                                update,
                                property,
                                changed,
                                doubleRangeMax(schema, trigger.get(), property)
                        ),
                        0.0D,
                        0.0D,
                        Double.MAX_VALUE
                )
                .setNameKey(translationKey(property) + ".min");
        config.addDouble(
                        property.serializedName() + "_max",
                        maximum,
                        changed -> setDoubleRange(
                                schema,
                                trigger,
                                update,
                                property,
                                doubleRangeMin(schema, trigger.get(), property),
                                changed
                        ),
                        0.0D,
                        0.0D,
                        Double.MAX_VALUE
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
        ItemPredicateEditor<T> editor = new ItemPredicateEditor<>(schema, property, trigger, update);
        config.addItemStack(
                        property.serializedName(),
                        editor.item(),
                        editor::setItem,
                        ItemStack.EMPTY,
                        true,
                        true
                )
                .setAllowNBTEdit(false)
                .setNameKey(translationKey(property));
        config.addString(
                        property.serializedName() + "_tag",
                        editor.tag(),
                        editor::setTag,
                        ""
                )
                .setNameKey(translationKey(property) + ".tag");
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

    private static <T> void addLocation(
            ConfigGroup config,
            CustomersTriggerSchema<T> schema,
            CustomersTriggerSchema.Property<T> property,
            Supplier<T> trigger,
            Consumer<T> update
    ) {
        LocationEditor<T> editor = new LocationEditor<>(schema, property, trigger, update);
        String propertyName = property.serializedName();
        String nameKey = translationKey(property);
        config.addBool(propertyName + "_enabled", editor.enabled(), editor::setEnabled, false)
                .setNameKey(nameKey + ".enabled");
        config.addString(
                        propertyName + "_dimension",
                        editor.dimension(),
                        editor::setDimension,
                        Level.OVERWORLD.location().toString()
                )
                .setNameKey(nameKey + ".dimension");
        config.addInt(
                        propertyName + "_x",
                        editor.x(),
                        editor::setX,
                        0,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                )
                .setNameKey(nameKey + ".x");
        config.addInt(
                        propertyName + "_y",
                        editor.y(),
                        editor::setY,
                        0,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                )
                .setNameKey(nameKey + ".y");
        config.addInt(
                        propertyName + "_z",
                        editor.z(),
                        editor::setZ,
                        0,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                )
                .setNameKey(nameKey + ".z");
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

    private static <T> void setDoubleRange(
            CustomersTriggerSchema<T> schema,
            Supplier<T> trigger,
            Consumer<T> update,
            CustomersTriggerSchema.Property<T> property,
            double minimum,
            double maximum
    ) {
        Optional<MinMaxBounds.Doubles> range;
        if (minimum == 0.0D && maximum == 0.0D) {
            range = Optional.empty();
        } else if (minimum == 0.0D) {
            range = Optional.of(MinMaxBounds.Doubles.atMost(maximum));
        } else if (maximum == 0.0D) {
            range = Optional.of(MinMaxBounds.Doubles.atLeast(minimum));
        } else {
            range = Optional.of(MinMaxBounds.Doubles.between(minimum, maximum));
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

    private static <T> double doubleRangeMin(
            CustomersTriggerSchema<T> schema,
            T trigger,
            CustomersTriggerSchema.Property<T> property
    ) {
        Optional<MinMaxBounds.Doubles> range = value(schema, trigger, property);
        return range.flatMap(MinMaxBounds.Doubles::min).orElse(0.0D);
    }

    private static <T> double doubleRangeMax(
            CustomersTriggerSchema<T> schema,
            T trigger,
            CustomersTriggerSchema.Property<T> property
    ) {
        Optional<MinMaxBounds.Doubles> range = value(schema, trigger, property);
        return range.flatMap(MinMaxBounds.Doubles::max).orElse(0.0D);
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

    static Optional<Optional<ItemPredicate>> itemPredicate(ItemStack item, String tagReference) {
        if (!tagReference.isBlank()) {
            String idValue = tagReference.startsWith("#") ? tagReference.substring(1) : tagReference;
            ResourceLocation id = ResourceLocation.tryParse(idValue);
            if (id == null) {
                return Optional.empty();
            }
            return Optional.of(Optional.of(
                    ItemPredicate.Builder.item()
                            .of(TagKey.create(Registries.ITEM, id))
                            .build()
            ));
        }
        if (item.isEmpty()) {
            return Optional.of(Optional.empty());
        }
        return Optional.of(Optional.of(
                ItemPredicate.Builder.item().of(item.getItem()).build()
        ));
    }

    static ItemStack selectedItem(Optional<ItemPredicate> predicate) {
        if (predicate.isEmpty() || predicate.get().items().isEmpty()) {
            return ItemStack.EMPTY;
        }
        HolderSet<Item> items = predicate.get().items().orElseThrow();
        return items.unwrapKey().isEmpty() && items.size() == 1
                ? new ItemStack(items.get(0).value())
                : ItemStack.EMPTY;
    }

    static String selectedTag(Optional<ItemPredicate> predicate) {
        return predicate.flatMap(ItemPredicate::items)
                .flatMap(HolderSet::unwrapKey)
                .map(tag -> "#" + tag.location())
                .orElse("");
    }

    static Optional<Optional<CustomersLocationPredicate>> location(
            boolean enabled,
            String dimension,
            int x,
            int y,
            int z
    ) {
        if (!enabled) {
            return Optional.of(Optional.empty());
        }
        ResourceLocation dimensionId = ResourceLocation.tryParse(dimension);
        if (dimensionId == null) {
            return Optional.empty();
        }
        return Optional.of(Optional.of(new CustomersLocationPredicate(
                ResourceKey.create(Registries.DIMENSION, dimensionId),
                new BlockPos(x, y, z)
        )));
    }

    private static final class LocationEditor<T> {
        private final CustomersTriggerSchema<T> schema;
        private final CustomersTriggerSchema.Property<T> property;
        private final Supplier<T> trigger;
        private final Consumer<T> update;
        private boolean enabled;
        private String dimension;
        private int x;
        private int y;
        private int z;

        private LocationEditor(
                CustomersTriggerSchema<T> schema,
                CustomersTriggerSchema.Property<T> property,
                Supplier<T> trigger,
                Consumer<T> update
        ) {
            Optional<CustomersLocationPredicate> location = value(schema, trigger.get(), property);
            this.schema = schema;
            this.property = property;
            this.trigger = trigger;
            this.update = update;
            enabled = location.isPresent();
            dimension = location.map(value -> value.dimension().location().toString())
                    .orElse(Level.OVERWORLD.location().toString());
            x = location.map(value -> value.position().getX()).orElse(0);
            y = location.map(value -> value.position().getY()).orElse(0);
            z = location.map(value -> value.position().getZ()).orElse(0);
        }

        private boolean enabled() { return enabled; }

        private String dimension() { return dimension; }

        private int x() { return x; }

        private int y() { return y; }

        private int z() { return z; }

        private void setEnabled(boolean changed) {
            enabled = changed;
            apply();
        }

        private void setDimension(String changed) {
            dimension = changed;
            apply();
        }

        private void setX(int changed) {
            x = changed;
            apply();
        }

        private void setY(int changed) {
            y = changed;
            apply();
        }

        private void setZ(int changed) {
            z = changed;
            apply();
        }

        private void apply() {
            location(enabled, dimension, x, y, z)
                    .ifPresent(value -> set(schema, trigger, update, property, value));
        }
    }

    private static final class ItemPredicateEditor<T> {
        private final CustomersTriggerSchema<T> schema;
        private final CustomersTriggerSchema.Property<T> property;
        private final Supplier<T> trigger;
        private final Consumer<T> update;
        private final ItemStack initialItem;
        private final String initialTag;
        private ItemStack item;
        private String tag;

        private ItemPredicateEditor(
                CustomersTriggerSchema<T> schema,
                CustomersTriggerSchema.Property<T> property,
                Supplier<T> trigger,
                Consumer<T> update
        ) {
            Optional<ItemPredicate> predicate = value(schema, trigger.get(), property);
            this.schema = schema;
            this.property = property;
            this.trigger = trigger;
            this.update = update;
            initialItem = selectedItem(predicate);
            initialTag = selectedTag(predicate);
            item = initialItem;
            tag = initialTag;
        }

        private ItemStack item() {
            return item;
        }

        private String tag() {
            return tag;
        }

        private void setItem(ItemStack changed) {
            if (ItemStack.isSameItemSameComponents(initialItem, changed)) {
                return;
            }
            item = changed.copy();
            tag = "";
            apply();
        }

        private void setTag(String changed) {
            if (initialTag.equals(changed)) {
                return;
            }
            tag = changed;
            if (!changed.isBlank()) {
                item = ItemStack.EMPTY;
            }
            apply();
        }

        private void apply() {
            itemPredicate(item, tag)
                    .ifPresent(predicate -> set(schema, trigger, update, property, predicate));
        }
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
