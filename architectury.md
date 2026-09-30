# Architectury Migration Plan

This document plans the migration from the current custom multi-loader
architecture to Architectury Loom and Architectury API.

The migration has two separate goals:

1. Use Architectury Loom as the common Gradle toolchain for supported loaders.
2. Use Architectury API to replace loader-specific code where it provides an
   equivalent abstraction.

Architectury API is a required external dependency. Customers must not bundle
or shade it. Development runs must include the matching platform artifact, and
published loader metadata must require Architectury API at runtime.

Architectury does not replace Minecraft-version compatibility code. Utilities
that isolate changes to Mojang classes and methods remain valuable on each
Minecraft-version branch.

## Gradle Build Strategy

### Plugins and mappings

Replace ModDevGradle and ForgeGradle with Architectury Loom in `common` and
the loader modules supported by the branch. The initial branch targets are
Forge only for 1.20.1, then NeoForge and Fabric for 1.21.1 and newer.

Use:

- Architectury Loom for mapped Minecraft dependencies, development runs,
  remapping, and production JAR creation.
- The Architectury Gradle plugin for common-platform transforms and
  `@ExpectPlatform` support.
- Mojang official mappings, optionally layered with Parchment when a matching
  Parchment release exists.
- The existing Java version from `gradle.properties`.

Loom supports Fabric, Forge, and NeoForge and supports Mojang mappings. It is
still described upstream as experimental, so each Minecraft branch should pin
a tested Loom version instead of sharing an unbounded plugin version.

### Architectury API dependency

Use the common artifact while compiling `common`:

```groovy
modImplementation "dev.architectury:architectury:${architectury_version}"
```

Use the matching platform artifact in each branch's supported loader:

```groovy
// Fabric: 1.21.1 and newer
modImplementation "dev.architectury:architectury-fabric:${architectury_version}"
modImplementation "net.fabricmc:fabric-loader:${fabric_loader_version}"
modImplementation "net.fabricmc.fabric-api:fabric-api:${fabric_api_version}"

// Forge: 1.20.1 only
modImplementation "dev.architectury:architectury-forge:${architectury_version}"

// NeoForge: 1.21.1 and newer
modImplementation "dev.architectury:architectury-neoforge:${architectury_version}"
```

Fabric Loader and Fabric API are separate dependencies from Architectury API.
Keep their versions in `gradle.properties` on 1.21.1-and-newer branches and
select versions matching the Minecraft branch.

Do not add Architectury API to `include`, `shadow`, `shadowBundle`, or another
embedded-library configuration. Only transformed Customers common classes and
resources should be assembled into loader JARs.

All supported-loader metadata must declare its matching Architectury API
artifact as required with the configured `architectury_version_range`. Fabric
metadata must additionally declare compatible `fabricloader`, `fabric-api`,
Java, and Minecraft versions. The 1.21.1 NeoForge declaration is currently
optional and must become required. Forge metadata needs this declaration only
on the 1.20.1 branch.

### Modules

Retain the current module layout:

| Module | Responsibility after migration |
| --- | --- |
| `common` | Gameplay, registrations, Architectury events, networking, shared client initialization, resources, and unit tests |
| `fabric` | 1.21.1-and-newer Fabric entrypoints and metadata, configuration backend, unsupported events, transfer integration, recipe conditions, and compatible optional integrations |
| `forge` | 1.20.1-only Forge entrypoint, metadata, configuration registration, unsupported events, capabilities, recipe conditions, and loader-specific integrations |
| `neoforge` | 1.21.1-and-newer NeoForge entrypoint, metadata, configuration registration, unsupported events, capabilities, recipe conditions, and FTB Quests |
| `testsupport` | Shared test bootstrap and utilities that are not included in production JARs |

Do not combine supported loaders into one module. Loom gives them a consistent
build model, but each still needs its own loader dependency, metadata,
entrypoint, remapped artifact, and residual platform integrations.

The loader entrypoints should eventually do little more than:

1. Register loader-owned configuration and recipe conditions.
2. Initialize unavoidable loader integrations.
3. Call `Customers.initialize()`.
4. Invoke client initialization only in a client environment.

### Common code assembly

Use the standard Architectury project configurations:

- `common` or `namedElements` for development compilation.
- `transformProductionFabric` for the Fabric production artifact.
- `transformProductionForge` for the Forge production artifact.
- `transformProductionNeoForge` for the NeoForge production artifact.
- A bundle configuration containing only the transformed `common` project.
- Loom `remapJar` as the final distributable artifact.

The exact configuration names should follow the pinned Loom and Architectury
plugin versions because their templates have changed over time.

### Development tasks

Keep the loader-qualified root tasks:

