# Minecraft 1.21.1 to 1.20.1 Migration Notes

This document records the differences required when porting the Customers
common code from Minecraft 1.21.1 to the Forge 1.20.1 target.

## Build configuration

| Setting | Minecraft 1.21.1 | Minecraft 1.20.1 |
| --- | --- | --- |
| Java toolchain | 21 | 17 |
| Forge | 52.x | 47.4.0 |
| Parchment Minecraft | 1.21.1 | 1.20.1 |
| Run directory | `run-forge-1.21.1` | `run-forge-1.20.1` |

Only `common` and `forge` are changed and tested on this branch. The
`neoforge` module remains included for the later 1.21.1 build.

## API migration

1.20.1 predates the 1.21.1 data-component, structured-persistence, and common
payload APIs. These differences are implemented through the compatibility
packages and documented in `Compatability.md`.

Block item interactions use the older `InteractionResult` return type in
1.20.1. The 1.21.1 pass-to-default and skip-default item interaction values
are represented by `InteractionResult.PASS` in the port.

## Compatibility implementation details

The port keeps business logic in `common` and puts Forge network registration
and event integration in `forge`. The NeoForge module remains included for the
1.21.1 target but is not changed, built, or tested during this migration.

### Persistence and containers

Minecraft 1.20.1 block entities load with `load(CompoundTag)` and save with
`saveAdditional(CompoundTag)`. Registry providers are not passed to these
methods. `PersistedContainer` uses `ItemStack.save(CompoundTag)` and
`ItemStack.of(CompoundTag)` for the 1.20 representation.

### Networking

The 1.21.1 `CustomPacketPayload` and `StreamCodec` APIs are replaced with
Forge's `SimpleChannel`. Common payload records retain FriendlyByteBuf read and
write methods, while Forge registers message IDs and schedules client handlers
on the main thread.

Block-break confirmation uses this same split: the common prompt and confirm
records have `FriendlyByteBuf` serialization, while the Forge compatibility
network helper assigns their `SimpleChannel` message IDs and directions.

The customer leaderboard uses the same `SimpleChannel` path. Its common
payload record exposes `FriendlyByteBuf` read and write methods instead of the
1.21.1 payload type and stream codec declarations.

### Blocks, recipes, loot, and GUI

1.20.1 blocks use `Block#use` for empty-hand and item interactions and do not
use the newer block codec hooks. Recipes use `Consumer<FinishedRecipe>`, loot
providers do not receive registry providers, and GUI sprite rendering uses
`GuiGraphics.blit` with explicit texture dimensions. Checkboxes use the 1.20
constructor and synchronize through an `onPress` override.

Customer spawner level settings, including their inventories, use the shared
data migration code. The 1.20 persistence adapter preserves sparse inventory
slot indexes while reading NBT so older spawner inventories move to level one
without shifting or losing items.

Leaderboard and level screens use the 1.20 `renderBackground(GuiGraphics)`
and three-argument `mouseScrolled` screen methods.

Minecraft 1.21.1 uses `data/<namespace>/loot_table`, while Minecraft 1.20.1
uses `data/<namespace>/loot_tables`. Forge resource processing copies the
common loot tables to the 1.20.1 path for the Forge build.

Minecraft 1.21.1 uses `data/<namespace>/tags/block` for block tags, while
Minecraft 1.20.1 uses `data/<namespace>/tags/blocks`. The Customers avoid-block
tags use the 1.20.1 path on this branch.

### MCA appearance

The MCA Appearance is intentionally not included in the Minecraft 1.20.1 port.
It remains supported for Minecraft 1.21.1 and newer, where the MCA renderer and
entity APIs used by the appearance are available.

## Test status

The common and Forge test sources compile against 1.20.1. The test-only platform
helpers initialize the 1.20.1 built-in registries before invoking Forge's normal
bootstrap and tolerate the known headless Forge networking initialization error.
All common and Forge tests pass with the 1.20.1 compatibility implementations.
