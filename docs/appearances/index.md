---
title: Appearances
---

## Appearances

The appearance of Customers and Suppliers is extensible. An appearance controls how
those villagers are rendered and which ambient, hurt, death, step, successful-trade,
and invalid-trade sounds they use.
Each spawner can enable the appearances its villagers may use.  Open the spawner UI
to enable or disable differences.

Appearances can customize these sounds:

* **Ambient** - Played periodically while the Customer or Supplier is nearby.
* **Hurt** - Played when the Customer or Supplier takes damage.
* **Death** - Played when the Customer or Supplier dies.
* **Step** - Played while the Customer or Supplier walks.
* **Yes** - Played after a successful trade and when the items in a trade interface
  satisfy an available trade.
* **No** - Played when the items in a trade interface do not satisfy an available
  trade.

Each sound override is optional. When an appearance does not provide a sound,
the normal Villager sound for that action is used.

### Default Appearance

The Default appearance uses vanilla Villager rendering and sounds with custom textures
that distinguish Suppliers, normal Customers, impatient Customers, and casual Customers.
The Default appearance is enabled initially.

### Monsters Appearance

The Monsters appearance makes Customers look and sound like friendly monsters while
preserving their normal customer behavior, including sitting while waiting. It uses the
customer's saved appearance variation to consistently select a zombie, skeleton, witch,
husk, drowned, or stray. Enable Monsters in a Customer Spawner's appearance list to make
it available in any spawning mode.

![night-shift-special.png]({{ '/screenshots/night-shift-special.png' | relative_url }})


### Appearance topics

* [Skins]({{ '/appearances/skins.html' | relative_url }})
* [Minecraft Comes Alive]({{ '/appearances/minecraft-comes-alive.html' | relative_url }})
* [Custom Appearance Mod Integration]({{ '/appearances/custom.html' | relative_url }})
