# Minecraft Compatibility Guide

This document defines the compatibility layer used to keep customer and supplier functionality code consistent across supported Minecraft versions.

Compatibility classes have the same fully qualified names and public method signatures on every supported branch. Their implementations may use different Minecraft or NeoForge APIs for the version built by that branch.

Before introducing a version-specific call in functionality code:

1. Check this document for an existing compatibility method.
2. Use the compatibility method when it represents the same behavior.
3. Add or extend a compatibility class when a small version-specific implementation can provide a stable shared API.
4. Keep structural differences, such as changed override signatures or renderer inheritance, in version-specific integration code.
5. Update this document and the applicable migration notes when compatibility behavior changes.

## Package Organization

Common and server compatibility classes belong in:

```text
com.vikingkittens.mc.customers.compatability
```

Client-only compatibility classes belong in:

```text
com.vikingkittens.mc.customers.client.compatability
```

Classes are grouped by the Minecraft concept they adapt and use the `CUtils` suffix.

`EntityRendererCUtils` bridges the erased renderer and render-state generic
types needed by the Minecraft 1.21.11 appearance dispatcher. It invokes the
selected production renderer's normal state extraction and submission paths.

## Common and Server Compatibility

### RegistrationCUtils

`RegistrationCUtils` retains Architectury `DeferredRegister` registration while
passing the matching registry key into block and item factories. Minecraft
1.21.11 requires block and item properties to receive this key through
`setId(...)` before their constructors run.

### INetworkHelper

`INetworkHelper` provides the shared clientbound and serverbound payload transport used by block
break confirmations. Forge registers both directions on its payload channel; NeoForge registers
them with its payload registrar. Functionality code uses `sendToPlayer` or `sendToServer` without
depending on either loader's transport API.

### EntityCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.EntityCUtils
```

Entity creation:

```java
public static <T extends Entity> T create(
        EntityType<T> entityType,
        Level level
);
```

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| `entityType.create(level)` | `entityType.create(level, EntitySpawnReason.COMMAND)` |

Immediate positioning:

```java
public static void snapTo(
        Entity entity,
        Vec3 position,
        float yRotation,
        float xRotation
);

public static void snapTo(
        Entity entity,
        BlockPos position,
        float yRotation,
        float xRotation
);
```

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| `entity.moveTo(...)` | `entity.snapTo(...)` |

Vehicle mounting:

```java
public static boolean startRiding(
        Entity passenger,
        Entity vehicle,
        boolean force
);
```

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| `passenger.startRiding(vehicle, force)` | `passenger.startRiding(vehicle, force, true)` |

Vehicle entity types used with this method must remain serializable. Minecraft 1.21.11 rejects server-side mounting when the vehicle entity type was registered with `EntityType.Builder.noSave()`.

### LevelCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.LevelCUtils
```

Methods:

```java
public static boolean isClientSide(Level level);

public static boolean isDaytime(Level level);

public static boolean isNighttime(Level level);

public static int getMinBuildHeight(LevelHeightAccessor level);

public static int getMaxBuildHeight(LevelHeightAccessor level);
```

| Method | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| `isClientSide` | `level.isClientSide` | `level.isClientSide()` |
| `isDaytime` | `level.isDay()` | `level.isBrightOutside()` |
| `isNighttime` | `level.isNight()` | `level.isDarkOutside()` |
| `getMinBuildHeight` | `level.getMinBuildHeight()` | `level.getMinY()` |
| `getMaxBuildHeight` | `level.getMaxBuildHeight()` | `level.getMaxY()` |

### ItemStackCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.ItemStackCUtils
```

Methods:

```java
public static void onCraftedBy(
        ItemStack stack,
        Player player,
        int count
);

public static ItemStack getCraftingRemainder(ItemStack stack);

public static ItemCost createItemCost(
        ItemStack stack,
        int count
);
```

| Method | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| `onCraftedBy` | `stack.onCraftedBy(player.level(), player, count)` | `stack.onCraftedBy(player, count)` |
| `getCraftingRemainder` | Check `hasCraftingRemainingItem()`, then call `getCraftingRemainingItem()` | Call `stack.getCraftingRemainder()` |
| `createItemCost` | Construct with `DataComponentPredicate.allOf(stack.getComponents())` | Construct with `DataComponentExactPredicate.allOf(stack.getComponents())` |

Both implementations return an empty `ItemStack` when no crafting remainder exists.

`createItemCost` preserves component-bearing variants such as potions while
centralizing the renamed component-predicate type used by supported versions.

### PlayerCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.PlayerCUtils
```

