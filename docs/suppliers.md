---
title: Supplier Spawner Block
---

## Supplier Spawner Block

A Supplier Spawner Block can be used to setup a Supplier that will show up at the beginning
of the day will new supplies to buy for your restaurant or stand when you can't or don't
want to gather them your self.  Lets say your Customers want steaks, but you don't want
to harvest a bunch of cows.  That's where a Supplier can help you out.

![supplier.png]({{ '/screenshots/supplier.png' | relative_url }})

### Crafting Supplier Spawner Blocks

You can craft a supplier spawner block from a barrel surrounded by 8 emeralds.

### Specifying What the Supplier Will Sell

The supplier spawner inventory has four pairs of slots on each row. Put the stack the
Supplier will sell in the first slot of a pair and its cost in the second slot. The full
stacks are used as entered, so a stack of 32 raw steaks followed by 5 emeralds makes an
offer of 32 raw steaks for 5 emeralds. The cost can be any item, not only emeralds.

The Appearance checkboxes select which appearances Suppliers from that spawner may use.
At least one appearance is always enabled.

When a Supplier Spawner is broken, its selected appearances and automatic-cost setting are retained on the
Spawner item. Items in ordinary inventory slots are dropped normally. Preset Spawners can include locked
inventory items; locked items stay with the Spawner item and cannot be moved or removed from its interface.

### Creating Spawners With Presets

Server operators and datapacks can create preset Supplier Spawner items with vanilla item components.
This command creates a named Supplier Spawner with all supported preset settings: selected appearances,
automatic-cost behavior, and locked inventory items.

```mcfunction
/give @s customers:supplier_spawner_block[
  custom_name='{"text":"Breakfast Supplier"}',
  custom_data={
    "customers:supplier_spawner_settings": {
      icon: "preset_pack:textures/icons/supplier.png",
      enabled_appearances: [
        "customers:default"
      ],
      automatic_cost: false,
      lockedInventory: {
        Size: 54,
        Items: [
          {Slot: 0, id: "minecraft:bread", count: 16},
          {Slot: 1, id: "minecraft:emerald", count: 3}
        ]
      }
    }
  }
]
```

The formatting is for readability; remove line breaks before entering the command in chat or an `.mcfunction`
file. Replace `customers:default` with any available Supplier appearance ID. `lockedInventory` has 54 slots;
each non-empty entry is locked automatically when placed. Supplier offer rows use slots 0-7, 9-16, and so on:
even-numbered slots are offered items and the following odd-numbered slots are their costs.
The optional `icon` is a texture resource location available on every player's client through a mod or resource
pack. It appears as a cutout on top of both the placed block and its item model.

![supplier-spawner-inventory.png]({{ '/screenshots/supplier-spawner-inventory.png' | relative_url }})

![supplier-trades.png]({{ '/screenshots/supplier-trades.png' | relative_url }})

### Supplier Spawning

The Supplier will spawn each morning up to 64 blocks away from the spawner at a position
from which it can walk back to the spawner. Once the Supplier is there you can start buying items.

Suppliers only spawn where they have enough vertical clearance and a 24x24 surface made from solid
blocks, slabs, carpet, or stairs.

Once it is dark the Supplier will walk away and despawn.

