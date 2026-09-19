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

