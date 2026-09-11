# Loader Compatibility

This document records how Customers supports multiple mod loaders for the
same Minecraft version. It covers project organization, loader abstractions,
known behavioral differences, development tasks, and migration decisions.

The initial target is Minecraft 1.21.1 on NeoForge and Forge. Minecraft
version differences remain documented separately in `Compatability.md` and
the applicable Minecraft-version comparison document.

Fabric is not currently supported. Compatibility services should remain
focused on Customers behavior so another loader can be added later without
moving gameplay logic out of the common module.

## Project Organization

The project adapts the 1.21.1 MultiLoader Template structure:

| Module | Responsibility |
| --- | --- |
| `common` | Loader-neutral gameplay, models, screens, entities, blocks, resources, compatibility interfaces, and tests |
| `neoforge` | NeoForge entrypoints, service implementations, events, registration, networking, capabilities, configuration, and data generation |
| `forge` | Forge entrypoints, service implementations, events, registration, networking, capabilities, configuration, and data generation |
| `testsupport` | Test-only ServiceLoader contract and shared Minecraft bootstrap dispatcher |

Loader modules compile and package the common Java sources and resources.
The common module cannot import classes from NeoForge or Forge. Loader
modules may use all public common code.

The build uses the template-compatible Gradle 8.10 wrapper. Gradle and
IntelliJ IDEA must run the build with Java 21; using Java 25 to launch
Gradle 8.10 fails before the project toolchain can be selected.

Loader entrypoints and event adapters translate loader lifecycle events into
loader-neutral initialization methods and callbacks. Loader event objects
must not be exposed to common code.

## Service-Loader Architecture

Customers follows the MultiLoader Template service-loader pattern. Common
code declares loader compatibility interfaces and discovers the active
loader's implementation with Java `ServiceLoader`.

Common services belong under:

```text
com.vikingkittens.mc.customers.compatability
```

Client-only services belong under:

```text
com.vikingkittens.mc.customers.client.compatability
```

Loader implementations remain in the corresponding compatibility package
inside each loader module. Each module registers its implementations with
files under `META-INF/services`.

Services should be focused rather than collected into one large interface:

| Service | Responsibility |
| --- | --- |
| `IPlatformHelper` | Loader identity, development-environment checks, optional-mod checks, and general environment information |
| `IItemStackHelper` | Stack-sensitive lifecycle hooks added by both loaders, including crafting remainders |
| `IRegistrationHelper` | Registration of blocks, items, entities, menus, sounds, and other registry entries |
| `INetworkHelper` | Payload registration, sending, direction validation, and handler scheduling |
| `IConfigHelper` | Loader-neutral access to validated configuration values |
| `IInventoryHelper` | Loader capability exposure and automation integration |
| `IClientPlatformHelper` | Client lifecycle, screens, renderers, HUD hooks, and other client-only integration |

The final set of interfaces may be divided further when a service would
otherwise expose unrelated responsibilities.

Client services must be loaded lazily from client initialization. Common
server code and dedicated-server startup must never load a client service or
its implementation.

Service interfaces describe Customers behavior rather than mirror loader
APIs. Loader event, payload-context, capability, registry-holder, and config
types must not appear in their method signatures.

## Shared Classes and Loader Adapters

Common behavior remains concrete whenever possible. Loader integration
remains thin.

| Existing concern | Preferred structure |
| --- | --- |
| Gameplay behavior | Concrete common class |
| Mod entrypoint | Separate thin NeoForge and Forge entrypoints |
| Event handling | Common behavior methods called by loader event adapters |
| Registration | Common declarations accessed through registration services |
| Networking | Common payload data and codecs with loader registration adapters |
| Configuration | Common getters backed by loader service implementations |
| Capabilities | Common inventory behavior exposed by loader capability adapters |
| Client hooks | Common renderers and screens registered by loader client adapters |
| Commands | Common command construction registered by loader event adapters |
| Data generation | Shared loader-neutral algorithms with loader provider adapters |

