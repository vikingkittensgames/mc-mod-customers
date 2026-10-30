package com.vikingkittens.mc.customers.compatability;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;

import static org.mockito.Mockito.*;

public final class VanillaPlatformTestHelper implements IPlatformTestHelper {
    @Override
    public void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        HolderLookup.Provider registries = withEmptyTags(
                VanillaRegistries.createLookup(),
                DamageTypeTags.IS_FIRE,
                DamageTypeTags.IS_EXPLOSION,
                DamageTypeTags.BYPASSES_SHIELD,
                BannerPatternTags.PATTERN_ITEM_FLOWER,
                BannerPatternTags.PATTERN_ITEM_CREEPER,
                BannerPatternTags.PATTERN_ITEM_SKULL,
                BannerPatternTags.PATTERN_ITEM_MOJANG,
                BannerPatternTags.PATTERN_ITEM_GLOBE,
                BannerPatternTags.PATTERN_ITEM_PIGLIN,
                BannerPatternTags.PATTERN_ITEM_FLOW,
                BannerPatternTags.PATTERN_ITEM_GUSTER,
                BannerPatternTags.PATTERN_ITEM_FIELD_MASONED,
                BannerPatternTags.PATTERN_ITEM_BORDURE_INDENTED
        );
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(registries)
                .forEach(DataComponentInitializers.PendingComponents::apply);
    }

    private static HolderLookup.Provider withEmptyTags(
            HolderLookup.Provider registries,
            TagKey<?>... tags
    ) {
        Set<TagKey<?>> emptyTags = Set.of(tags);
        return new HolderLookup.Provider() {
            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(
                    ResourceKey<? extends net.minecraft.core.Registry<? extends T>> registryKey
            ) {
                return registries.lookup(registryKey).map(parent -> new HolderLookup.RegistryLookup.Delegate<>() {
                    @Override
                    public HolderLookup.RegistryLookup<T> parent() {
                        return parent;
                    }

                    @Override
                    public Optional<HolderSet.Named<T>> get(TagKey<T> tag) {
                        return emptyTags.contains(tag) ? Optional.of(emptyTag(tag)) : parent.get(tag);
                    }
                });
            }

            @Override
            public Stream<ResourceKey<? extends net.minecraft.core.Registry<?>>> listRegistryKeys() {
                return registries.listRegistryKeys();
            }
        };
    }

    private static <T> HolderSet.Named<T> emptyTag(TagKey<T> tag) {
        HolderSet.Named<T> empty = mock(HolderSet.Named.class);
        when(empty.key()).thenReturn(tag);
        when(empty.isBound()).thenReturn(true);
        when(empty.size()).thenReturn(0);
        return empty;
    }
}
