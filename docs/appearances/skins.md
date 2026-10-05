---
title: Skins
parent_title: Appearances
parent_url: /appearances/
---

### Skins

Skin packs are data-driven appearances that use standard Minecraft player skins. Each
skin-pack JSON becomes a separately selectable appearance in Customer and Supplier
Spawner interfaces. When a skin-pack appearance is selected for a villager, its saved
appearance variation consistently selects one of the skins in that pack.

The mod includes an **MC Skins** appearance containing Alex, Ari, Efe, Herobrine,
Makena, Steve, and Zuri.

![apperance-skins.png]({{ '/screenshots/apperance-skins.png' | relative_url }})

A skin pack uses synchronized data-pack definitions together with client resource-pack
textures and optional sounds:

When distributing a skin pack as one combined ZIP, install it in the world's `datapacks`
folder for the skin definitions and in the client's `resourcepacks` folder for its
textures and sounds. Enable the resource-pack copy from Minecraft's Resource Packs menu.

```text
data/{namespace}/customers/skin_packs/{pack}.json
data/{namespace}/customers/skins/{skin}.json

assets/{namespace}/textures/customers/skins/{texture}.png
assets/{namespace}/textures/customers/skins/{texture}.png.mcmeta

assets/{namespace}/sounds/customers/skins/{sound}.ogg
assets/{namespace}/sounds.json
```

For example, this file defines an appearance named **Example Skins**:

**data/example/customers/skin_packs/example.json:**
```json
{
  "name": "Example Skins",
  "skins": [
    "example:steve",
    "example:alex"
  ]
}
```

Each referenced skin has its own definition. Steve uses the standard wide-arm player
model:

**data/example/customers/skins/steve.json:**
```json
{
  "texture": "example:steve",
  "model": "wide"
}
```

Alex uses the slim-arm player model and demonstrates the optional rendering and sound
settings:

**data/example/customers/skins/alex.json:**
```json
{
  "texture": "example:alex",
  "model": "slim",
  "scale": 0.9375,
  "shadow_radius": 0.5,
  "name_tag_offset": 0.0,
  "names": [
    "Alex",
    {
      "translate": "name.example.alex"
    }
  ],
  "sounds": {
    "ambient": "example:alex_ambient",
    "hurt": "example:alex_hurt",
    "death": "example:alex_death",
    "step": "example:alex_step",
    "yes": "example:alex_yes",
    "no": "example:alex_no"
  }
}
```

`names` is optional. When it is missing or empty, Customers does not assign a name, allowing naming mods such as
Villager Names to name the Customer or Supplier. Otherwise, Customers selects one entry when it assigns the skin and
stores it as the entity's normal Minecraft custom name.

Each entry uses Minecraft's standard JSON text-component format. A JSON string is a literal name. An object can use
`translate`, `text`, styling, or the other normal component fields. Translated names require the matching key in the
client resource pack, for example:

```json
{
  "name.example.alex": "Alex"
}
```

in `assets/example/lang/en_us.json`. Components are retained rather than resolved on the server, so every client can
display a translated name using its selected language.

The texture IDs in those definitions resolve to:

```text
assets/example/textures/customers/skins/steve.png
assets/example/textures/customers/skins/alex.png
```

Standard `.png.mcmeta` files can animate modern 64×64 skins. A legacy 64×32 skin can
set `"legacy": true`; legacy skins are converted at runtime and cannot be animated.

Every sound is optional. A missing sound uses the normal Villager sound for that action.
Custom sounds use Minecraft's standard `sounds.json` system. For example:

**assets/example/sounds.json:**
```json
{
  "alex_ambient": {
    "sounds": [
      "example:customers/skins/alex_ambient"
    ]
  },
  "alex_hurt": {
    "sounds": [
      "example:customers/skins/alex_hurt"
    ]
  },
  "alex_no": {
    "sounds": [
      "example:customers/skins/alex_no"
    ]
  },
  "alex_death": {
    "sounds": [
      "example:customers/skins/alex_death"
    ]
  },
  "alex_step": {
    "sounds": [
      "example:customers/skins/alex_step"
    ]
  },
  "alex_yes": {
    "sounds": [
      "example:customers/skins/alex_yes"
    ]
  }
}
```

Those entries load OGG files from:

```text
assets/example/sounds/customers/skins/alex_ambient.ogg
assets/example/sounds/customers/skins/alex_hurt.ogg
assets/example/sounds/customers/skins/alex_death.ogg
assets/example/sounds/customers/skins/alex_step.ogg
assets/example/sounds/customers/skins/alex_yes.ogg
assets/example/sounds/customers/skins/alex_no.ogg
```

