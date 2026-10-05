# GeckoLib Integration Decisions

This log records implementation choices made while completing [appearance-gecko.md](appearance-gecko.md).

## 2026-10-04

- Gecko models are represented by a loader-neutral `SkinCustomersVillagerModel` value instead of a GeckoLib type.
  `wide` and `slim` remain constants; any other value must be a namespaced resource ID.
- Gecko-only fields are part of the existing skin definition codec so definitions can load on servers and clients
  without GeckoLib classes. Gecko definitions are filtered from selection when the `geckolib` mod is absent.
- `animation`, `sitting_pivot`, and `animations.idle`, `animations.walk`, and `animations.sit` are required for Gecko
  definitions. Supplying Gecko-only fields to a `wide` or `slim` definition is treated as a pack error.
- `head_bone` and `head_tracking` remain optional in serialized data, allowing validation to distinguish explicit
  Gecko-only properties from defaults. Runtime accessors supply `head` and the documented tracking defaults.
- Model-space points are stored as a dedicated three-float value. This preserves the compact JSON array and keeps
  coordinate conversion independent of GeckoLib versions.
- Sitting alignment converts the configured Blockbench pivot to blocks, applies the skin scale, and rotates horizontal
  correction by body yaw. The canonical Customers seated hip remains `[0, 12, 0]` model units.
- Each rendered source owns one weakly cached Gecko proxy per selected skin definition. Proxies are never added to a
  level, and their animation, pose, movement, equipment, damage, action, passenger, and visibility state is refreshed
  from the source before rendering.
- The Gecko renderer marks the configured head bone for matrix tracking and captures its current animated local-space
  transform during recursive rendering. Wanted items consume that anchor in the same nested render pass; a missing
  head bone falls back to entity height and is logged only once per model.
- Missing or invalid Gecko resources fall back to the existing wide skin renderer. The renderer retries on later
  frames so resource-pack reloads can repair a model without restarting the client, while duplicate errors are muted.
- The checked-in mixed example pack reuses Customers' built-in Alex and Steve textures. This keeps the example fully
  runnable while avoiding duplicated binary art and makes Gecko-absent filtering visible in the same appearance.
- `celebrate` maps to the existing Customer `THANKING` state. Crossbow use maps to `crossbow_charge`, while a charged
  crossbow held outside its use cycle maps to `crossbow_hold`; both remain optional.
- Generalizing the appearance dispatcher to `EntityRenderer` also exposes shadow radius through the appearance
  contract. This ensures both existing player skins and Gecko skins retain their data-defined `shadow_radius` when
  rendered through the outer Customers dispatcher.
- Minecraft 1.21.11 and 1.20.1 keep the same skin JSON contract. Their GeckoLib 5 and GeckoLib 4 adapters will be
  ported on those branches rather than adding cross-version reflection to the 1.21.1 renderer.
- Minecraft 1.21.1 pins GeckoLib 4.8.4. GeckoLib 4.9.3 was rejected because its Fabric artifact requires Loom
  1.17.21 while this branch deliberately uses Architectury Loom 1.13.469; GeckoLib also has a confirmed 1.21.1 4.9
  rendering regression. The metadata accepts compatible GeckoLib 4 updates through, but not including, GeckoLib 5.
  Loader-specific artifacts are compile-only dependencies of the shipped mod, are loaded in development, and are
  copied only into production test clients; GeckoLib is never shaded.
- Complete resource IDs ending in `.geo.json`, `.animation.json`, or `.png` bypass the Customers shorthand paths.
  This reuses the existing fields, keeps current packs unchanged, and supports resources from any installed namespace
  without adding three parallel override fields.
- `required_mods` is a generic skin-definition list rather than a Gecko-only field. Availability uses Architectury's
  loader-neutral mod check, and validation runs before model-specific validation so ordinary and Gecko skins follow
  the same dependency contract.
- Existing-mod support deliberately loads only raw model, animation, and texture assets. It does not attempt to invoke
  an upstream entity renderer or reproduce custom layers, model mutation, particles, texture selection, or controller
  variables. Those are not portable asset contracts and would turn each reuse into a code integration.
- Ribbits 4.1.6 is the documentation example because its merchant geometry and idle/walk animations are simple and
  packaged on both supported loaders. Its missing seated pose is stated explicitly instead of presenting a visually
  incomplete mapping as full compatibility.
- Skin names use Minecraft's standard JSON text-component representation rather than a Customers-specific literal or
  translation-key schema. This preserves styling and client-side translation while a compatibility codec isolates the
  older Minecraft 1.20.1 serializer API.
- Appearance names are assigned once as normal entity custom names instead of being stored in additional appearance
  properties. Vanilla synchronization and persistence then apply, while an absent or empty name list leaves the name
  untouched for mods such as Villager Names.
- The Gecko renderer records the caller's exact pose entry and restores to it after rendering. This removes every
  nested pose GeckoLib may leave behind when model loading fails, allowing the existing wide-model fallback to render
  without corrupting Minecraft's shared render state.
- Head tracking deterministically replaces the previous frame's custom rotation. If an animation changed the head
  during the current frame, its rotation is used as the base; otherwise the model's initial rotation is used. This
  retains authored head movement without accumulating look offsets across frames.
