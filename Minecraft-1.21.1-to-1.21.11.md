# Minecraft 1.21.1 to 1.21.11 Migration Notes

This document records differences discovered while maintaining support for both Minecraft 1.21.1 and 1.21.11.

## Build Configuration

| Setting | Minecraft 1.21.1 | Minecraft 1.21.11 |
| --- | --- | --- |
| NeoForge | `21.1.230` | `21.11.45` |
| Parchment Minecraft version | `1.21.1` | `1.21.11` |
| Parchment mappings version | `2024.11.17` | `2025.12.20` |
| ModDevGradle | `2.0.141` | `2.0.142` |

## API Changes

| Minecraft 1.21.1 | Minecraft 1.21.11 | Notes |
| --- | --- | --- |
| `net.minecraft.Util` | `net.minecraft.util.Util` | The utility class moved into the `util` package. |
| `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` | Resource identifiers were renamed. Factory methods such as `fromNamespaceAndPath` and `withDefaultNamespace` remain available. |
| `net.minecraft.world.entity.MobSpawnType` | `net.minecraft.world.entity.EntitySpawnReason` | Entity spawn reasons were renamed. Constants such as `COMMAND` remain available. |
| `net.minecraft.world.entity.npc.Villager` | `net.minecraft.world.entity.npc.villager.Villager` | Villager classes moved into the `npc.villager` package. |
| `net.minecraft.world.entity.npc.VillagerData` | `net.minecraft.world.entity.npc.villager.VillagerData` | Villager classes moved into the `npc.villager` package. |
| `net.minecraft.world.entity.npc.VillagerProfession` | `net.minecraft.world.entity.npc.villager.VillagerProfession` | Villager classes moved into the `npc.villager` package. |
| `net.minecraft.world.entity.npc.VillagerType` | `net.minecraft.world.entity.npc.villager.VillagerType` | Villager classes moved into the `npc.villager` package. |
| `net.minecraft.client.renderer.RenderType` | `net.minecraft.client.renderer.rendertype.RenderType` | Render types moved into the `rendertype` package. |
| `net.minecraft.client.resources.PlayerSkin` | `net.minecraft.world.entity.player.PlayerSkin` | Player skin data moved from client resources to the player entity package. |
| `net.minecraft.client.model.VillagerModel` | `net.minecraft.client.model.npc.VillagerModel` | Villager models moved into the `model.npc` package. |
| `net.minecraft.client.model.WitchModel` | `net.minecraft.client.model.monster.witch.WitchModel` | Witch models moved into a monster-specific package. |
| `Level.isClientSide` | `Level.isClientSide()` | Client-side state is now exposed through an accessor method. |
| `EntityType.create(level)` | `EntityType.create(level, EntitySpawnReason)` | Entity creation now requires an explicit spawn reason. |
| `Entity.moveTo(...)` | `Entity.snapTo(...)` | Immediate entity positioning was renamed. |
| `VillagerData.getLevel()` | `VillagerData.level()` | Villager data is now a record with record-style accessors. |
| `ItemStack.onCraftedBy(level, player, count)` | `ItemStack.onCraftedBy(player, count)` | The level argument was removed. |
| `FMLEnvironment.dist` | `FMLEnvironment.getDist()` | The distribution value is now exposed through an accessor method. |
| `VillagerData` stores direct villager type and profession values | `VillagerData` stores `Holder<VillagerType>` and `Holder<VillagerProfession>` | Use `withType(registries, key)` and `withProfession(registries, key)` when assigning registry entries. |
| `ItemInteractionResult` for `Block.useItemOn` | `InteractionResult` | Item-specific block interaction results were consolidated into `InteractionResult`. |
| `InteractionResult.sidedSuccess(isClientSide)` | `isClientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER` | Side-specific interaction success is now represented by separate constants. |
| `ItemStack.hasCraftingRemainingItem()` and `getCraftingRemainingItem()` | `ItemStack.getCraftingRemainder()` | The remainder getter now returns an empty stack when no remainder exists. |
| `Player.sendSystemMessage(message)` | `Player.displayClientMessage(message, false)` | The base player type now exposes system chat delivery through `displayClientMessage`; direct system-message sending is on `ServerPlayer`. |
| `GameProfile.getName()` | `GameProfile.name()` | `GameProfile` now uses a record-style name accessor. |
| Entity persistence overrides use `CompoundTag` | Entity persistence overrides use `ValueInput` and `ValueOutput` | Read values through optional/default accessors and store complex values with codecs such as `BlockPos.CODEC`. |
| Block entity persistence overrides use `saveAdditional(CompoundTag, HolderLookup.Provider)` and `loadAdditional(CompoundTag, HolderLookup.Provider)` | Block entity persistence overrides use `saveAdditional(ValueOutput)` and `loadAdditional(ValueInput)` | Block entities use the same structured persistence API as entities. |
| `ItemStackHandler.serializeNBT(...)` and `deserializeNBT(...)` | `ItemStackHandler.serialize(ValueOutput)` and `deserialize(ValueInput)` | Store handlers in a named child value container. |
| Manual `NbtUtils` serialization for block positions, block states, and UUIDs | `ValueInput.read` and `ValueOutput.store` with `BlockPos.CODEC`, `BlockState.CODEC`, and `UUIDUtil.CODEC` | Structured persistence uses codecs while existing field names and nested list structure can be preserved. |
| `Entity.causeFallDamage(float, float, DamageSource)` | `Entity.causeFallDamage(double, float, DamageSource)` | Fall distance now uses double precision. |
| `Mob.customServerAiStep()` | `Mob.customServerAiStep(ServerLevel)` | Server AI hooks now receive the server level explicitly. |
| `GuiGraphics.blit(Identifier, ...)` | `GuiGraphics.blit(RenderPipeline, Identifier, ...)` | GUI texture draws require an explicit pipeline such as `RenderPipelines.GUI_TEXTURED`. |
| GUI transforms use the 3D pose-stack methods `pushPose`, `popPose`, `translate(x, y, z)`, and `scale(x, y, z)` | GUI transforms use the 2D matrix-stack methods `pushMatrix`, `popMatrix`, `translate(x, y)`, and `scale(x, y)` | `GuiGraphics.pose()` now returns a `Matrix3x2fStack`. |
| GUI rendering can directly call global `RenderSystem` blend/color methods and `GuiGraphics.flush()` | GUI rendering is submitted through the selected render pipeline | Remove obsolete global blend/color setup and explicit GUI flushing. |
| `RenderLevelStageEvent` exposes a `Stage` enum and `getStage()` | Render stages are represented by event subclasses such as `RenderLevelStageEvent.AfterEntities` | Subscribe directly to the required stage subtype. |
| `RenderLevelStageEvent.getCamera()` | `RenderLevelStageEvent.getLevelRenderState().cameraRenderState` | Camera position and orientation are stored in the extracted level render state. |
| `RenderType.debugFilledBox()` and `LevelRenderer.addChainedFilledBoxVertices(...)` | `DrawableGizmoPrimitives` with `addQuad(...)` | Debug-style filled geometry is submitted through the gizmo renderer. |
| Entity renderers and models receive entities directly | Entity renderers extract entity data into `EntityRenderState` subclasses consumed by models and render layers | Custom renderer state must explicitly carry any entity data needed during rendering. |
| `MobRenderer<T, M>` | `MobRenderer<T, S, M>` | The additional generic parameter is the renderer's `LivingEntityRenderState` type. |
| Villager render layers accept item renderers directly | `CustomHeadLayer` uses `PlayerSkinRenderCache`, `VillagerProfessionLayer` takes explicit adult/baby models, and `CrossedArmsItemLayer` uses its parent renderer | Layer constructors now consume render-state-oriented dependencies. |
| `LevelHeightAccessor.getMinBuildHeight()` and `getMaxBuildHeight()` | `LevelHeightAccessor.getMinY()` and `getMaxY()` | World build-height accessors were shortened. |
| `VillagerData.getProfession()` returns a `VillagerProfession` | `VillagerData.profession()` returns a `Holder<VillagerProfession>` | Compare professions by registry key with `Holder.is(...)`. |
| `EntityType.Builder.build(String)` | `EntityType.Builder.build(ResourceKey<EntityType<?>>)` | Create the entity-type key from the identifier supplied by `DeferredRegister.register`. |
| `VillagerProfession` takes a string name | `VillagerProfession` takes a `Component` name | Use a literal component to preserve an existing internal profession name. |
| `Entity.startRiding(vehicle, force)` | `Entity.startRiding(vehicle, force, sendGameEvent)` | Pass `true` for the third argument when normal mount game events should still be emitted. |
| Non-serializable vehicle entity types can be mounted | Server-side Entity.startRiding rejects vehicles whose entity type cannot serialize | Do not use EntityType.Builder.noSave() for helper entities that must act as vehicles. |
| Base `Entity` subclasses can inherit general damage handling | Base `Entity` subclasses implement `hurtServer(ServerLevel, DamageSource, float)` | Non-living helper entities must explicitly define their server damage behavior. |
| `ServerPlayer.serverLevel()` | `ServerPlayer.level()` | `ServerPlayer.level()` has a covariant `ServerLevel` return type. |
| `BlockEntityType.Builder.of(factory, blocks).build(null)` | `new BlockEntityType<>(factory, blocks)` | The builder was removed; construct block entity types directly with their valid blocks. |
| Blocks perform block-entity cleanup from `Block.onRemove(...)` | Block entities perform cleanup from `BlockEntity.preRemoveSideEffects(...)` | The block entity is removed before `affectNeighborsAfterRemoval`, so inventory and entity cleanup must run in the pre-removal hook. |
| `Block.neighborChanged(...)` receives the changed neighbor position | `Block.neighborChanged(...)` receives an optional redstone `Orientation` | Add the nullable orientation parameter to block neighbor callbacks. |
| `Level.isNight()` | `Level.isDarkOutside()` | The renamed method reports whether the dimension's exterior is currently dark. |
| `Level.isDay()` | `Level.isBrightOutside()` | The renamed method reports whether the dimension's exterior is currently bright. |
| Humanoid armor layers take separate inner/outer models and a model manager | Humanoid armor layers take adult/baby `ArmorModelSet` values and an equipment renderer | Bake the named armor model sets through `ArmorModelSet.bake(...)`. |
| Zombie-style models animate directly from the entity | `ZombieModel` animates from `ZombieRenderState.isAggressive` | Extract aggressive state in the renderer before model animation. |
| Custom stray overlay rendering copies entity-driven humanoid models | `SkeletonClothingLayer` renders from `SkeletonRenderState` and a registered outer model layer | Use the standard state-based clothing layer for stray overlays. |
| Custom drowned models animate directly from entities and use a copied overlay layer | `DrownedModel` animates from `ZombieRenderState`, and `DrownedOuterLayer` handles adult and baby overlays | Use the standard drowned model, render state, armor model sets, and outer layer. |
| Witch renderers update model and held-item state during direct entity rendering | `WitchModel` and `WitchItemLayer` consume `WitchRenderState` populated during state extraction | Extract held-item model data, entity ID, item presence, and potion state before rendering. |
| `RenderNameTagEvent` combines entity access, render decisions, pose stacks, buffers, and lighting | Name-tag handling is split between `RenderNameTagEvent.CanRender` and `RenderNameTagEvent.DoRender`, with deferred render submission | Store customer data on the entity render state during `CanRender`, then submit custom item models through `SubmitNodeCollector` during `DoRender`. |
| `SoundEvent.getLocation()` | `SoundEvent.location()` | `SoundEvent` now exposes its identifier through a record-style accessor. |
| `GoalSelector` takes a profiler supplier in its constructor | `GoalSelector` has a no-argument constructor | Construct standalone goal selectors without supplying a profiler. |
| Tests can mock `LoadingModList.get()` without an active loader | `FMLEnvironment` and `LoadingModList.get()` resolve through `FMLLoader.getCurrent()` | Install a persistent mocked FML loader before initializing Minecraft classes in regular unit tests. |
| Structured NBT tests can pass a mocked `HolderLookup.Provider` to `TagValueInput.create(...)` | Codec-backed `TagValueInput` reads require a provider that creates a valid serialization context | Use `HolderLookup.Provider.create(Stream.empty())` when a test needs no registries. |
| `neoforge:composite` model children retain their individual render types | A composite model JSON applies the composite root's single render type to all child geometry | Put a shared `render_type` on the composite root, or use a `type: "neoforge:composite"` blockstate model when children require different render types. |
| Custom data added during `RenderNameTagEvent.CanRender` remains available while rendering | NeoForge resets render-state extension data after extraction completes | Register a post-extraction modifier with `RegisterRenderStateModifiersEvent` before consuming the data during deferred rendering. |
| Entity-driven models read riding state directly from the entity | Render-state models require sitting or passenger state to be extracted explicitly | Store sitting state for villager-style models and populate `HumanoidRenderState.isPassenger` for humanoid customer models. |