Abstract base classes are used only when:

1. The base depends exclusively on Java and Minecraft types.
2. Most behavior is genuinely identical.
3. Loader subclasses provide a small number of ordinary hooks.
4. The loader framework can safely instantiate or register the subclasses.

Abstract bases should not be introduced only to share annotations, static
event subscribers, loader event parameter types, or registry-holder types.
Forge and NeoForge annotations and event buses remain in separate thin
adapter classes.

Registered gameplay classes such as entities and block entities remain
common concrete classes. Creating loader subclasses for them would duplicate
registered types and complicate factories, persistence, casts, and tests.
Loader-specific capabilities and lookups are supplied through services or
adapters instead.
Methods patched onto otherwise vanilla Minecraft classes are tested in each loader module, even when shared callers access them through a
service interface. Common tests cannot compile against loader-patched methods.
Loader-neutral registration handles live in shared feature facades rather than entity classes. This prevents ordinary entity class loading,
including `instanceof` checks, from triggering registration side effects. Existing facade aliases may remain for caller compatibility.
When both loaders patch the same overridable Minecraft method but the vanilla common compile surface omits it, the shared subclass may provide
the matching public method without `@Override`; normal JVM virtual dispatch still invokes it on loader runtimes.
Shared search and gameplay utilities accept registered blocks, configuration values, or suppliers as explicit inputs instead of reaching
through loader-resident feature facades.
Loader-backed configuration reads should be deferred behind suppliers when shared or unit-tested behavior needs the value. Tests inject
ordinary values without bootstrapping the loader configuration lifecycle.

## Development Tasks

The root project provides consistently named loader tasks:

| Purpose | NeoForge | Forge |
| --- | --- | --- |
| Client | `runClientNeoForge` | `runClientForge` |
| Server | `runServerNeoForge` | `runServerForge` |
| Data generation | `runDataNeoForge` | `runDataForge` |
| Build loader JAR | `buildNeoForge` | `buildForge` |

`buildAllLoaders` builds both distributable loader JARs.
`testAllLoaders` runs common and loader-specific tests.

Qualified module tasks remain available, including `:neoforge:runClient` and
`:forge:runClient`. IntelliJ IDEA run configurations use explicit loader
names so both environments can coexist.

Each loader uses a separate run directory. NeoForge uses
`run-neoforge-1.21.1`, and Forge uses `run-forge-1.21.1`. Worlds,
configuration, installed test mods, and generated logs must not be shared
between loaders.

## Loader Abstractions

| Functionality | Common-facing design | NeoForge | Forge |
| --- | --- | --- | --- |
| Loader and optional-mod checks | `IPlatformHelper` | NeoForge `ModList` and environment | Forge `ModList` and environment |
| Initialization | Common initialization methods | NeoForge `@Mod` entrypoint | Forge `@Mod` entrypoint |
| Registry entries | Registration service and common suppliers | NeoForge deferred registers | Forge deferred registers |
| Common/server events | Common behavior methods | NeoForge event adapter | Forge event adapter |
| Client events | Client service and common behavior | NeoForge client events | Forge client events |
| Networking | `INetworkHelper` | NeoForge payload registrar | Forge payload registration |
| Configuration | `IConfigHelper` | NeoForge `ModConfigSpec` | Forge `ForgeConfigSpec` |
| Inventory automation | Common inventory plus `IInventoryHelper` | NeoForge item-handler capability | Forge item-handler capability |
| Villager biome type | `IPlatformHelper.villagerTypeForBiome` | NeoForge villager-type data map | `VillagerType.byBiome` |
| Recipe conditions | Loader-owned condition adapter | NeoForge condition | Forge condition |
| Data generation | Shared algorithms and loader entrypoint | NeoForge providers | Forge providers |
| Rendering and HUD | `IClientPlatformHelper` | NeoForge render events | Forge render events |
| Economy lifecycle | Common `Economy.serverStarted` and `serverTick` methods | NeoForge server event adapters | Forge server event adapters |

