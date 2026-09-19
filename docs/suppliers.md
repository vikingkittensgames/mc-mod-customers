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

![supplier-spawner-inventory.png]({{ '/screenshots/supplier-spawner-inventory.png' | relative_url }})

![supplier-trades.png]({{ '/screenshots/supplier-trades.png' | relative_url }})

### Supplier Spawning

The Supplier will spawn each morning up to 64 blocks away from the spawner at a position
from which it can walk back to the spawner. Once the Supplier is there you can start buying items.

Suppliers only spawn where they have enough vertical clearance and a 24x24 surface made from solid
blocks, slabs, carpet, or stairs.

Once it is dark the Supplier will walk away and despawn.