Sound definitions may use normal Minecraft `sounds.json` features such as multiple
weighted variants, volume, pitch, subtitles, streaming, and replacement. Skin packs
with IDs that conflict with code-defined appearances are ignored in favor of the
code-defined appearance.

![apperance-skins-datapack.png]({{ '/screenshots/apperance-skins-datapack.png' | relative_url }})

### Animated GeckoLib models

When [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) is installed on the client, a skin definition
may use a namespaced Gecko model ID instead of `wide` or `slim`. GeckoLib is optional: ordinary skins continue to
work without it, Gecko skins are omitted from random selection when it is absent, and a pack containing only Gecko
skins is hidden until GeckoLib is installed.

Minecraft 1.21.1 uses GeckoLib 4.8.4 or another compatible GeckoLib 4 release. Gecko skin definitions use the same
texture and sound fields as ordinary skins. No Gecko sound-keyframe setup is needed.

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
    "sit": "pose.sit",
    "run": "move.run",
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
    "spyglass": "action.spyglass",
    "throw_spear": "action.throw_spear",
    "toot_horn": "action.toot_horn",
    "brush": "action.brush",
    "celebrate": "action.celebrate"
  }
}
```

`animation`, `sitting_pivot`, and the `idle`, `walk`, and `sit` animation mappings are required. Other animation
mappings are optional. A missing run, crouch-walk, or swim animation uses walk; a missing crouch-idle, flying, or
sleep animation uses idle. Optional action and reaction animations simply do not play when absent.

For GeckoLib 4, the example IDs resolve to:

```text
assets/example/geo/customers/skins/animated_shopkeeper.geo.json
assets/example/animations/customers/skins/animated_shopkeeper.animation.json
assets/example/textures/customers/skins/animated_shopkeeper.png
```

GeckoLib 5 branches use these model and animation locations instead, allowing a cross-version pack to include both:

```text
assets/example/geckolib/models/customers/skins/animated_shopkeeper.geo.json
assets/example/geckolib/animations/customers/skins/animated_shopkeeper.animation.json
```

#### Reusing GeckoLib assets from another mod

A skin can use model, animation, and texture files that are already inside another installed mod. Use the complete
resource path, including its directory and file extension, instead of the shorter Customers ID. Add `required_mods`
so Customers does not select the skin when the mod providing those files is absent.

For example, [Ribbits](https://www.curseforge.com/minecraft/mc-mods/ribbits) 4.1.6 contains a merchant model at
`assets/ribbits/geo/merchant_ribbit.geo.json`, animations at
`assets/ribbits/animations/ribbit.animation.json`, and its texture at
`assets/ribbits/textures/entity/ribbit.png`. A skin using those installed assets can be defined as:

```json
{
  "texture": "ribbits:textures/entity/ribbit.png",
  "model": "ribbits:geo/merchant_ribbit.geo.json",
  "animation": "ribbits:animations/ribbit.animation.json",
  "required_mods": [
    "ribbits"
  ],
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

Ribbits does not provide a sitting animation, so this example keeps its idle pose while Customers aligns the leg
pivot with the seat. A model with a purpose-built sitting animation will produce a better seated result. The Ribbits
model also has no separate head bone, so this example disables head tracking and uses its body bone plus
`name_tag_offset` for wanted-item placement.

Other popular GeckoLib mods vary considerably. Mowzie's Mobs, Creeper Overhaul, and Bosses of Mass Destruction ship
Gecko model and animation JSON that can be referenced. The inspected Minecraft 1.21.1 Naturalist and L_Ender's
Cataclysm jars contain entity textures but no `.geo.json` or `.animation.json` files, so their models cannot be used
this way.

Only the selected raw model, animation, and texture resources are reused. Customers does not inherit the other mod's
renderer code, extra render layers, emissive passes, dynamic texture or bone selection, particles, or custom
animation-controller state. Prefer models whose required animations use ordinary keyframes and test every mapped
state. Inspect an installed mod jar as a ZIP to find its resource paths, animation names, bones, and pivots.

Resource paths and animation names are implementation details of the providing mod and may change when it updates.
Pin and test the supported version in a modpack. Referencing installed files avoids copying them into the appearance
pack, but pack authors must still follow the originating mod's license and distribution terms.

The `sitting_pivot` is the model's hip position in Blockbench model units, where 16 units equal one block. Positive X
points right, positive Y points up, positive Z points forward, and the feet should be at Y zero. Customers aligns this
point to its standard seated hip at `[0, 12, 0]`, including the skin scale and the villager's facing direction.

`head_bone` defaults to `head`. Customers applies clamped player-looking rotation to that bone after JSON animation
and uses its current animated position to place wanted items. `name_tag_offset` moves the item row vertically from
the head bone; it does not define a separate overhead point. If the bone is absent, the model still renders, head
tracking is disabled, and wanted items use entity height.

#### Producing a model in Blockbench

Install [Blockbench](https://www.blockbench.net/) and its **GeckoLib Models & Animations** plugin. In Blockbench,
open **File > Plugins**, select **Available**, search for `GeckoLib`, and install the plugin. The
[official GeckoLib Blockbench guide](https://github.com/bernie-g/geckolib/wiki/Making-Your-Models-%28Blockbench%29)
covers installation, project creation, conversion, rigging, animation, and export. The
[current plugin guide](https://wiki.geckolib.com/docs/geckolib5/making-models/blockbench-plugin-usage/) is also a
useful reference for the Blockbench interface, although Minecraft 1.21.1 uses GeckoLib 4 rather than GeckoLib 5.

Create the project with **File > New > GeckoLib Animated Model** and select the entity model type. Set the project
name, object ID, namespace/mod ID, and texture size under **File > Project**. An existing Bedrock or modded entity
project can instead be converted with **File > Convert Project > GeckoLib Animated Model**. Keep the editable
`.bbmodel` source: importing an exported `.geo.json` later does not preserve every authoring detail.

Rig the model before animating it:

1. Put every cube in a group. GeckoLib animates groups as bones, not loose cubes.
2. Use one root group and parent the body, head, arms, and legs beneath it. Child bones follow their parent.
3. Move each bone pivot to the joint it rotates around: the neck for the head, shoulders for arms, hips for legs,
   and the model's hip for body movement.
4. Name the look-at bone `head`, or put its exact name in `head_bone`. Parent hair, hats, ears, and other head
   accessories to it, but do not parent torso geometry to it.
5. Place the standing model with its feet at Y zero. Record the hip pivot shown by Blockbench and use those X, Y,
   and Z values for `sitting_pivot`; Customers uses them to align the model with a chair or counter seat.
6. Create or import a PNG with the texture dimensions selected in the project, unwrap the cubes, and check all UVs
   in Blockbench's textured preview.

Switch to Blockbench's **Animate** workspace and add the animation names referenced by the skin JSON. Animation names
in the definition must exactly match the names exported under the animation file's `animations` object. Create these
three animations first:

- `misc.idle`: a looping standing animation. A subtle breathing or body movement keeps it from looking frozen.
- `move.walk`: a looping walk cycle with opposite arm and leg motion.
- `pose.sit`: a looping or held seated pose whose hips remain at the configured `sitting_pivot`.

Preview each loop through its final frame so it does not snap at the boundary. Add optional action and reaction
animations only after the three required states work. Avoid continuously keyframing the head bone's X and Y rotation
in locomotion animations when head tracking is enabled; Customers applies the player's look direction after the JSON
animation. Head translations and deliberate action poses are still supported.

Export all three runtime assets:

1. Use **File > Export > Export GeckoLib Model** and place the resulting `.geo.json` at the model path shown above.
2. Use **File > Export > Export GeckoLib Animations** and place the resulting `.animation.json` at the animation
   path shown above.
3. Right-click the texture in Blockbench's **Textures** panel, select **Save As**, and place the PNG at the texture
   path shown above.

The [Blockbench basics video](https://www.youtube.com/watch?v=QhPzgpapOWE),
[GeckoLib modeling video](https://www.youtube.com/watch?v=VlUwLXkwb2c), and
[GeckoLib animation video](https://www.youtube.com/watch?v=3srLEdFTgVI) provide visual walkthroughs. They use an
older Minecraft/GeckoLib project, but the Blockbench modeling, bone, pivot, UV, keyframe, and looping techniques still
apply. Customers handles the code integration, so pack authors should follow this page for names and file locations.

Blender can be used for reference geometry, sculpting, or previews, but GeckoLib does not document a direct Blender
production exporter. Finish cube geometry, pixel-aligned UVs, bones, pivots, animations, and export in Blockbench.

After editing, use `/reload` for data-pack definitions and `F3+T` for client model, animation, texture, or sound assets.
The repository's `examples/customers-gecko-skins` directory is a combined data/resource pack showing one ordinary
skin and one animated skin in the same selectable appearance.