Optional economy integrations use `IPlatformHelper.isModLoaded` before reflective access to the
other mod's public pricing API. This keeps Village Shop System and ProjectE optional and prevents
their loader-specific artifacts from becoming common compile or runtime dependencies. Forge and
NeoForge server-start and post-tick events call the same common economy lifecycle methods. Each
loader also registers the common economy data reload listener through its server-resource reload
event so datapack and administrator configuration definitions have equivalent merging behavior.

## Important Loader Differences

### Registration

NeoForge and Forge both support deferred registration, but their packages,
holder types, registry keys, and lifecycle APIs differ.

Common code exposes stable suppliers for registered objects. Loader
implementations own when and how those suppliers receive their values.

`IRegistrationHelper` accepts vanilla registry keys and returns
`CustomersRegistryEntry`, which exposes only `get`, `getKey`, and `getId`.
Each loader provider retains its deferred registers until its entrypoint
binds them to that loader's mod event bus.

Custom registries use `CustomersRegistry`, a deferred vanilla-registry
handle. NeoForge can create its registry immediately. Forge declares it
through `DeferredRegister.makeRegistry`, creates a vanilla wrapper during
`NewRegistryEvent`, and resolves the wrapper only after that lifecycle step.
The villager appearance registry is the first custom registry migrated to
this handle. Loader registration helpers own custom-registry lifecycle
events, so feature packages do not require loader-specific registry event
subscribers. The default appearance is registered through the same shared
registration service as ordinary registry entries. Built-in extension
appearances keep registration in their feature packages and expose explicit
initializers that loader entrypoints invoke before binding registrations.
Data-pack registry declarations are retained by the registration service and applied during each loader's data-pack registry event.
The shared declaration supplies the same codec for persistent and network synchronization on both loaders.
The shared Skins initializer registers its provider and declares both synchronized data-pack registries before the loader registration
helper is bound to its event bus.

### Events

Both loaders divide lifecycle and gameplay events across event buses, but
event names, packages, payloads, and registration conventions can differ.
Thin loader adapters unwrap events and call common methods with Minecraft
objects and ordinary values.

### Networking

`INetworkHelper.sendToPlayer` accepts shared custom payloads. NeoForge sends payloads through its payload-aware distributor; Forge wraps the
payload in vanilla `ClientboundCustomPayloadPacket` because its distributor API accepts complete packets.
Gameplay code sends outbound payloads through this service and does not depend directly on either loader's networking API.

Payload records and codecs can remain common when they depend only on
Minecraft classes. Registration, direction constraints, thread scheduling,
player lookup, and sending are loader-specific.

Payload handlers enter common code with Minecraft objects and decoded payload
data rather than loader payload contexts.
Network snapshot value models live in `common`. Factories that read loader-module gameplay entities remain with those entities until the
entity feature itself moves to `common`.
Payload records and codecs move to `common` independently of their handlers. Loader handlers receive loader contexts and delegate decoded
payloads to shared or client behavior.
All customer client-bound payload records and codecs are shared. NeoForge currently owns the loader-context handlers and registration event;
Forge will provide corresponding adapters around the same payload types and codecs.

### Inventories and Automation

NeoForge and Forge expose related item-handler capability concepts from
different packages. Common block entities own inventory behavior and demand
rules without importing either capability API.

