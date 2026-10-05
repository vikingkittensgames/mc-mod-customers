# Customers Gecko Skins Example

This combined data and resource pack demonstrates one ordinary skin, one self-contained GeckoLib-animated skin, and
three optional skins that reference assets from another installed mod in the same Customers **Gecko Example**
appearance. It deliberately contains no copied binary artwork.

Copy or ZIP this directory into both the world's `datapacks` folder and the client's `resourcepacks` folder. Enable
the resource pack, install GeckoLib 4.8.4 or a compatible GeckoLib 4 release, enter the world, and select
**Gecko Example** on a Customer or Supplier Spawner. Without GeckoLib, only Alex is selected.

The animated Steve model includes the required `misc.idle`, `move.walk`, and `pose.sit` animations, a tracked `head`
bone, a `[0, 12, 0]` sitting pivot, and both literal and translated name examples. It is intentionally small and
readable as an authoring starting point.

When Ribbits 4.1.6 is installed, the appearance also includes its merchant, gardener, and sorcerer models using the
model, animation, and texture resources directly from the Ribbits jar. `required_mods` removes those definitions from
selection when Ribbits is absent. Ribbits has no sitting animation or separate head bone, so the examples map sitting
to idle, disable head tracking, and position wanted items relative to the body bone.
