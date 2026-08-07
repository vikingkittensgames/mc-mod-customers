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

## Common and Server Compatibility

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

- `CustomerVillagerEntity` state, positions, block states, counter target, and traded-player UUIDs
- `SupplierVillagerEntity` state and positions
- `CustomerSpawnerBlockEntity` customer UUIDs and counter reservations
- `SupplierSpawnerBlockEntity` daytime transition flags

`CustomerSeatEntity` intentionally keeps empty version-specific overrides because it does not persist data.

`DataWriter.addChild(key)` appends a child to the list identified by `key`. The Minecraft 1.21.11 adapter must reuse the same `ValueOutput.ValueOutputList` handle for repeated calls with a key because requesting the list again can replace previously written entries.

Existing `ItemStackHandler` inventory serialization remains in thin
version-specific block-entity overrides. Shared item-stack lists use
`DataReader` and `DataWriter` so functionality code can remain the same across
versions.

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

## Version-Specific Integration Code

The following changes should not be hidden behind static compatibility methods because Java requires version-specific override signatures, superclass types, generics, or event subscriptions:

- `causeFallDamage(float, ...)` versus `causeFallDamage(double, ...)`
- `customServerAiStep()` versus `customServerAiStep(ServerLevel)`
- `hurtServer(ServerLevel, DamageSource, float)`
- `neighborChanged(...)`
- block removal versus `BlockEntity.preRemoveSideEffects(...)`
- entity and block-entity persistence override signatures
- `RenderLevelStageEvent.Stage` versus render-stage event subclasses
- entity-driven renderers versus render-state renderers
- direct block-entity renderers versus extracted block-entity render states
- client/server data-generation event and provider architecture
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
