# Customers Gecko Skins Example for Minecraft 1.21.11

This combined data and resource pack demonstrates one ordinary skin, one self-contained GeckoLib 5 animated skin,
and one optional skin that references an animated model from Christmas Dweller. It contains no copied binary artwork.

Copy or ZIP this directory into both the world's `datapacks` folder and the client's `resourcepacks` folder. Enable
the resource pack, install GeckoLib 5.4.5 or a compatible GeckoLib 5 release, enter the world, and select
**Gecko Example** on a Customer or Supplier Spawner. Without GeckoLib, only Alex is selected.

The animated Steve model includes the required `misc.idle`, `move.walk`, and `pose.sit` animations, a tracked `head`
bone, a `[0, 12, 0]` sitting pivot, and both literal and translated name examples.

When Christmas Dweller 1.10.0 is installed, the appearance also includes its Santa Dweller model using the model,
animation, and texture resources directly from that mod's jar. `required_mods` removes the definition from selection
when Christmas Dweller is absent.