Methods:

```java
public static void sendSystemMessage(
        Player player,
        Component message
);

public static void sendActionBarMessage(
        Player player,
        Component message
);

public static ServerLevel getServerLevel(ServerPlayer player);
```

| Method | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| `sendSystemMessage` | `player.sendSystemMessage(message)` | `player.displayClientMessage(message, false)` |
| `sendActionBarMessage` | `player.displayClientMessage(message, true)` | `player.displayClientMessage(message, true)` |
| `getServerLevel` | `player.serverLevel()` | `player.level()` |

### InteractionCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.InteractionCUtils
```

Method:

```java
public static InteractionResult sidedSuccess(boolean clientSide);
```

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| `InteractionResult.sidedSuccess(clientSide)` | `clientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER` |

### ProfileCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.ProfileCUtils
```

Method:

```java
public static String getName(GameProfile profile);
```

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| `profile.getName()` | `profile.name()` |

### VillagerCUtils

Package:

```text
com.vikingkittens.mc.customers.compatability.VillagerCUtils
```

Methods:

```java
public static VillagerData withTypeAndProfession(
        VillagerData data,
        RegistryAccess registries,
        ResourceKey<VillagerType> type,
        ResourceKey<VillagerProfession> profession
);

public static boolean hasProfession(
        VillagerData data,
        ResourceKey<VillagerProfession> profession
);
```

| Behavior | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| Assign type and profession | Resolve direct registry values and construct or update `VillagerData` | Use registry-aware `withType` and `withProfession` |
| Compare profession | Compare the direct profession value | Compare the profession holder using its registry key |

The compatibility class has version-specific imports because villager classes moved from `net.minecraft.world.entity.npc` to `net.minecraft.world.entity.npc.villager`.

## Persistence Compatibility

Persistence changed structurally between the supported versions. The Minecraft override methods remain version-specific, while shared customer and supplier persistence logic should operate through project-owned reader and writer interfaces.

Package:

```text
com.vikingkittens.mc.customers.compatability.persistence
```

Public contracts and factory:

```text
DataReader
DataWriter
PersistenceCUtils
```

Minecraft 1.21.11 uses the package-private `ValueInputDataReader` and
`ValueOutputDataWriter` adapters. Minecraft 1.21.1 uses the package-private
`CompoundTagDataReader` and `CompoundTagDataWriter` adapters.

Factory methods:

| Operation | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| Create reader | `PersistenceCUtils.reader(CompoundTag)` | `PersistenceCUtils.reader(ValueInput)` |
| Create writer | `PersistenceCUtils.writer(CompoundTag)` | `PersistenceCUtils.writer(ValueOutput)` |

Reader operations:

```java
Optional<String> getString(String key);

Optional<Float> getFloat(String key);

Optional<Integer> getInt(String key);

List<String> getStrings(String key);

boolean getBoolean(String key);

Optional<BlockPos> getBlockPos(String key);

Optional<BlockState> getBlockState(String key);

Optional<UUID> getUuid(String key);

List<UUID> getUuids(String key);

List<ItemStack> getItemStacks(String key);

DataReader childOrEmpty(String key);

List<DataReader> getChildren(String key);
```

Writer operations:

```java
void putString(String key, String value);

void putFloat(String key, float value);

void putInt(String key, int value);

void putStrings(String key, Collection<String> values);

void putBoolean(String key, boolean value);

void putBlockPos(String key, BlockPos value);

void putBlockState(String key, BlockState value);

void putUuid(String key, UUID value);

void putUuids(String key, Collection<UUID> values);

void putItemStacks(String key, List<ItemStack> values);

DataWriter child(String key);

DataWriter addChild(String key);
```

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| Adapters read and write `CompoundTag`, `ListTag`, and `NbtUtils` values | Adapters read and write `ValueInput`, `ValueOutput`, and codec-backed values |

Registry-backed factory overloads preserve complete item stacks, including
counts and data components. Minecraft 1.21.1 adapters use
`ItemStackHandler.serializeNBT` and `deserializeNBT`; Minecraft 1.21.11
adapters use `ItemStackHandler.serialize` and `deserialize`.