| Root task | Delegates to |
| --- | --- |
| 1.20.1: `runClientForge`, `runServerForge`, `buildForge` | Forge module tasks |
| 1.21.1+: `runClientFabric`, `runServerFabric`, `buildFabric` | Fabric module tasks |
| 1.21.1+: `runClientNeoForge`, `runServerNeoForge`, `buildNeoForge` | NeoForge module tasks |

Architectury Loom provides the loader-specific client and server tasks. The
aliases remain useful because their names are stable for documentation,
IntelliJ, and manual testing.

Keep the working directories:

```text
run-forge-1.20.1
run-fabric-{minecraft_version}
run-neoforge-{minecraft_version}
```

This is preferable to sharing a run directory. It prevents loader-specific
worlds, configs, optional mods, and logs from contaminating another test.

### Loader game tests

Keep loader-dependent assertions out of `common` JUnit tests. `@ExpectPlatform`
and Architectury hooks resolve only when Minecraft is running under a concrete
loader, so a regular JVM test cannot verify their selected implementation.

Use a `gameTest` source set in each supported loader module and expose a
headless `runGameTest` task:

- Fabric registers its test class from a test-only `fabric-gametest` entrypoint
  and enables `fabric-api.gametest`.
- NeoForge uses `@GameTestHolder`, enables the Customers namespace, and starts
  the server with `neoforge.gameTestServer`.

Game-test classes and Fabric's test metadata must not be included in release
JARs. A test may use a loader-provided empty structure or a stable vanilla
template when it does not inspect the scene itself.

### Static resources

Do not use data generators or retain `runData` tasks. Maintain the recipes,
recipe-unlock advancements, loot tables, blockstates, and models as ordinary
JSON under `common/src/main/resources` when their behavior is identical for
every supported loader.

Keep a resource in a loader module only when its JSON syntax is loader-specific,
such as the Fabric and NeoForge recipe-condition declarations. Static block and
item models must use vanilla model formats. Counter geometry remains in static
element models; disabled spawner models use `minecraft:block/cube_bottom_top`
with flattened Customers-owned textures. Do not use `neoforge:composite`.

Minecraft loader startup and the loader GameTests validate the resource pack.
Do not add unit tests that assert static JSON contents, since those resources
must remain independently editable.

### Build and verification tools

Retain or add root tasks for:

- `buildAllLoaders`
- `testAllLoaders`
- `spotlessApply`
- `spotlessCheck`

For every loader artifact, verify:

- The remapped JAR starts with the required Architectury API installed.
- Startup fails with a useful dependency error when Architectury API is absent.
- Architectury classes are not packaged inside the Customers JAR.
- Common classes and resources occur exactly once.
- Client and dedicated-server runs both start.
- Static resources load on each supported loader without model, recipe, loot,
  or advancement errors.

Use `dependencies` and `dependencyInsight` to detect accidental Architectury
embedding or mismatched common/platform artifacts.

## Minecraft Version Strategy

### Recommendation

Keep release branches, but define them around tested Minecraft
source/binary-compatibility families rather than assuming all versions with
the same major or minor prefix are compatible.

Architectury API itself uses separate branches at Mojang API breakpoints.
Matching Architectury API major versions are evidence that releases may be
close, but they do not prove that Customers or its other dependencies can use
one binary.

Each Customers branch should have:

- One compile target in `gradle.properties`.
- One pinned Loom version.
- One Architectury API line.
- Explicit loader versions and dependency ranges.
- A declared Minecraft runtime range containing only versions manually tested
  with the produced JAR.
- Its own `run-{loader}-{version}` directories.

### Initial branches

| Customers branch | Compile target | Architectury API | Initial loaders | Runtime range policy |
| --- | --- | --- | --- | --- |
| `architectury-1.20.1` | 1.20.1 | 9.2.x | Forge only | 1.20.1 only |
| `architectury-1.21.1` | 1.21.1 | 13.0.x | Fabric and NeoForge | Start with 1.21.1; add 1.21 only after testing |
| `architectury-1.21.11` | 1.21.11 | 19.0.x | Fabric and NeoForge | Start with 1.21.11 only |
| `architectury-26.1` | latest 26.1 patch | 20.1.x | Fabric and NeoForge initially | Test 26.1, 26.1.1, and 26.1.2 before publishing one range |
| `architectury-26.2` | 26.2 | 21.1.x | Fabric and NeoForge initially | 26.2 only |
| `architectury-26.3` | 26.3 | 22.0.x | Fabric and NeoForge initially | 26.3 only |

Architectury API 9.2 supports Minecraft 1.20 and 1.20.1 on Fabric and Forge,
but does not provide a normal NeoForge 1.20.1 target. Customers will support
Forge only on its 1.20.1 branch.