Loader adapters expose common inventories through the appropriate
capability. Pickup counters retain their demand-aware insertion rules through
a loader-neutral inventory interface.
`PersistedContainer` provides shared vanilla `Container` behavior while preserving the former `ItemStackHandler`
`Size`/`Items`/`Slot` persistence layout so existing inventories load without a data-version migration.
Customer and supplier spawners use this shared container directly for persistence, menus, item drops, and offer construction.
Pickup counters also use `PersistedContainer` for stored stacks, while their demand-aware automation input remains a loader capability adapter.
`ItemInsertionTarget` expresses only shared filtered-insertion behavior. Loader adapters expose it as a virtual input slot and reject extraction.
Forge and NeoForge use equivalent item-handler adapter behavior here; only the loader interface package differs.
Payment boxes use vanilla container behavior and shared atomic insertion logic. Loader capability registration creates the loader-specific
inventory wrapper rather than storing it in the block entity.
`CustomerPaymentBoxBlockEntity` and its loader-neutral behavior tests live in `common`; loader modules test only their capability adapters.
The payment-box block and registry declarations also live in `common`. Loader event adapters add its items to creative tabs and expose its
container capability.
`IRegistrationHelper.registerBlockEntityType` adapts a shared `BiFunction` factory to each environment's accessible
`BlockEntityType.Builder` supplier type.

### Configuration

`IConfigHelper` exposes validated behavior values to shared code. Each loader implementation reads its own native config spec; only loader
entrypoints register those specs with their loader lifecycle.
Shared-bound gameplay callers obtain numeric configuration through `CustomersServices.config()`. Loader-native config classes remain confined
to provider implementations and loader-only registration or condition code.
Boolean command, interaction, and recipe-condition decisions also use `IConfigHelper`. NeoForge's native `Config` is referenced only by its
service provider and entrypoint registration.

NeoForge and Forge provide similar config-spec systems from different
packages. Common gameplay code uses plain configuration getters supplied by
`IConfigHelper`.

Defaults, validation ranges, file names, and reload behavior must remain
equivalent across loaders.

### Data Generation

Generated resources are loader-neutral whenever their JSON format is
vanilla. Loader-specific conditions and model extensions require
loader-specific providers or post-processing.

Both loader data-generation tasks must produce equivalent common resources.

### Optional Mod Integrations

Optional integrations are enabled only when a compatible artifact and
runtime mod are available for the active loader. Integration classes must not
be loaded when their target mod is absent.

MCA appearance support requires separate dependency and compatibility checks
for NeoForge and Forge. Lack of a compatible MCA build for one loader
disables only the MCA appearance on that loader.

## Migration Log

### Initial Audit

The original project was a single NeoForge module. NeoForge APIs appeared in
registration, events, commands, configuration, networking, item handlers,
villager biome mapping, client rendering hooks, appearance registration,
recipe conditions, and data generation.

The migration keeps NeoForge runnable while extracting common code, then adds
Forge through parallel service implementations and loader adapters.

The initial module cutover moves the original source tree intact into the
`neoforge` module. Loader-neutral sources are then moved into `common` in
tested feature-sized batches. This keeps a working loader as the baseline
instead of attempting a single all-or-nothing source split.

The Forge scaffold uses Forge `52.1.16` for Minecraft 1.21.1 and
ForgeGradle 6. Loader runs share `run-forge-1.21.1` with each other but do
not share worlds, configuration, or logs with NeoForge.

Loader-neutral gameplay and Minecraft-version compatibility utilities move
to `common` before their callers. Tests that still require a loader-aware
Minecraft bootstrap remain in the loader module until that bootstrap is
separated from the shared test behavior.

Player-facing handwritten assets and shared data live in `common`. Loader
modules retain loader metadata, service descriptors, genuinely
loader-specific resources, and their own generated resources. Forge writes
to `forge/src/generated/resources`, while NeoForge writes to
`neoforge/src/generated/resources`.

Shared persistence uses vanilla NBT and item-stack codecs. Its item-list
encoding preserves the former NeoForge `ItemStackHandler` layout, including
the integer `Slot` and `Size` fields, so existing saved data remains
compatible on both loaders.

Shared registry declarations use `IRegistrationHelper` and
`CustomersRegistryEntry`. Each loader translates those declarations into its
own deferred registers and binds them to its mod event bus after feature
initialization. Customer and supplier entity and profession registrations,
followed by both spawners, payment boxes, and pickup counters, are the first
live features migrated to this service. Loader event listeners such as
creative-tab population and capability attachment remain in the loader
module.

