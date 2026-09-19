---
title: Village Shop System and ProjectE
parent_title: Economy and Item Cost Suggestions
parent_url: /economy/
---

### Village Shop System and ProjectE

If you are already managing a server with other mods that manage an economy, we want to reduce
the duplicate work for you and try to base costs on what you already have.  Right now we support
integrating with the
[Villager Shop System](https://www.curseforge.com/minecraft/mc-mods/village-shop-system) mod and the
[ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte) mod.

![mod-logo-villager-shop-system.png]({{ '/screenshots/mod-logo-villager-shop-system.png' | relative_url }})
When [Villager Shop System](https://www.curseforge.com/minecraft/mc-mods/village-shop-system) is installed and enabled,
we want to try and keep costs in sync with villager shops.
Customers uses its sell-price calculator and the
server's Village Shop System custom prices. Bulk villager ratios are rounded up to at least one
emerald when an individual stack would otherwise truncate to zero.

![mod-logo-projecte.png]({{ '/screenshots/mod-logo-projecte.png' | relative_url }})
When [ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte) is installed and enabled,
We want to be in sync with the ProjectE item equivelancy system that you may
have made adjustments to.
Customers compares the stack's EMC value with ordinary wheat. The
standard Farmer trade of 20 wheat for one emerald converts that wheat-equivalent value into an
emerald cost. Items with no EMC value continue to the next provider.

Neither integration is a required dependency, and each can be disabled independently.

![economy-mods-config.png]({{ '/screenshots/economy-mods-config.png' | relative_url }})

