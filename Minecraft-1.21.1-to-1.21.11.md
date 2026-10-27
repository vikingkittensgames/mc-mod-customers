# Minecraft 1.21.1 to 1.21.11

This file records player-facing and pack-author differences between the supported Minecraft branches. Behavior not
listed here is intended to remain the same.

## Appearance packs

- Both versions support appearance-provided names, contextual appearance properties, scrollable appearance lists,
  optional GeckoLib skins, custom sounds, sitting pivots, head tracking, and wanted-item placement above animated
  head bones.
- Minecraft 1.21.1 uses GeckoLib 4. Minecraft 1.21.11 uses GeckoLib 5.4.5 or newer within major version 5.
- Short model and animation IDs do not change. Customers resolves them to the correct version-specific asset folders.
- Self-contained GeckoLib 5 assets for Minecraft 1.21.11 belong under
  `assets/{namespace}/geckolib/models/customers/skins` and
  `assets/{namespace}/geckolib/animations/customers/skins`.
- Complete paths that reuse another mod's resources must match that mod's jar for the selected Minecraft version.
  The 1.21.11 example uses Christmas Dweller rather than Ribbits.
- The MCA Reborn appearance remains a separate add-on for Minecraft 1.21.1 and is not included in the 1.21.11 build.

## Implementation compatibility

Minecraft 1.21.11's entity rendering pipeline extracts immutable render states before submission. The GeckoLib 5
adapter therefore snapshots the selected skin, animation inputs, bone transforms, shadow size, and overhead anchor
through render-state data. Minecraft 1.21.1 keeps its GeckoLib 4 entity-driven adapter. These differences are internal
and should not change the configured animation-selection rules.