`Customers` in `common` owns only shared mod identity. Loader entrypoints
use explicit names such as `CustomersNeoForge` and delegate registration and
lifecycle work without making the shared class depend on loader annotations
or event buses.

Common unit tests bootstrap vanilla Minecraft through NeoForm without
loading a mod loader. `IPlatformTestHelper` selects the vanilla, NeoForge,
or Forge bootstrap through test-runtime `META-INF/services` descriptors.
Common tests use a test-only `VanillaRegistrationHelper` that creates
in-memory vanilla registries and registers values immediately.
Loader modules retain integration tests that require patched loader APIs,
loader services, or loader startup state. Test-support classes are not
included in distributable mod JARs.

Forge uses its own `@Mod` entrypoint and generated `META-INF/mods.toml`. The entrypoint forces shared feature declarations to initialize
before binding `ForgeRegistrationHelper` to the Forge mod event bus.

`Customers.initialize()` is the loader-neutral registration entrypoint. Both loader entrypoints call it before binding their registration
helper to the loader event bus. Loader-specific event and capability adapters are registered separately afterward.

Forge attaches an `InvWrapper` to the shared payment-box block entity through `AttachCapabilitiesEvent`. NeoForge registers its equivalent
capability through `RegisterCapabilitiesEvent`. Vanilla hoppers can use the shared `Container` directly; these adapters support loader-aware
modded automation.

Pickup-counter block, item, and block-entity declarations use the shared registration service. NeoForge creative-tab and item-handler
capability events live in `CustomerPickupCounterNeoForgeEvents`, leaving the feature facade free of loader APIs in preparation for moving the
complete feature to `common`.

Loader entrypoints live in `com.vikingkittens.mc.customers`. Loader-specific code remains organized by feature when it implements customer-
or supplier-specific lifecycle behavior. The `compatability` packages contain only reusable loader services and generic adapters.

Customer and supplier spawner declarations use the shared registration service. Their NeoForge creative-tab listeners are isolated in
`CustomerSpawnerNeoForgeEvents` and `SupplierSpawnerNeoForgeEvents`.

Core customer and supplier blocks, block entities, menus, entities, AI goals, registration declarations, inventory behavior, and associated
unit tests live in `common`. Loader modules retain lifecycle events, commands, payload registration, client integration, capability adapters,
and data generators.

Menu construction uses `IRegistrationHelper.registerMenuType` because Forge and NeoForge expose a constructor and nested factory that vanilla
mappings keep private. Closing a player menu uses `IPlatformHelper.closeContainer` because loader patches change the visibility of
`Player.closeContainer`. Common gameplay uses vanilla `BlockState.isAir()` rather than loader-added empty-state helpers.

Both loaders initialize all shared customer and supplier registry declarations through `Customers.initialize()`. Forge feature adapters
register customer and supplier attributes, creative-tab items, dimension-change cleanup, and the pickup counter's input-only item capability.

Loader-neutral screens, renderers, client appearance implementations, render proxies, synchronized client state, and layout logic live in
`common`. Loader modules retain only client lifecycle events, render-stage hooks, name-tag events, payload handlers, and loader-specific
configuration screens.

Forge client MOD-bus adapters register shared customer and supplier screens, entity renderers, the pickup-counter renderer, model layers, and
client appearance implementations. Client implementation remains in `common`; only Forge event subscriptions remain in the Forge module.

The shared resource root includes `pack.mcmeta`. Minecraft 1.21.1 uses resource-pack format 34 and data-pack format 48, so the mod pack
declares the inclusive supported-format range 34-48. Forge requires this metadata to create valid resource-pack information for the mod.

Command trees and handlers live in `common` and accept a Brigadier `CommandDispatcher`. Thin Forge and NeoForge event subscribers pass their
loader-specific `RegisterCommandsEvent` dispatcher into the shared commands.

Player interaction decisions live in common classes receiving vanilla Minecraft arguments. Forge and NeoForge event adapters translate
loader-specific event result types and cancellation APIs around those shared decisions.

