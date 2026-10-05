# GeckoLib Models in the Skins Appearance Plan

## Goal

Extend the existing data-driven Skins appearance so individual skin definitions can optionally use animated
[GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) models.

This is not a new appearance type. It reuses the existing skin-pack and skin registries, deterministic selection,
textures, scale and shadow settings, sounds, and Spawner UI entries.

Without GeckoLib, `model` supports only `wide` and `slim`. With GeckoLib installed, `model` may instead be a
namespaced model resource ID such as `example:shopkeeper`, enabling the GeckoLib properties below.

## Research Summary

GeckoLib supports Forge, NeoForge, and Fabric. GeckoLib 4 applies to Minecraft 1.20.1 and 1.21.1, while Minecraft
1.21.11 uses GeckoLib 5.

Official references:

- [GeckoLib source](https://github.com/bernie-g/geckolib)
- [Installation](https://github.com/bernie-g/geckolib/wiki/Installation-%28Geckolib4%29)
- [Blockbench model creation](https://github.com/bernie-g/geckolib/wiki/Making-Your-Models-%28Blockbench%29)
- [GeckoLib entities](https://github.com/bernie-g/geckolib/wiki/Geckolib-Entities-%28Geckolib4%29)
- [GeoModel resources](https://github.com/bernie-g/geckolib/wiki/Geo-Models-%28Geckolib4%29)
- [Animation controllers](https://github.com/bernie-g/geckolib/wiki/The-Animation-Controller-%28Geckolib4%29)
- [GeckoLib 5 changes](https://github.com/bernie-g/geckolib/wiki/Geckolib-5-Changes)

GeckoLib 4 loads models and animations from `assets/{namespace}/geo` and `assets/{namespace}/animations`.
GeckoLib 5 moves them to `assets/{namespace}/geckolib/models` and
`assets/{namespace}/geckolib/animations`. The skin definition remains independent of GeckoLib Java classes, with
version-specific rendering adapters.

### Existing-Mod Asset Review

The Minecraft 1.21.1 distributions of several high-download GeckoLib dependents were inspected to determine whether
their installed jars contain reusable model resources:

| Mod and inspected version | Geo models | Animation files | Reuse considerations |
|---|---:|---:|---|
| Mowzie's Mobs 1.8.2 | 24 | 14 | Resources exist, but several models use custom controllers, form visibility, layers, or renderer behavior. |
| Creeper Overhaul 4.0.6 | 27 | 18 | Resources exist with simple idle and walk animations, but no general sitting animation. |
| Ribbits 4.1.6 | 25 | 1 | Resources exist with simple idle and walk animations and provide a practical reuse example. |
| Bosses of Mass Destruction 1.10.2 | 6 | 6 | Resources exist, but the animation sets are specialized for bosses. |
| Naturalist 2.0.5 | 0 | 0 | The jar contains entity textures but no Gecko model or animation JSON to reuse. |
| L_Ender's Cataclysm 3.33 | 0 | 0 | The jar contains entity textures but no Gecko model or animation JSON to reuse. |

Depending on GeckoLib does not guarantee that a mod ships reusable JSON assets. Models compiled into Java classes
cannot be loaded through this skin format. Packaged JSON also does not include behavior supplied by the original
mod's renderer, such as extra render layers, emissive passes, dynamic bone visibility, runtime texture selection,
particles, or custom animation-controller state.

## Dependency Strategy

GeckoLib is an optional Customers dependency. Add a pinned `geckolib_version` and GeckoLib's official Maven:

```groovy
maven {
    name = 'GeckoLib'
    url = 'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/'
    content {
        includeGroup('software.bernie.geckolib')
    }
}
```

Use the loader-specific artifact and do not bundle or shade it:

```text
software.bernie.geckolib:geckolib-neoforge-${minecraft_version}:${geckolib_version}
software.bernie.geckolib:geckolib-fabric-${minecraft_version}:${geckolib_version}
software.bernie.geckolib:geckolib-forge-${minecraft_version}:${geckolib_version}
```

Declare GeckoLib as optional in loader metadata. Only conditionally loaded client classes may reference GeckoLib.

| Minecraft | Loaders | GeckoLib generation | Initial tested line |
|---|---|---|---|
| 1.20.1 | Forge, Fabric | GeckoLib 4 | 4.8.x |
| 1.21.1 | NeoForge, Fabric | GeckoLib 4 | 4.8.4 |
| 1.21.11 | NeoForge, Fabric | GeckoLib 5 | 5.4.x |

## Existing Skin-Pack Layout

The registry layout does not change:

```text
data/{namespace}/customers/skin_packs/{pack}.json
data/{namespace}/customers/skins/{skin}.json
```

Existing groups may mix vanilla and GeckoLib skins:

```json
{
  "name": "Fantasy Staff",
  "skins": [
    "example:steve_shopkeeper",
    "example:animated_shopkeeper",
    "example:animated_chef"
  ]
}
```

An ordinary player skin remains unchanged:

```json
{
  "texture": "example:steve_shopkeeper",
  "model": "wide"
}
```

## GeckoLib Skin Definition

```json
{
  "texture": "example:animated_shopkeeper",
  "model": "example:animated_shopkeeper",
  "animation": "example:animated_shopkeeper",
  "scale": 1.0,
  "shadow_radius": 0.5,
  "name_tag_offset": 0.5,
  "head_bone": "head",
  "head_tracking": {
    "enabled": true,
    "maximum_yaw": 60.0,
    "maximum_pitch": 45.0,
    "yaw_multiplier": 1.0,
    "pitch_multiplier": 1.0
  },
  "sitting_pivot": [0.0, 12.0, 0.0],
  "animations": {
    "idle": "misc.idle",
    "walk": "move.walk",
    "run": "move.run",
    "sit": "pose.sit",
    "crouch_idle": "pose.crouch_idle",
    "crouch_walk": "pose.crouch_walk",
    "swim": "move.swim",
    "fall_flying": "move.fall_flying",
    "sleep": "pose.sleep",
    "hurt": "reaction.hurt",
    "death": "reaction.death",
    "swing_mainhand": "action.swing_mainhand",
    "swing_offhand": "action.swing_offhand",
    "use_item": "action.use_item",
    "eat": "action.eat",
    "drink": "action.drink",
    "block": "action.block",
    "bow": "action.bow",
    "crossbow_charge": "action.crossbow_charge",
    "crossbow_hold": "action.crossbow_hold",
    "throw_spear": "action.throw_spear",
    "spyglass": "action.spyglass",
    "toot_horn": "action.toot_horn",
    "brush": "action.brush",
    "celebrate": "action.celebrate"
  },
  "sounds": {
    "ambient": "example:shopkeeper_ambient",
    "hurt": "example:shopkeeper_hurt",
    "death": "example:shopkeeper_death",
    "step": "example:shopkeeper_step",
    "yes": "example:shopkeeper_yes",
    "no": "example:shopkeeper_no"
  }
}
```

## Model Field

Replace the closed `wide`/`slim` enum codec with a model reference accepting `wide`, `slim`, or a namespaced resource
ID. Bare values other than `wide` and `slim` are invalid. The Java representation distinguishes vanilla wide/slim
models from a Gecko model `ResourceLocation` without exposing GeckoLib types.

## GeckoLib Availability

When GeckoLib is absent, wide and slim definitions remain available, Gecko definitions are filtered from existing
skin selection, a mixed pack remains applicable when it has a vanilla definition, and a Gecko-only pack is hidden.
Gecko-specific JSON may decode without loading GeckoLib classes.

When GeckoLib is present, Gecko definitions become eligible alongside vanilla definitions. This extends
`SkinPackCustomersVillagerAppearance.getAvailableSkinIds()` rather than adding another provider.

Any skin may declare `required_mods`. All named mod IDs must be loaded before that skin can be selected. This keeps a
skin that references another mod's assets out of selection when its providing mod is absent. A mixed pack remains
available as long as at least one of its definitions has satisfied dependencies.

## Resource Resolution

Texture behavior remains unchanged:

```text
assets/example/textures/customers/skins/animated_shopkeeper.png
```

GeckoLib 4 resolves the example model and animation to:

```text
assets/example/geo/customers/skins/animated_shopkeeper.geo.json
assets/example/animations/customers/skins/animated_shopkeeper.animation.json
```

GeckoLib 5 resolves them to:

```text
assets/example/geckolib/models/customers/skins/animated_shopkeeper.geo.json
assets/example/geckolib/animations/customers/skins/animated_shopkeeper.animation.json
```

A cross-version resource pack may carry both asset layouts.

Resource IDs without a file extension use the Customers paths above. A complete model ID ending in `.geo.json`,
animation ID ending in `.animation.json`, or texture ID ending in `.png` is instead used exactly as written. This
allows a definition to reference resources already exposed by another installed mod, for example:

```json
{
  "texture": "ribbits:textures/entity/ribbit.png",
  "model": "ribbits:geo/merchant_ribbit.geo.json",
  "animation": "ribbits:animations/ribbit.animation.json",
  "required_mods": ["ribbits"],
  "scale": 1.0,
  "shadow_radius": 0.35,
  "name_tag_offset": 1.0,
  "head_bone": "body",
  "head_tracking": {
    "enabled": false
  },
  "sitting_pivot": [0.0, 2.1, -0.5],
  "animations": {
    "idle": "idle",
    "walk": "walk",
    "sit": "idle"
  }
}
```

The values after the namespace are paths inside `assets/{namespace}`. Complete paths are intentionally not rewritten
for GeckoLib 4 or 5, because they refer to the contents of a particular upstream jar. Definitions may need different
paths on different Minecraft branches if that mod reorganizes its assets.

## Definition Fields

All models retain the existing `texture`, `scale`, `shadow_radius`, `name_tag_offset`, and `sounds` fields. The
optional `required_mods` list is available to both ordinary and Gecko skins.

A Gecko model additionally supports `animation`, `head_bone`, `head_tracking`, `sitting_pivot`, and `animations`.
Required Gecko values are `animation`, `sitting_pivot`, and the idle, walk, and sit animation mappings. `head_bone`
defaults to `head`, head tracking defaults to enabled, and all other animations are optional.

Contradictory Gecko-only properties on wide or slim definitions should fail codec validation with a useful message.

## Coordinate System and Anchors

`sitting_pivot` uses Blockbench model units: 16 units per block, positive X right, positive Y up, positive Z forward,
and feet at Y zero. Scale is applied after conversion. Horizontal correction is rotated by interpolated body yaw.

The pivot is aligned with the canonical Customers humanoid seated hip point `[0, 12, 0]`.

The configured head bone is both the procedural head-tracking bone and the runtime anchor for wanted items. After
GeckoLib evaluates the current animation, the renderer records the transformed head-bone position. The wanted-item
row renders from that position with the existing `name_tag_offset` added vertically. The normal extra gap when a
visible name tag is present remains unchanged.

Pack creators set `name_tag_offset` to the desired distance from the head pivot to the row center. When the head bone
is absent, rendering falls back to the existing entity-height calculation and still applies `name_tag_offset`.

## Foundational Client Changes

Widen `CustomersVillagerClientAppearance.getRenderer` from `MobRenderer<?, ?>` to `EntityRenderer<?>`, because a
Gecko renderer is not guaranteed to be a `MobRenderer`.

Add an optional runtime overhead-anchor provider to the client appearance. Gecko returns the transformed head-bone
position; other appearances return no anchor and retain entity-height positioning. Apply `name_tag_offset` after
resolving either anchor. Do not expose GeckoLib types in the public API.

The anchor must come from the current animation frame, not only the static model pivot. Rendering order must ensure
the current-frame bone transform is available before wanted items render.

## Skin Client Renderer

Keep one `SkinCustomersVillagerClientAppearance`. Wide and slim use their current renderers; a resource-ID model uses
the optional Gecko delegate. The main skin class must not statically reference GeckoLib. If Gecko support is
unexpectedly unavailable, fall back to the normal Villager renderer instead of crashing.

## Gecko Render Proxy

Do not make Customer or Supplier entities implement GeckoLib interfaces. Create a conditionally loaded proxy that
owns the animation cache/controllers, weakly references the source, and mirrors position, rotations, pose, movement,
equipment, hurt/death/swing/use state, visibility, glow, name, passenger, and vehicle state. It implements
`CustomersVillagerRenderProxy`, is never added or saved, and is cached per source entity and selected skin.

Use a separate GeckoLib 5 render-state adapter rather than version checks throughout common logic.

## Animation Contract

Required looping animations are `idle`, `walk`, and `sit`. Optional locomotion is `run`, `crouch_idle`,
`crouch_walk`, `swim`, `fall_flying`, and `sleep`. Optional reactions are `hurt`, `death`, and `celebrate`. Optional
humanoid actions are main/offhand swing, generic use, eat, drink, block, bow, crossbow charge/hold, spear, spyglass,
horn, and brush.

Use walk as the fallback for run/crouch-walk/swim and idle for other missing pose animations. Hurt, action, and
celebrate animations are replayable one-shots; death holds its final frame.

Register base locomotion first, upper-body actions second, reactions third, and procedural head tracking after JSON
animation transforms. Avoid conflicting concurrent keyframes on the same bones.

## Head Tracking

The head bone pivots at the neck, contains head cubes, parents head accessories, and excludes the torso. GeckoLib 4
uses entity model data from the custom-animation hook; GeckoLib 5 carries equivalent values through render state.

Tracking adds to animation rotation, converts degrees to radians once, applies configured multipliers and limits,
skips safely when disabled, and logs a missing bone once per model rather than per frame.

## Sounds

Make no sound implementation changes. Gecko definitions use the existing skin `sounds` object and the existing
ambient, hurt, death, step, yes, and no lookup/fallback behavior. Continue using
`assets/{namespace}/sounds/customers/skins` and normal `sounds.json`. Do not use Gecko sound keyframes for these
behavioral sounds.

## Pack Construction

A pack continues combining `data/{namespace}/customers/skin_packs`, `data/{namespace}/customers/skins`, existing
skin textures and sounds, plus Gecko model and animation assets. Install the data portion in the world and the resource
portion on every client. A combined ZIP may be copied to both directories when its metadata supports both pack types.

Use `/reload` for definition changes and `F3+T` for model, animation, texture, and sound changes.

## Blockbench Guide

Install `GeckoLib Models & Animations`, create a GeckoLib Animated Model of entity type, and use one root with nested
body, head, arm, and leg bones. Only groups/bones animate. Put pivots at the neck, shoulders, and hips, keep feet at Y
zero, and record the sitting hip coordinates.

Create looping idle, walk, and sit animations, then optional humanoid animations. Hurt/actions are one-shot; death
holds its last frame. Verify transitions, a stable sitting pivot, and head accessory parenting.

Export the GeckoLib model, animation JSON, and PNG. Retain `.bbmodel` sources outside distributed resources.

## Blender Guidance

GeckoLib has no documented direct Blender production exporter. Blender may provide concepts, references, sculpting,
previews, or interchange geometry, but Blockbench remains the final rigging, animation, validation, and export tool.
Expect to rebuild cube geometry, pivots, parenting, animations, and pixel-aligned UVs after interchange.

## Implementation Phases

1. Extend the skin model codec and definition while preserving vanilla JSON compatibility.
2. Filter Gecko definitions based on optional-mod availability and test mixed/Gecko-only packs.
3. Generalize client renderer and runtime overhead-anchor support without changing existing appearances.
4. Implement and test the GeckoLib 4 proxy, dynamic model, controllers, head tracking, sitting, and overhead anchor.
5. Validate NeoForge and Fabric development and production builds on Minecraft 1.21.1.
6. Add the GeckoLib 5 adapter and asset resolution on Minecraft 1.21.11.
7. Backport the GeckoLib 4 integration to Forge and Fabric on Minecraft 1.20.1.
8. Add a mixed example pack and player-facing authoring documentation.

## Testing

JUnit coverage includes unchanged wide/slim decoding, Gecko resource IDs, invalid bare models, optional-mod filtering,
mixed and Gecko-only packs, unchanged vanilla deterministic selection, unchanged sounds, scale and pivot conversion,
yaw rotation, animation priority/fallbacks, head tracking, and class-loading guards. Do not test static resource file
contents.

Manual testing covers Gecko absent/present, mixed packs, missing resources, reloads, idle/walk/sit and other states,
head tracking, stairs/slabs, wanted items with and without names, multiplayer, dedicated servers, and remapped
production clients on each loader.

## Performance and Failure Behavior

Cache proxies, animation state, decoded definitions, and resource IDs. Do not parse JSON, scan entities, resolve
registries, or repeatedly log from render callbacks. Rebuild resource caches only after reload.

Without Gecko, filter Gecko definitions. Missing renderer/assets/definitions fall back to normal Villager rendering.
Missing optional animations use documented fallbacks, missing head bones disable tracking and use entity-height item
positioning, invalid pivots reject the definition, and missing sounds keep Villager sounds.

## Completion Criteria

- Existing skin packs work unchanged.
- GeckoLib remains optional.
- Mixed vanilla and Gecko skin groups work.
- No new appearance registries or sound implementation exist.
- Models, animations, sitting pivots, head tracking, and wanted-item placement are data-driven.
- All loader production builds pass.
- GeckoLib 4/5 differences remain isolated behind adapters.
- An example mixed skin pack and authoring guide are included.

## Implementation Status

Minecraft 1.21.1 implementation is complete and awaiting in-game validation on NeoForge and Fabric:

- the existing skin codec accepts validated Gecko model, animation, tracking, and pivot data
- Gecko definitions are filtered when GeckoLib is absent without loading Gecko classes
- the common appearance renderer accepts both `MobRenderer` and Gecko `EntityRenderer` delegates
- weakly cached proxies drive locomotion, sitting, item use, swings, crossbows, damage, death, and celebration
- head tracking and the current-frame wanted-item anchor use the configured head bone
- complete resource paths can reuse assets from another loaded mod, with generic required-mod filtering
- the existing sound path is unchanged
- GeckoLib 4.8.4 is available in development and production test clients without being bundled
- `examples/customers-gecko-skins` provides a mixed vanilla/animated pack
- `docs/appearances/skins.md` documents construction and Blockbench workflow

The GeckoLib 5 adapter for Minecraft 1.21.11 and the GeckoLib 4 backport for Minecraft 1.20.1 remain branch-porting
work. Their resource paths and dependency versions are specified above so those ports do not redesign the format.
