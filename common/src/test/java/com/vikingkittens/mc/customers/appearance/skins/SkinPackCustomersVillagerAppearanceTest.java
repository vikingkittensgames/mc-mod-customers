package com.vikingkittens.mc.customers.appearance.skins;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkinPackCustomersVillagerAppearanceTest {
    private static final ResourceLocation STEVE = ResourceLocation.parse("example:steve");
    private static final ResourceLocation ALEX = ResourceLocation.parse("example:alex");

    @Test
    void selectsPackSkinDeterministicallyFromVariationSeed() {
        List<ResourceLocation> skins = List.of(STEVE, ALEX);

        assertEquals(Optional.of(STEVE), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 0.0F));
        assertEquals(Optional.of(STEVE), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 0.49F));
        assertEquals(Optional.of(ALEX), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 0.5F));
        assertEquals(Optional.of(ALEX), SkinPackCustomersVillagerAppearance.selectSkinId(skins, 1.0F));
        assertEquals(Optional.empty(), SkinPackCustomersVillagerAppearance.selectSkinId(List.of(), 0.5F));
    }

    @Test
    void createsSoundEventsUsingReferencedSoundIds() {
        ResourceLocation ambient = ResourceLocation.parse("example:steve_ambient");
        SkinCustomersVillagerDefinition definition = new SkinCustomersVillagerDefinition(
                STEVE,
                SkinCustomersVillagerModel.WIDE,
                false,
                SkinCustomersVillagerDefinition.DEFAULT_SCALE,
                SkinCustomersVillagerDefinition.DEFAULT_SHADOW_RADIUS,
                0.0F,
                List.of(),
                Map.of("ambient", ambient),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Map.of(),
                List.of()
        );

        assertEquals(ambient, definition.getSound(SkinCustomersVillagerSound.AMBIENT).orElseThrow());
        assertNull(definition.getSound(SkinCustomersVillagerSound.DEATH).orElse(null));
        assertEquals(ambient, SoundEvent.createVariableRangeEvent(ambient).getLocation());
    }

    @Test
    void selectsNamesAcrossEachSkinsVariationRange() {
        List<Component> names = List.of(Component.literal("First"), Component.literal("Second"));

        assertEquals("First", SkinPackCustomersVillagerAppearance.selectName(
                names,
                SkinPackCustomersVillagerAppearance.getSelectedSkinVariation(2, 0.24F)
        ).orElseThrow().getString());
        assertEquals("Second", SkinPackCustomersVillagerAppearance.selectName(
                names,
                SkinPackCustomersVillagerAppearance.getSelectedSkinVariation(2, 0.49F)
        ).orElseThrow().getString());
        assertEquals("First", SkinPackCustomersVillagerAppearance.selectName(
                names,
                SkinPackCustomersVillagerAppearance.getSelectedSkinVariation(2, 0.74F)
        ).orElseThrow().getString());
        assertEquals("Second", SkinPackCustomersVillagerAppearance.selectName(
                names,
                SkinPackCustomersVillagerAppearance.getSelectedSkinVariation(2, 0.99F)
        ).orElseThrow().getString());
        assertEquals(Optional.empty(), SkinPackCustomersVillagerAppearance.selectName(List.of(), 0.5F));
    }

    @Test
    void selectsANameFromTheSelectedSkinDefinition() {
        SkinCustomersVillagerDefinition steve = definition(
                SkinCustomersVillagerModel.WIDE,
                List.of(),
                List.of(Component.literal("Steve One"), Component.literal("Steve Two"))
        );
        SkinCustomersVillagerDefinition alex = definition(
                SkinCustomersVillagerModel.SLIM,
                List.of(),
                List.of(Component.literal("Alex One"), Component.literal("Alex Two"))
        );
        Map<ResourceLocation, SkinCustomersVillagerDefinition> definitions = Map.of(STEVE, steve, ALEX, alex);

        assertEquals("Steve Two", SkinPackCustomersVillagerAppearance.selectName(
                List.of(STEVE, ALEX), definitions::get, 0.49F
        ).orElseThrow().getString());
        assertEquals("Alex One", SkinPackCustomersVillagerAppearance.selectName(
                List.of(STEVE, ALEX), definitions::get, 0.74F
        ).orElseThrow().getString());
    }

    @Test
    void geckoSkinsAreAvailableOnlyWhenGeckoLibIsLoaded() {
        SkinCustomersVillagerDefinition vanilla = definition(SkinCustomersVillagerModel.WIDE);
        SkinCustomersVillagerDefinition gecko = definition(
                SkinCustomersVillagerModel.gecko(ResourceLocation.parse("example:model"))
        );

        assertEquals(true, SkinPackCustomersVillagerAppearance.isAvailable(vanilla, false, modId -> true));
        assertEquals(false, SkinPackCustomersVillagerAppearance.isAvailable(gecko, false, modId -> true));
        assertEquals(true, SkinPackCustomersVillagerAppearance.isAvailable(gecko, true, modId -> true));
    }

    @Test
    void filtersMixedAndGeckoOnlyPacksWithoutGeckoLib() {
        ResourceLocation geckoId = ResourceLocation.parse("example:gecko");
        ResourceLocation missingId = ResourceLocation.parse("example:missing");
        Map<ResourceLocation, SkinCustomersVillagerDefinition> definitions = Map.of(
                STEVE, definition(SkinCustomersVillagerModel.WIDE),
                geckoId, definition(SkinCustomersVillagerModel.gecko(ResourceLocation.parse("example:model")))
        );

        assertEquals(
                List.of(STEVE),
                SkinPackCustomersVillagerAppearance.getAvailableSkinIds(
                        List.of(STEVE, geckoId, missingId),
                        definitions::get,
                        false,
                        modId -> true
                )
        );
        assertEquals(
                List.of(),
                SkinPackCustomersVillagerAppearance.getAvailableSkinIds(
                        List.of(geckoId),
                        definitions::get,
                        false,
                        modId -> true
                )
        );
        assertEquals(
                List.of(STEVE, geckoId),
                SkinPackCustomersVillagerAppearance.getAvailableSkinIds(
                        List.of(STEVE, geckoId),
                        definitions::get,
                        true,
                        modId -> true
                )
        );
    }

    @Test
    void skinsUsingAnotherModsAssetsRequireThatMod() {
        SkinCustomersVillagerDefinition definition = definition(
                SkinCustomersVillagerModel.gecko(ResourceLocation.parse("ribbits:geo/merchant_ribbit.geo.json")),
                List.of("ribbits")
        );

        assertEquals(false, SkinPackCustomersVillagerAppearance.isAvailable(definition, true, modId -> false));
        assertEquals(true, SkinPackCustomersVillagerAppearance.isAvailable(definition, true, "ribbits"::equals));
    }

    private static SkinCustomersVillagerDefinition definition(SkinCustomersVillagerModel model) {
        return definition(model, List.of(), List.of());
    }

    private static SkinCustomersVillagerDefinition definition(SkinCustomersVillagerModel model, List<String> requiredMods) {
        return definition(model, requiredMods, List.of());
    }

    private static SkinCustomersVillagerDefinition definition(
            SkinCustomersVillagerModel model,
            List<String> requiredMods,
            List<Component> names
    ) {
        boolean gecko = model.isGecko();
        return new SkinCustomersVillagerDefinition(
                STEVE,
                model,
                false,
                SkinCustomersVillagerDefinition.DEFAULT_SCALE,
                SkinCustomersVillagerDefinition.DEFAULT_SHADOW_RADIUS,
                0.0F,
                names,
                Map.of(),
                gecko ? Optional.of(ResourceLocation.parse("example:animation")) : Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                gecko ? Optional.of(new SkinCustomersVillagerPoint(0.0F, 12.0F, 0.0F)) : Optional.empty(),
                gecko ? Map.of("idle", "idle", "walk", "walk", "sit", "sit") : Map.of(),
                requiredMods
        );
    }
}