Entity and block-entity override signatures cannot be hidden by static methods. Each branch keeps thin version-specific overrides that create an adapter and delegate to shared persistence logic.

Completed shared persistence coverage:

- `CustomerVillagerEntity` state, positions, block states, counter target, traded-player UUIDs, and contextual appearance properties
- `SupplierVillagerEntity` state, positions, and contextual appearance properties
- `CustomerSpawnerBlockEntity` customer UUIDs and counter reservations
- `SupplierSpawnerBlockEntity` daytime transition flags

`CustomerSeatEntity` intentionally keeps empty version-specific overrides because it does not persist data.

`DataWriter.addChild(key)` appends a child to the list identified by `key`. The Minecraft 1.21.11 adapter must reuse the same `ValueOutput.ValueOutputList` handle for repeated calls with a key because requesting the list again can replace previously written entries.

Existing `ItemStackHandler` inventory serialization remains in thin
version-specific block-entity overrides. Shared item-stack lists use
`DataReader` and `DataWriter` so functionality code can remain the same across
versions.

## Data-Pack Registries

Skin definitions and skin-pack definitions use NeoForge synchronized
data-pack registries. Register them with `DataPackRegistryEvent.NewRegistry`
and provide both disk and network codecs so dedicated-server definitions are
available through client registry access.

Registry keys deliberately use `customers:skins` and
`customers:skin_packs`, producing the version-independent data paths
`data/{namespace}/customers/skins` and
`data/{namespace}/customers/skin_packs`.

## Client Compatibility

### TextureC

Package:

```text
com.vikingkittens.mc.customers.client.compatability.TextureC
```

Definition:

```java
public record TextureC(String namespace, String path) {
}
```

`TextureC` prevents `ResourceLocation` and `Identifier` from leaking into shared screen and rendering logic.

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| Convert to `ResourceLocation` | Convert to `Identifier` |

### GuiGraphicsCUtils

Package:

```text
com.vikingkittens.mc.customers.client.compatability.GuiGraphicsCUtils
```

Methods:

```java
public static void blit(
        GuiGraphics graphics,
        TextureC texture,
        int x,
        int y,
        float u,
        float v,
        int width,
        int height,
        int textureWidth,
        int textureHeight
);

public static void pushTransform(GuiGraphics graphics);

public static void popTransform(GuiGraphics graphics);

public static void translate(
        GuiGraphics graphics,
        float x,
        float y
);

public static void scale(
        GuiGraphics graphics,
        float x,
        float y
);

public static void renderItem(
        GuiGraphics graphics,
        ItemStack stack,
        int x,
        int y,
        float scale
);
```

| Behavior | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| Texture drawing | Call `GuiGraphics.blit` with a `ResourceLocation` | Call `GuiGraphics.blit` with `RenderPipelines.GUI_TEXTURED` and an `Identifier` |
| Push and pop | Use `pushPose` and `popPose` | Use `pushMatrix` and `popMatrix` |
| Translation | Use the 3D pose-stack translation with a zero Z value | Use the 2D matrix-stack translation |
| Scaling | Use the 3D pose-stack scale with a Z scale of `1.0F` | Use the 2D matrix-stack scale |
| GUI item rendering | Render the item and decorations through `GuiGraphics` after applying the compatibility transform | Render the item and decorations through the current GUI item submission API after applying the compatibility transform |

### BossBarCUtils

Package:

```text
com.vikingkittens.mc.customers.client.compatability.BossBarCUtils
```

Method:

```java
public static void render(
        GuiGraphics graphics,
        int x,
        int y,
        BossEvent bossEvent
);
```

This method renders a vanilla-style boss bar at a caller-selected position while
keeping the sprite and GUI submission differences out of shared feature code.

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| Use `ResourceLocation` boss-bar sprites and the 1.21.1 `GuiGraphics.blitSprite` overload | Use `Identifier` boss-bar sprites and the 1.21.11 GUI sprite rendering API |

### RenderingCUtils

Package:

```text
com.vikingkittens.mc.customers.client.compatability.RenderingCUtils
```

This class should adapt small rendering submissions whose inputs can be represented by stable project-owned data.

Planned operations:

```java
public static void submitItemIcon(...);

public static void applyCameraOrientation(...);

public static void renderDebugBoxes(
        RenderLevelStageEvent event,
        List<DebugBoxC> boxes
);
```

Debug geometry should be represented independently of Minecraft rendering APIs:

```java
public record DebugBoxC(
        AABB bounds,
        int color
) {
}
```

| Behavior | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| Debug boxes | Use `RenderType.debugFilledBox()` and `LevelRenderer.addChainedFilledBoxVertices(...)` | Submit quads through `DrawableGizmoPrimitives` |
| Item icons | Render directly through `ItemRenderer` and `MultiBufferSource` | Populate `ItemStackRenderState` and submit it through `SubmitNodeCollector` |
| Camera orientation | Read the event camera | Read `cameraRenderState` from the extracted level render state |

Event subscriptions remain version-specific and pass stable render descriptions into this class.

### RenderStateCUtils

Package:

```text
com.vikingkittens.mc.customers.client.compatability.RenderStateCUtils
```

This class may centralize customer render-data extraction where a stable API is possible.

| Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- |
| Models and render layers read directly from entities | Post-extraction modifiers populate reusable entity render states |

Renderer inheritance, renderer generics, model setup signatures, and event registration remain version-specific.

### Block Entity Type Construction

Minecraft 1.21.11 no longer provides the public vanilla
`BlockEntityType.Builder` used by 1.21.1. Common registrations create block
entity types through `IRegistrationHelper`: Fabric uses
`FabricBlockEntityTypeBuilder`, while NeoForge uses its access-transformed
`BlockEntityType` constructor.

## Version-Specific Integration Code

### Optional Advancement and Event APIs

FTB Quests integration is shared in common code and uses its loader-neutral
API classes. The named NeoForge distribution supplies those classes at
compile time because the intermediary shared artifact requires a newer Loom
than the project supports. Thin Fabric and NeoForge entrypoint hooks
initialize the common server and client registrations, while each loader
supplies its matching FTB Quests runtime artifact. Forge does not initialize
the integration because FTB Quests 2101.1.x has no Forge runtime artifact.

Gecko skin definitions and the public client appearance contract do not expose GeckoLib classes. The optional
renderer adapter is version-specific because GeckoLib 4 renders entity instances while GeckoLib 5 uses Minecraft's
render-state architecture. Minecraft 1.20.1 and 1.21.1 use GeckoLib 4 asset paths under `geo` and `animations`;
Minecraft 1.21.11 uses GeckoLib 5 paths under `geckolib/models` and `geckolib/animations`. Keep model selection,
animation-state decisions, pivot math, and overhead-anchor semantics identical across branches.

Short Gecko skin resource IDs are expanded by the version-specific adapter into the Customers asset layout. Complete
IDs ending in `.geo.json`, `.animation.json`, or `.png` are preserved so skins can reference another mod's installed
assets. Do not rewrite complete paths between GeckoLib generations; they describe a specific upstream jar and may
need branch-specific data if that mod reorganizes its resources. Use Architectury `Platform.isModLoaded` through the
shared skin-availability path for generic `required_mods` filtering.

Skin `names` use Minecraft's standard JSON text-component representation. Access its codec only through
`ComponentCUtils.codec()` because Minecraft 1.21.1 and 1.21.11 expose `ComponentSerialization.CODEC`, while Minecraft
1.20.1 requires a codec adapter around the older `Component.Serializer`. Appearance-provided names are assigned as
normal entity custom names so Minecraft handles synchronization, persistence, and client-side translation.

The following changes should not be hidden behind static compatibility methods because Java requires version-specific override signatures, superclass types, generics, or event subscriptions:

- `causeFallDamage(float, ...)` versus `causeFallDamage(double, ...)`
- `customServerAiStep()` versus `customServerAiStep(ServerLevel)`
- `hurtServer(ServerLevel, DamageSource, float)`
- `neighborChanged(...)`
- block removal versus `BlockEntity.preRemoveSideEffects(...)`
- entity and block-entity persistence override signatures
- `RenderLevelStageEvent.Stage` versus render-stage event subclasses
- entity-driven renderers versus render-state renderers
- `MobRenderer` generic signatures
- villager render-layer constructors
- zombie, husk, drowned, skeleton, stray, and witch renderer architecture
- combined versus split `RenderNameTagEvent`
- test bootstrap differences
- composite-model JSON behavior
- classes that only moved packages

Shared business logic should be extracted beneath these integration points whenever practical.

## Implementation Order

Compatibility support should be introduced in small, test-driven groups:

1. `EntityCUtils`
2. `LevelCUtils`
3. `ItemStackCUtils`
4. `PlayerCUtils`
5. `InteractionCUtils`
6. `ProfileCUtils`
7. `VillagerCUtils`
8. `TextureC` and `GuiGraphicsCUtils`
9. `RenderingCUtils`
10. Persistence reader and writer adapters

Each group should include focused tests, migration of applicable call sites, and matching implementations on every supported Minecraft branch.

## Minecraft 1.20.1 implementation

The 1.20.1 Fabric and Forge port keeps the shared compatibility class names
and places version-specific behavior in those classes whenever method
signatures allow it. `architectury.md` contains the full build, loader,
resource, and production-run details.

| Compatibility area | Minecraft 1.21.1 | Minecraft 1.20.1 |
| --- | --- | --- |
| Item offer cost | `ItemCost` with component predicates | `ItemStack` cost copied from the configured item |
| Item comparison | `ItemStack.isSameItemSameComponents` | `ItemStack.isSameItemSameTags` |
| Entity synchronized data | `defineSynchedData(SynchedEntityData.Builder)` | `defineSynchedData()` and `entityData.define(...)` |
| Custom registry keys | `ResourceKey.createRegistryKey(...)` | Use the same source method and let Loom `remapJar` produce each loader's production JAR |
| Block entity persistence | `CompoundTag` plus `HolderLookup.Provider` | `CompoundTag` without a provider |
| Item-stack persistence | provider-aware `ItemStack.save/parse` | `ItemStack.save` and `ItemStack.of`; provider parameters are retained only for source compatibility |
| Interaction result | `ItemInteractionResult` for `useItemOn` | `InteractionResult` for block use; unsupported items return `PASS` |
| Project interface methods that expose Minecraft state | Project-owned method names are safe | Use project-owned names such as `isVillagerInWater()` and call vanilla state methods only inside their implementation so remapping does not treat the interface contract as a Minecraft override |
| Payload networking | `CustomPacketPayload` and `StreamCodec` | Project-owned payloads serialized through `FriendlyByteBuf` and delivered by Architectury `NetworkManager` |
| Boss-bar rendering | Boss-bar sprites rendered with `GuiGraphics.blitSprite` | `textures/gui/bars.png` atlas rendered with `GuiGraphics.blit` UV offsets |
| Recipe generation | `RecipeOutput` | recipe consumer callbacks |

`ItemStackCUtils` owns item comparison, crafting, and offer-cost construction.
Persistence adapters own version-specific serialization. Networking remains
common through Architectury.

The MCA Reborn Appearance is a separate Fabric and NeoForge add-on for Minecraft 1.21.1.
It registers the existing `customers:mca` identifier so spawner settings and
spawned villagers saved by the former built-in integration remain compatible.
The Minecraft 1.20.1 Fabric and Forge port and Minecraft 1.21.11 builds do not
include the add-on or its MCA dependency.

Additional 1.20.1 details:

| Area | 1.20.1 implementation |
| --- | --- |
| Block interactions | Implement `Block#use`; newer split item-use hooks are retained only as internal helpers. |
| Block codecs | Blocks use the normal `Block(Properties)` constructor without 1.21 codec hooks. |
| Block entities | Use `load(CompoundTag)` and `saveAdditional(CompoundTag)`; update tags use `getUpdateTag()` without a provider. |
| Containers | Implement the 1.20 `Container` methods directly or through `PersistedContainer`. |
| Recipe conditions | Forge uses `ICondition#getID` and `ICondition#test(IContext)`; Fabric uses `ResourceConditions`. |
| Loot generation | Loot providers and block loot providers use constructors without registry providers. |
| GUI sprites | `GuiGraphics.blit` with explicit texture dimensions replaces 1.21 sprite blitting. |
| Player faces | `PlayerFaceRenderer` accepts a skin `ResourceLocation`; `PlayerInfo#getSkinLocation` supplies online textures. |
| Checkboxes | Construct `Checkbox` directly and override `onPress` for menu synchronization. |
| Render layers | 1.20 layer helpers use explicit RGBA values and an older argument layout. |

The 1.20.1 test helpers load `BuiltInRegistries` under the bootstrap guard
before calling `Bootstrap.bootStrap()`. This avoids circular registry
initialization ordering in headless tests. Production networking uses
Architectury and does not require the old Forge networking bootstrap shim.