NeoForge registers custom payload codecs through `RegisterPayloadHandlersEvent`, while Forge creates a versioned `PayloadChannel`. Both loader
implementations delegate received customer payloads to the same client handlers in `common`.

Forge and NeoForge use loader-specific client gameplay event subscribers for boss-bar customization and logout notifications. Both adapters
render through the common `CustomerBossBarRenderer` and clear the common synchronized snapshot and marker managers.

Forge and NeoForge expose different name-tag render result types and different nameplate-distance hooks. Their client event adapters resolve
those differences and pass the final visibility decision to the common `CustomerWantedItemsRenderer`.

Forge and NeoForge retain loader-specific render-stage event adapters. Counter-marker rendering and debug-box submission live in `common` and
accept vanilla `PoseStack` and camera-position arguments.

Each appearance implementation owns its loader-specific client registration adapter in its feature package. The central customer renderer is
independent of Monsters, Skins, MCA, and other optional appearances.

Conditional recipe resources are loader-specific. NeoForge uses a `neoforge:conditions` array and registers `ICondition` codecs in its
condition-codec registry. Forge uses a singular `forge:condition` object, Forge's `ICondition` signature with `DynamicOps`, and
`ForgeRegistries.Keys.CONDITION_SERIALIZERS`. Both implementations read the same settings through `IConfigHelper`.

Recipe providers use portable vanilla APIs and live in `common`. Data-generator registration and block-loot providers remain loader-specific
because Forge and NeoForge patch or widen those APIs beyond the common NeoForm surface. Block-state and model providers also remain
loader-specific. Their model-generator APIs are structurally equivalent but use different loader namespaces, so the two providers are mirrored
with identical model geometry, textures, render types, composite children, facing variants, and item transforms.

Each loader writes its complete data-generator output to its own `src/generated/resources` directory and packages that directory only in its
own artifact. Handwritten resources remain shared in `common/src/main/resources`. This avoids collisions between Forge's `forge:composite` and
NeoForge's `neoforge:composite` model loader IDs while allowing each loader's `runData` task to operate independently.

Customer and supplier entities obtain biome-specific villager types through
`IPlatformHelper`. This keeps NeoForge data-map access and Forge's patched
vanilla biome mapping out of shared gameplay logic.

The core appearance interfaces, selection, persistence, settings, sound
delegation, registry access, and default appearance live in `common`.
Their loader-neutral tests also run in `common` using the vanilla test
registration provider.
Loader-specific appearance integrations retain only their loader-dependent
registration, rendering, and optional-mod adapters.
The built-in monster appearance implementation and its loader-neutral registration also live in `common`; only its client rendering
integration remains loader-specific. Skin-pack definitions, selection, appearance behavior, and provider lookup are also shared; loaders
retain their data-pack registry lifecycle adapters.
MCA availability detection, appearance behavior, voice selection, and variation logic are shared because MCA exposes the APIs they use
independently of the loader event bus. The shared MCA initializer conditionally declares its appearance through the registration service only
when MCA is loaded. Loader modules retain client-renderer adapters.

The MultiLoader Template supplies the common-source convention, loader module
layout, service-loader pattern, and IDE run-configuration approach. Template
version numbers are reviewed rather than copied blindly. Customers continues
to read supported versions from `gradle.properties`.

Fabric was considered during the initial audit and excluded from the current
scope. This avoids a third event model, registry model, networking API,
inventory-transfer API, configuration implementation, and data-generation
entrypoint.

## Compatibility Checklist

For every loader-sensitive feature:

1. Keep behavior and state in `common` when possible.
2. Add or extend a focused common service interface.
3. Implement the service for NeoForge and Forge.
4. Register implementations through `META-INF/services`.
5. Keep loader events and annotations in thin loader adapters.
6. Add common unit tests for behavior.
7. Add loader-specific wiring tests where practical.
8. Verify both client, server, data-generation, and build tasks.
9. Record new differences and decisions in this document.