Fabric and NeoForge should be first-class targets on every 1.21.1-and-newer
branch, with matching Architectury API, Fabric Loader, and Fabric API
artifacts. Forge remains confined to 1.20.1; do not treat the Forge-like
loaders as interchangeable platforms.

### Additional 1.20 and 1.21 families

If support expands to every modern Architectury line, use these as porting
boundaries:

| Minecraft family | Architectury API line | Branch recommendation |
| --- | --- | --- |
| 1.20 and 1.20.1 | 9.2.x | Forge-only 1.20.1 branch; do not add 1.20 without a separate compatibility decision |
| 1.20.2 | 10.1.x | Separate branch |
| 1.20.3 and 1.20.4 | 11.1.x | One branch only after both are tested |
| 1.20.5 | 12.0.x | Separate branch |
| 1.20.6 | 12.1.x | Separate branch |
| 1.21 and 1.21.1 | 13.0.x | One branch, if one Customers JAR passes both |
| 1.21.2 and 1.21.3 | 14.0.x | One branch only after both are tested |
| 1.21.4 | 15.0.x | Separate branch |
| 1.21.5 | 16.1.x | Separate branch |
| 1.21.6 | 17.0.x | Separate from 1.21.7 until proven compatible |
| 1.21.7 and 1.21.8 | 17.0.x | One branch only after both are tested |
| 1.21.9 and 1.21.10 | 18.0.x | One branch only after both are tested |
| 1.21.11 | 19.0.x | Separate branch |
| 26.1 through 26.1.2 | 20.1.x | One branch only after all patches are tested |
| 26.2 | 21.1.x | Separate branch |
| 26.3 | 22.0.x | Separate branch |

Avoid a single `1.21.x` source branch. Customers already has substantial
Mojang API differences between 1.21.1 and 1.21.11, including identifiers,
entity creation, villager records, rendering state, GUI pipelines, item
components, persistence, and interaction results.

When two versions share a branch, compile against the earliest version unless
a later patch is required by the loader toolchain. Test the exact production
JAR on every declared Minecraft version; recompiling separately is safer when
method descriptors or class locations differ.

## Minecraft 1.21.11 Port Details

This section records the differences and decisions verified while porting the
completed 1.21.1 Architectury implementation to 1.21.11. It is authoritative
for the `architectury-1.21.11` branch and must be updated as each compatibility
difference is implemented and tested.

### Build baseline

| Setting | 1.21.11 value |
| --- | --- |
| Java | `21` |
| Architectury Loom | `1.13.469` |
| Architectury API | `19.0.1` |
| NeoForge | `21.11.45` |
| NeoForge loader range | `[3,)` |
| Fabric Loader | `0.19.5` |
| Fabric API | `0.141.6+1.21.11` |
| Parchment | `1.21.11:2025.12.20` |
| FTB Quests | `2111.1.5` |
| Minecraft runtime range | `[1.21.11]` |

Architectury API, Fabric API, and FTB Quests publish 1.21.11 artifacts built
with Loom 1.13.x, so the branch retains Architectury Loom 1.13.469.

FTB Quests publishes shared, Fabric, and NeoForge 1.21.11 artifacts. Retain
the common Customers task implementation and the thin loader initialization
hooks established on the 1.21.1 branch.

MCA Reborn does not currently publish a 1.21.11 artifact. The MCA appearance
integration must remain disabled on this branch unless a compatible artifact
becomes available and is manually tested.

### Porting differences

