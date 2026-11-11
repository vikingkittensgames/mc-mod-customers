# Minecraft 1.21.11 to 26.1.2

This file records player-facing, server-admin, and pack-author differences
between the completed Minecraft 1.21.11 branch and the Minecraft 26.1.2
branch. Port Minecraft 1.21.1 to 1.21.11 first, then use this file for the
second port from 1.21.11 to 26.1.2. Behavior not listed here is intended to
remain the same.

## Supported runtime

- This branch supports Minecraft 26.1.2 only on Fabric and NeoForge. It does
  not declare compatibility with Minecraft 26.1 or 26.1.1.
- Minecraft 26.1.2 and this build require Java 25.
- The baseline optional integrations are GeckoLib 5.5.2 and FTB Quests
  26.1.2.9. GeckoLib remains optional unless an enabled Skins appearance uses
  an animated GeckoLib model.
- Production FTB Quests testing uses the loader-specific FTB Quests 26.1.2.9,
  FTB Library 26.1.2.8, and FTB Teams 26.1.2.4 artifacts. Fabric additionally
  requires Team Reborn Energy 5.0.0.

## Build tooling

- Compared with the 1.21.11 branch, the 26.1.2 branch moves from Java 21 to
  Java 25 and from mapped/remapped artifacts to Minecraft's unobfuscated
  artifacts.
- The 26.1.2 build uses Gradle 9.5.1, Architectury Loom 1.17.493 with the
  `loom-no-remap` plugin, and Shadow 9.6.1. It does not configure Parchment
  mappings or produce remapped loader jars.

## Appearance packs

- The appearance API, contextual properties, appearance-provided names,
  custom sounds, sitting pivots, head tracking, and wanted-item placement are
  intended to behave the same as on Minecraft 1.21.11.
- Minecraft 26.1.2 uses GeckoLib 5.5.2 or newer within major version 5.
- The GeckoLib 5 skin schema and logical resource IDs are unchanged from the
  Minecraft 1.21.11 branch. Self-contained assets remain under
  `assets/{namespace}/geckolib/models/customers/skins` and
  `assets/{namespace}/geckolib/animations/customers/skins`.
- Complete model, animation, and texture paths that reuse another mod's
  resources must still match that mod's Minecraft 26.1.2 jar.
- Minecraft 26.1.2 stores the vanilla witch texture at
  `textures/entity/witch/witch.png`. This affects the built-in Monsters
  appearance internally; appearance-pack authors only need to account for it
  when directly referencing vanilla witch resources.
- The MCA Reborn appearance remains a separate add-on and is not included in
  the main mod.

## Customer progress bar

The player-facing progress bar and wanted-item groups are unchanged. Minecraft
26.1.2 assigns an explicit UUID when the server creates a boss event and uses a
two-phase GUI extraction API. Customers immediately synchronizes the matching
snapshot whenever progress-bar visibility changes so Fabric and NeoForge both
render the wanted-item groups with the correct event.

## Counter selection marker

The selected counter position still bobs above the block and the surrounding
valid positions remain translucent green cubes. On Minecraft 26.1.2, the
selected position is a translucent color-coded cube for the active spawner
mode instead of the spinning textured marker block used by earlier branches.
This is a visual difference only and does not change counter selection.

## FTB Quests packs

FTB Quests 26.1.2 stores custom task definitions as JSON5. Customers writes
and reads its task fields through that format; the Customers task types,
conditions, counts, and in-game behavior are unchanged.
