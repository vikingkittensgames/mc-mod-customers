package com.vikingkittens.mc.customers.advancements.triggers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.chat.Component;

/**
 * Defines the optional record properties of a trigger once, then generates its codec and integration metadata from
 * that definition. Trigger records using this schema must represent every component as an {@link Optional}.
 */
public final class CustomersTriggerSchema<T> {
    public static final String PROPERTY_TRANSLATION_PREFIX = "advancements.triggers.property.";

    private final Codec<T> codec;
    private final Constructor<T> constructor;
    private final List<Property<T>> properties;

    private CustomersTriggerSchema(Constructor<T> constructor, List<Property<T>> properties) {
        this.constructor = constructor;
        this.properties = List.copyOf(properties);
        codec = createCodec();
    }

    public static <T> Builder<T> builder(Class<T> type) {
        if (!type.isRecord()) {
            throw new IllegalArgumentException("Trigger schema types must be records");
        }
        return new Builder<>(type);
    }

    public Codec<T> codec() {
        return codec;
    }

    public List<Property<T>> properties() {
        return properties;
    }

    public Optional<Property<T>> property(String serializedName) {
        return properties.stream().filter(property -> property.serializedName().equals(serializedName)).findFirst();
    }

    public Object value(T instance, Property<T> property) {
        try {
            return property.component().getAccessor().invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to read trigger property " + property.serializedName(), exception);
        }
    }

    public T with(T instance, Property<T> property, Object value) {
        Object[] values = new Object[properties.size()];
        for (Property<T> current : properties) {
            values[current.componentIndex()] = value(instance, current);
        }
        values[property.componentIndex()] = value;
        return construct(values, property.serializedName());
    }

    private Codec<T> createCodec() {
        return new MapCodec<T>() {
            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops) {
                return properties.stream().map(property -> ops.createString(property.serializedName()));
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                Object[] values = new Object[properties.size()];
                for (Property<T> property : properties) {
                    O encoded = input.get(property.serializedName());
                    if (encoded == null) {
                        values[property.componentIndex()] = Optional.empty();
                        continue;
                    }
                    DataResult<?> decoded = property.codec().parse(ops, encoded);
                    Optional<?> result = decoded.result();
                    if (result.isEmpty()) {
                        String message = decoded.error()
                                .map(error -> error.message())
                                .orElse("unknown decoding error");
                        return DataResult.error(() -> "Unable to decode " + property.serializedName() + ": " + message);
                    }
                    values[property.componentIndex()] = Optional.of(result.orElseThrow());
                }
                try {
                    return DataResult.success(construct(values, "schema"));
                } catch (IllegalStateException exception) {
                    return DataResult.error(exception::getMessage);
                }
            }

            @Override
            public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                for (Property<T> property : properties) {
                    Optional<?> value = (Optional<?>) CustomersTriggerSchema.this.value(input, property);
                    if (value.isPresent()) {
                        encodeProperty(property, value.orElseThrow(), ops, prefix);
                    }
                }
                return prefix;
            }
        }.codec();
    }

    @SuppressWarnings("unchecked")
    private static <O, V> void encodeProperty(
            Property<?> property,
            Object value,
            DynamicOps<O> ops,
            RecordBuilder<O> prefix
    ) {
        Codec<V> propertyCodec = (Codec<V>) property.codec();
        prefix.add(property.serializedName(), propertyCodec.encodeStart(ops, (V) value));
    }

    private T construct(Object[] values, String propertyName) {
        try {
            return constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to construct trigger while handling " + propertyName, exception);
        }
    }

    public enum Editor {
        HIDDEN,
        OPTIONAL_BOOLEAN,
        OPTIONAL_DOUBLE_RANGE,
        OPTIONAL_ENUM,
        OPTIONAL_INT_RANGE,
        OPTIONAL_ITEM_PREDICATE,
        OPTIONAL_LOCATION,
        OPTIONAL_RESOURCE_LOCATION
    }

    public record Property<T>(
            String serializedName,
            Component name,
            Codec<?> codec,
            Editor editor,
            List<?> editorValues,
            boolean taskEditable,
            int componentIndex,
            RecordComponent component
    ) {}

    public static final class Builder<T> {
        private final Constructor<T> constructor;
        private final List<RecordComponent> components;
        private final List<Property<T>> properties = new ArrayList<>();

        private Builder(Class<T> type) {
            components = Arrays.asList(type.getRecordComponents());
            try {
                constructor = type.getDeclaredConstructor(
                        components.stream().map(RecordComponent::getType).toArray(Class<?>[]::new)
                );
            } catch (NoSuchMethodException exception) {
                throw new IllegalArgumentException("Trigger record does not have a canonical constructor", exception);
            }
        }

        public Builder<T> property(
                String componentName,
                String serializedName,
                Codec<?> codec,
                Editor editor,
                boolean taskEditable
        ) {
            return property(componentName, serializedName, codec, editor, List.of(), taskEditable);
        }

        public Builder<T> property(
                String componentName,
                String serializedName,
                Codec<?> codec,
                Editor editor,
                List<?> editorValues,
                boolean taskEditable
        ) {
            int componentIndex = -1;
            for (int index = 0; index < components.size(); index++) {
                if (components.get(index).getName().equals(componentName)) {
                    componentIndex = index;
                    break;
                }
            }
            if (componentIndex < 0) {
                throw new IllegalArgumentException("Unknown trigger record component " + componentName);
            }
            properties.add(new Property<>(
                    serializedName,
                    Component.translatable(PROPERTY_TRANSLATION_PREFIX + serializedName),
                    codec,
                    editor,
                    List.copyOf(editorValues),
                    taskEditable,
                    componentIndex,
                    components.get(componentIndex)
            ));
            return this;
        }

        public CustomersTriggerSchema<T> build() {
            boolean coversEveryComponent = properties.size() == components.size()
                    && properties.stream().map(Property::componentIndex).distinct().count() == components.size();
            if (!coversEveryComponent) {
                throw new IllegalStateException("Every trigger record component must have a schema property");
            }
            return new CustomersTriggerSchema<>(constructor, properties);
        }
    }
}