The initial mapped compilation confirmed these package and type changes:

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` |
| `net.minecraft.advancements.critereon` | `net.minecraft.advancements.criterion` |
| `net.minecraft.Util` | `net.minecraft.util.Util` |
| `MobSpawnType` | `EntitySpawnReason` |
| `world.entity.npc` villager types | `world.entity.npc.villager` |
| `DimensionTransition` / `changeDimension` | `TeleportTransition` / `teleport` |
| `client.resources.PlayerSkin` | `world.entity.player.PlayerSkin` |
| `client.renderer.RenderType` | `client.renderer.rendertype.RenderType` |
| `DataComponentPredicate` for exact components | `DataComponentExactPredicate` |
| Offer costs could retain the stack's resolved component map | Build `DataComponentExactPredicate` from non-default components only; normalize offers loaded from affected 1.21.11 saves |
| `ItemInteractionResult.SUCCESS` | `InteractionResult.SUCCESS` |
| `ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION` | `InteractionResult.TRY_WITH_EMPTY_HAND` |
| `ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION` | `InteractionResult.PASS` |
| `DirectionProperty` | `EnumProperty<Direction>` |
| `ServerQuestFile.getInstance().ifPresent(...)` | `ServerQuestFile.ifExists(...)` |
| FTB task creation `Consumer<Task>` | `BiConsumer<Task, CompoundTag>` |
| `CompoundTag` scalar getters | Return `Optional` values |
| `ResourceKey.location()` | `ResourceKey.identifier()` |
| `RegistryAccess.registry` / `registryOrThrow` | `lookup` / `lookupOrThrow` |
| `Registry.get(Identifier)` for values | `Registry.getValue(Identifier)` |
| `ItemPredicate.Builder.of(value)` | `of(HolderGetter, value)` |
| Entity render methods operating directly on entities | Extract reusable render states, then submit them |
| Renderer-owned mutable model state reached the single active model | Carry custom poses through the shared render state so base and profession-layer models receive the same pose |
| One-parameter `BlockEntityRenderer<T>` | `BlockEntityRenderer<T, S>` with extraction and submission |
| Separate armor model-layer constants | `ArmorModelSet<ModelLayerLocation>` |
| FTB Library `ConfigGroup` | `EditableConfigGroup` |
| FTB Library config/UI packages | `client.config`, `client.config.gui`, and `client.gui.widget` |
| FTB Library `config.NameMap` | `util.NameMap` |
| `BlockEntityType.Builder.of(...)` | Construct through `IRegistrationHelper`; Fabric uses `FabricBlockEntityTypeBuilder` and NeoForge uses its access-transformed constructor |
| Block and item properties could be constructed without registry keys | Pass the matching `ResourceKey` into block and item factories and call `setId(...)` before construction |
| Item models were discovered directly under `models/item`, with custom-model-data `overrides` in the baked model | Add `items/<item>.json` item definitions and express custom-model-data selection with `minecraft:range_dispatch` |
| GUI text colors could omit the alpha byte | Supply explicit ARGB colors; a zero alpha byte renders text transparent |
| The old `GuiGraphics.blit` texture-region overload accepted a texture identifier directly | Use the render-pipeline overload through `GuiGraphicsCUtils.blit` |
| Advancement backgrounds used full `textures/...png` paths | Use texture identifiers without the `textures/` prefix or `.png` suffix |
| `minecraft:custom_model_data` accepted a numeric component value | Encode it as an object containing the `floats` list |
| Item-model textures were available through the combined block/item atlas | Item textures use a separate atlas; explicitly add non-`textures/item` model textures to `atlases/items.json` |
| Shaped-recipe ingredients used `{ "item": "<id>" }` and `{ "tag": "<id>" }` objects | Encode items as `"<id>"`, tags as `"#<id>"`, and alternatives as arrays of those strings |
| A shared `pack.mcmeta` described both mod resources and data | Omit it and let each loader supply metadata for the active pack type; Minecraft 1.21.11 resource format 75.0 rejects `supported_formats`, while data format 94.1 requires it for a shared range beginning at 75 |
| Loader GameTests used vanilla `@GameTest`; NeoForge discovered `@GameTestHolder` classes | Fabric supplies its own `@GameTest`; NeoForge registers test functions and `FunctionGameTestInstance` values |
| `Block.onRemove(...)` cleanup | `affectNeighborsAfterRemoval(ServerLevel, ...)` cleanup |
| `LevelHeightAccessor.getMinBuildHeight()` / `getMaxBuildHeight()` | `getMinY()` / `getMaxY()` |
| `GoalSelector(ProfilerSupplier)` | No-argument `GoalSelector()` |
| `SoundEvent.getLocation()` | `SoundEvent.location()` |
| `EntityType.create(Level)` | `EntityType.create(Level, EntitySpawnReason)` |
| Non-serializable vehicle entity types could be mounted | Server-side `startRiding` rejects vehicle types registered with `.noSave()`; the invisible customer seat must be serializable |
| NeoForge `common.util.TriState` | Vanilla `net.minecraft.util.TriState` |
| NeoForge biome data-map values are direct `VillagerType` values | Biome data maps return `ResourceKey<VillagerType>` |
| NeoForge `Capabilities.ItemHandler.BLOCK` and `IItemHandler` | `Capabilities.Item.BLOCK` and `ResourceHandler<ItemResource>` |
| Fabric resource conditions receive `HolderLookup.Provider` | Resource conditions receive `RegistryOps.RegistryInfoLookup` |
| Fabric world-render events under `client.rendering.v1` | Events under `client.rendering.v1.world`, with extracted camera render state |
| `BossHealthOverlay.render` stored its vertical offset in local-variable slot 3 | The added profiler local moves the vertical offset to slot 4; Fabric's boss-bar mixin must target slot 4 |
| NeoForge staged render event plus `Stage` enum | Typed events such as `RenderLevelStageEvent.AfterEntities` |
| Entity-render hooks receive pose stacks and buffers directly | Common renderers submit through `SubmitNodeCollector` using extracted `EntityRenderState` |
| `GuiGraphics.drawString(...)` returns an integer width | `drawString(...)` returns `void` |
| Block entities serialized directly through `CompoundTag` | Block entities use `ValueInput` and `ValueOutput`; Customers retains its 1.21.1 `Size`/`Items`/`Slot` inventory schema and reuses child/list handles so repeated writes append |

`Identifier` retains the factory methods used by Customers, including
`parse`, `fromNamespaceAndPath`, and `withDefaultNamespace`.

The 1.21.11 branch keeps Minecraft-version differences behind the existing
Customers compatibility utilities or thin loader adapters. Architectury API
remains the first choice for registration, networking, events, and client
registration when it exposes the required behavior.

Minecraft does not data-fix item stacks nested inside Customers-owned fields.
The persistence adapters therefore reproduce the established 1.21.1 sparse
inventory schema through the 1.21.11 value-input and value-output APIs.
Minecraft's `ValueOutput.child` and `childrenList` methods replace an existing
value each time they are called. `ValueOutputDataWriter` caches those handles
so the unchanged 1.21.1 customer and supplier persistence loops retain every
configured entry.

NeoForge GameTests must apply Loom's `gameTestServer` userdev template rather
than setting a property on a normal server run. Fabric continues to enable its
GameTest runner with `fabric-api.gametest`.

Standard customer and supplier entity renderer registration remains common
through Architectury. `CustomersVillagerAppearanceEntityRenderer` delegates
to the selected appearance renderer and then submits wanted-item icons using
the delegate's extracted name-tag state. This keeps wanted items visible
independently of name-tag rendering and removes both the Fabric
`EntityRendererMixin` and the NeoForge name-tag event adapter.

Architectury API 19 does not expose counter-marker world-render events.
Fabric therefore uses `WorldRenderEvents.AFTER_ENTITIES`; NeoForge uses
`RenderLevelStageEvent.AfterEntities`. Both adapters pass the pose stack,
camera render state, and model-view matrix to the same common renderer.

NeoForge 21.11 replaces the deprecated item-handler capability used by the
1.21.1 branch. Payment boxes expose `VanillaContainerWrapper`, while pickup
counters expose the transaction-aware `NeoForgeItemInsertionTarget` as a
`ResourceHandler<ItemResource>`. Fabric retains its loader-specific transfer
adapter because Architectury API 19 has no common inventory-transfer API.

Common tests must depend on Architectury through Loom's `modImplementation`
configuration. Adding the raw Architectury artifact directly to
`testRuntimeOnly` leaves intermediary Minecraft names on the JUnit runtime
classpath.

This section replaces the deleted
`Minecraft-1.21.1-to-1.21.11.md` pre-Architectury migration document.

## API Migration Areas

### Platform and optional-mod checks

Replace `IPlatformHelper.platformName`, `isModLoaded`, and development or
environment checks with `dev.architectury.platform.Platform`.

Remove the corresponding implementations from:

- `FabricPlatformHelper`
- `ForgePlatformHelper`
- `NeoForgePlatformHelper`
- `CustomersServices`

`Player.closeContainer()` is vanilla on the current branches, so
`IPlatformHelper.closeContainer` can be removed.

The NeoForge villager-type biome data map has behavior beyond Architectury's
generic biome hooks. Keep a narrow `villagerTypeForBiome` platform bridge until
there is an Architectury API that preserves NeoForge data-map overrides.

### Registration

Replace `IRegistrationHelper`, `CustomersRegistryEntry`, and most uses of
`CustomersRegistry` with:

- `DeferredRegister`
- `RegistrySupplier`
- `RegistrarManager`
- `RegistrarBuilder`

This moves standard blocks, items, entities, block entities, menu types,
sounds, statistics, and advancement triggers entirely into `common`.

Use `RegistrarBuilder.syncToClients()` for the custom appearance registry if
its behavior matches the current loader implementations.

Architectury registration does not automatically cover every loader lifecycle
for codec-backed data-pack registries. Keep a narrowly scoped platform bridge
for synchronized data-pack registry declaration if required after a spike.
Do not retain the general `IRegistrationHelper` solely for that operation.

### Networking

Replace `INetworkHelper`, `ForgeNetworkHelper`, and `NeoForgeNetworkHelper`
with `NetworkManager`.

Move payload type registration, codecs, receivers, thread queuing, and
send-to-player/send-to-server calls into `common`. Existing
`CustomPacketPayload` records and stream codecs already fit the Architectury
API shape.

Keep client-only receiver behavior behind client initialization so dedicated
servers never load client classes.

Architectury registers an S2C payload type separately on each physical side.
`CustomersNetworking` registers outbound S2C types only when
`Platform.getEnvironment()` is `SERVER`; client initialization registers the
matching receiver and therefore the client-side type. Do not register both
from shared initialization on a physical client, because Fabric rejects a
duplicate payload type registration.

### Item stack hooks

Replace `IItemStackHelper` and its identical Forge and NeoForge
implementations with `ItemStackHooks.getCraftingRemainingItem` on branches
where that method exists.

Keep `ItemStackCUtils` for Mojang-version differences such as:

- `onCraftedBy` signatures
- item/component equality
- `ItemCost` component predicates
- later vanilla crafting-remainder API changes

The class becomes a Minecraft-version shim rather than a loader service.

### Configuration

Architectury API does not provide a complete common configuration system.
Keep `IConfigHelper` or replace it with a similarly testable Customers-owned
configuration interface.

Use `config/customers-common.toml` as the shared Customers configuration
contract. It retains the existing keys, comments, defaults, validation rules,
and server-authoritative settings on every supported loader.

Forge and NeoForge config spec construction and registration remain in their
loader modules. NeoForge continues to use its registered `ModConfigSpec` and
the built-in Mods-page Config screen. Fabric reads and writes the same
`config/customers-common.toml` schema through a Customers-owned backend. Fabric
has no built-in mod configuration screen, so its settings are file-based until
an optional Mod Menu integration or Customers screen is added later. Fabric
configuration changes take effect after restarting the client or server.

Fabric must register Customers resource conditions matching the recipe-toggle
properties, so the three recipe settings behave the same way as NeoForge's
existing recipe conditions. Shared gameplay should continue to consume ordinary
values without importing a loader or third-party config API.

Do not use `@ExpectPlatform` for every configuration getter. A supplied
configuration object remains easier to unit test and avoids many generated
platform methods.

### Events

Move event subscription into `common` where Architectury supplies an
equivalent:

- block break and placement
- block and entity interaction
- entity add/remove
- chunk load/unload
- command registration
- server lifecycle
- server tick
- reload listener registration
- client lifecycle and logout where supported

Common callbacks should retain the existing gameplay methods and event-order
tests. Architectury cancellation results must be mapped carefully to the
current Forge/NeoForge behavior, especially handlers currently registered at
`LOWEST` priority.

`CustomersCustomerEvents` registers quick selling through
`InteractionEvent.INTERACT_ENTITY`. Architectury API 13.0.8 does not connect
that declared event to Fabric, so `CustomersFabricEvents` forwards Fabric
API's `UseEntityCallback` into the Architectury event. Revisit and remove that
small Fabric bridge when an Architectury update wires the event itself.

`CustomersFabricEvents` also uses Fabric API's `UseBlockCallback` for the
sneaking pickup-counter behavior. Fabric's callback does not have NeoForge's
independent block and item-use controls, so it calls the shared
`CustomerPickupCounterBlock.handleSecondaryUse` helper and returns success to
suppress the held item's use. The helper preserves the normal counter behavior:
an empty hand retrieves an item and a held stack is inserted as a stack.

Fabric API has no post-placement callback that supplies both the successful
placement's final state and its player. `BlockItemMixin` therefore injects at
the successful return from `BlockItem.placeBlock` and delegates to
`CustomerSpawnerCache.onBlockPlaced`. The mixin is server-only in behavior,
does not change placement results, and keeps cache updates and counter-placement
advancement events out of failed or claim-cancelled placements.

Fabric's `PlayerBlockBreakEvents.AFTER` and `ServerChunkEvents` directly cover
the remaining customer-spawner cache updates. They run after a successful break
and at chunk load/unload, respectively, so they preserve the cache semantics of
the NeoForge adapter without relying on a pre-action listener order.

`ServerPlayerGameModeMixin` has priority 900, below Fabric API's default 1000,
and injects at the pre-break call to `Block.playerWillDestroy`. That lets the
Fabric pre-break callback and any claim or protection listeners complete first;
when they cancel, vanilla returns before Customers opens a confirmation prompt.
The mixin then delegates only Customers spawner and leaderboard confirmation to
the shared `BlockBreakConfirmation` service.

Customer and supplier villagers reject dimension transfer directly from their
common entity classes by discarding themselves in `changeDimension`. This
replaces the late NeoForge entity-leave callbacks, which occurred after vanilla
had begun its transfer process.

Keep Customers block-break confirmation as a loader adapter at `LOWEST`
priority. Claim and protection mods must be able to cancel a break before
Customers opens its confirmation prompt; Architectury's common block-break
event has no equivalent listener-priority contract.

Keep loader adapters for events without a sufficient Architectury equivalent,
including specialized render stages, name-tag rendering, boss-overlay
customization, capability registration, and loader config screens.

For Fabric, first check Architectury events and then Fabric API callbacks
before introducing mixins. Keep any unavoidable mixin narrowly scoped to event
capture and call common Customers behavior from it.

Fabric registers counter-marker rendering through
`WorldRenderEvents.AFTER_ENTITIES`, using the same common renderer and
extracted camera state as NeoForge. Fabric has no individual boss-bar callback
with the information and cancellation needed by Customers. Its client-only
`BossHealthOverlayMixin` delegates customer bars to `CustomerBossBarRenderer`,
suppresses vanilla title rendering for those bars, and applies the shared
layout increment. Wanted-item rendering runs from the common
`CustomersVillagerAppearanceEntityRenderer` after the selected appearance
renderer submits, so it uses the same production path on Fabric and NeoForge.

### Commands

Use `CommandRegistrationEvent.EVENT` to register customer and supplier command
trees from `common`.

Remove the duplicated Forge and NeoForge command event classes after command
permissions and build-context arguments are verified on both loaders.

### Creative tabs

Use `CreativeTabRegistry` to append Customers blocks and items to vanilla
creative tabs from `common`.

This should remove the repeated `BuildCreativeModeTabContentsEvent` handlers
from customer spawners, supplier spawners, payment boxes, pickup counters, and
leaderboards.

### Entity attributes

Use `EntityAttributeRegistry.register` in `common` for customer and supplier
attribute suppliers.

Remove the duplicated `EntityAttributeCreationEvent` handlers after entity
startup is verified on both loaders.

### Reload listeners and economy lifecycle

`CustomersEconomyEvents` registers `EconomyDataReloadListener` through
`ReloadListenerRegistry`, initializes the economy at
`LifecycleEvent.SERVER_STARTED`, and advances it at `TickEvent.SERVER_POST`.
This removes the NeoForge-only economy event adapter and gives Fabric the same
reload and lifecycle behavior.

### Menus and screens

`CustomersClientRegistrations` uses `MenuRegistry` for the customer and
supplier spawner screens. The common screen implementations and their
registration now run from each loader's client entrypoint.

### Entity renderers and model layers

`CustomersClientRegistrations` uses `EntityRendererRegistry`,
`EntityModelLayerRegistry`, and `BlockEntityRendererRegistry` for customer and
supplier entities, the customer seat, the customer model layer, and the pickup
counter renderer. It also registers built-in monster and skin appearances, and
the optional MCA appearance only when MCA is present.

It also uses `ClientPlayerEvent.CLIENT_PLAYER_QUIT` to clear synchronized
customer-spawner snapshots when the player leaves a level.

Optional integration initialization must still check `Platform.isModLoaded`
before loading classes from the optional mod.

### HUD, name tags, and world rendering

Architectury API does not expose direct equivalents for every current
Forge/NeoForge client render event.

Keep thin loader adapters for:

- customer boss-bar replacement
- render-name-tag decisions and custom item rendering
- counter-marker world rendering
- loader configuration screens

The render algorithms, marker models, and state remain common. Only event
capture and loader-specific render submission should remain in loader modules.

### Inventories and automation

Architectury API does not provide one common abstraction covering Forge item
handlers, NeoForge 21.11 resource handlers, and Fabric transfer APIs.

Keep:

- `ItemInsertionTarget`
- a transaction-aware Fabric Transfer API adapter for pickup counters; payment
  boxes use the automatic `Container` fallback
- Forge item-handler wrappers
- NeoForge `ResourceHandler<ItemResource>` wrappers
- loader capability or transfer registration

Continue using vanilla `Container` behavior for gameplay and persistence.
Loader modules should only expose that behavior through their capability API.

Re-evaluate this area independently on later Architectury versions; do not
assume a newer transfer abstraction has the same simulation, filtering, or
extraction semantics.

### Recipe conditions

Recipe-condition APIs remain loader-specific. Keep
`RecipeEnabledCondition` and its serializer registration in the Forge and
NeoForge modules, and provide the equivalent Fabric resource-condition
registration when the recipe must be disabled.

Share the condition's semantic decision code and configuration values, but
allow loader-specific codecs, registration, and static JSON.

### Static resources

Architectury API does not unify Fabric, Forge, and NeoForge data generators.
Avoid that difference entirely by maintaining vanilla-format JSON under
`common/src/main/resources`. Loader-specific recipe conditions remain the only
resource split when a loader requires a distinct condition codec.

### FTB Quests

FTB Quests 2111.1.x publishes compatible Fabric and NeoForge artifacts.
Customers keeps its task types, trigger schemas, configuration UI, and event
listeners in `common`; thin loader client and server hooks initialize them
only when FTB Quests is present. Each loader supplies its matching FTB Quests
runtime artifact.

### MCA and other optional mods

Use `Platform.isModLoaded` for optional-mod detection.

Keep compile dependencies and direct API integration in only the loader and
Minecraft-version modules where compatible artifacts exist. Shared reflection
or shared interfaces may remain common when they avoid eagerly loading absent
classes.

Do not advertise an integration on Fabric merely because the other mod has a
Fabric artifact. Confirm that the API classes and behavior Customers uses are
available on that loader and Minecraft version.

### Persistence

Keep the existing Customers persistence layer:

- `DataReader`
- `DataWriter`
- `CompoundTagDataReader`
- `CompoundTagDataWriter`
- `PersistedContainer`
- `PersistenceCUtils`

These classes normalize Minecraft-version serialization changes and preserve
saved-data compatibility. They solve a different problem from Architectury.

### Minecraft-version compatibility utilities

The following utilities generally remain because Architectury does not
normalize Mojang API changes:

- `ComponentCUtils`
- `EntityCUtils`
- `InteractionCUtils`
- `LevelCUtils`
- `PlayerCUtils`
- `ProfileCUtils`
- `VillagerCUtils`
- `TextureC`
- `GuiGraphicsCUtils`
- `BossBarCUtils`
- `RenderingCUtils`
- `DebugBoxC`

Review each utility on every version branch and remove it only when all
supported versions use the same vanilla API. Do not replace straightforward
Minecraft-version shims with `@ExpectPlatform`; that would incorrectly model
a version difference as a loader difference.

### Remaining Customers platform bridges

After the migration, `CustomersServices` should be removed or reduced to
genuinely Customers-owned injectable behavior such as configuration.

For small unavoidable loader calls, prefer a focused `@ExpectPlatform` method
over a broad service interface. Retain ordinary interfaces when tests need to
inject behavior or when several related configuration values form one logical
contract.

## Migration Order

1. Convert the 1.21.1 Gradle build and reproduce current NeoForge client,
   server, test, and build tasks without changing gameplay code.
2. Make Architectury API required in NeoForge dependencies and metadata.
3. Add the Fabric module, metadata, entrypoints, development runs, and a
   minimal smoke-test build before porting gameplay integrations.
4. Replace platform checks and crafting-remainder services. Move assertions
   that exercise those APIs into Fabric and NeoForge GameTests.
5. Replace standard registration and registry handle types.
6. Replace networking.
7. Move supported server events, commands, creative tabs, attributes, reload
   listeners, and lifecycle hooks to common.
8. Move supported screen, renderer, and model-layer registration to common.
9. Add Fabric adapters for configuration, transfers, resource conditions, and
   unsupported events.
10. Reduce remaining loader modules to configs, capabilities or transfers,
    recipe conditions, and optional integrations.
11. Remove obsolete services and `META-INF/services` descriptors.
12. Port the resulting architecture to the other Minecraft branches one
    compatibility family at a time.

Each step should keep the branch's supported loader builds usable. Fabric can
initially start with only enough integration to launch, but it is not
release-ready until its gameplay, persistence, automation, networking, and
client behavior pass the same verification as NeoForge. Do not combine the
Gradle toolchain conversion and all API migration into one untestable change.

## Required Test Matrix

For every supported branch:

| Area | Verification |
| --- | --- |
| Common logic | `:common:test` |
| Loader-dependent common behavior | `:fabric:runGameTest` and `:neoforge:runGameTest` |
| 1.20.1 Forge logic | `:forge:test`, build, client smoke test, dedicated server smoke test |
| 1.21.1+ Fabric logic | `:fabric:test`, build, client smoke test, dedicated server smoke test |
| 1.21.1+ NeoForge logic | `:neoforge:test`, build, client smoke test, dedicated server smoke test |
| Static resources | Loader startup and GameTests, with no generated-resource directory |
| Dependencies | Start with Architectury installed and confirm a clear failure without it |
| Packaging | Inspect JAR contents and confirm Architectury API is not embedded |
| Version range | Launch the same production JAR on every declared Minecraft version |
| Automation | Exercise insertion, filtering, extraction, and simulation through each loader's item-transfer API |
| Optional mods | Launch with and without each integration available for that loader; keep FTB Quests in the NeoForge matrix until its Customers integration is ported |

## Sources

- [Architectury Loom](https://github.com/architectury/architectury-loom)
- [Architectury Loom documentation](https://docs.architectury.dev/loom/introduction/)
- [Architectury API source](https://github.com/architectury/architectury-api)
- [Architectury API releases on CurseForge](https://www.curseforge.com/minecraft/mc-mods/architectury-api/files/all)
- [Architectury templates](https://github.com/architectury/architectury-templates)
- [Fabric Loader development setup](https://docs.fabricmc.net/develop/getting-started/setting-up-a-development-environment)
- [Fabric API source](https://github.com/FabricMC/fabric)
- [Fabric Transfer API](https://github.com/FabricMC/fabric/tree/1.21.1/fabric-transfer-api-v1)
