---
title: Economy and Item Cost Suggestions
---

## Economy and Item Cost Suggestions

The Customer and Supplier Spawner config gives you a lot of easy control and customization
to fit your shop builds, but is some situations like a server where players can build their
own shops you would want to limit the configuration to prevent exploiting the sell of items.
You may also want to integrate the cost of items with your existing server currency system
like custom coins that have other purposes vs vanilla emeralds.  This is where the economy
configuration comes in.

![economy-config.png]({{ '/screenshots/economy-config.png' | relative_url }})

|                    Spawner Manual                     |                 Spawner Automatic                 |                     Server Forced                     |
|:-----------------------------------------------------:|:-------------------------------------------------:|:-----------------------------------------------------:|
| ![economy-manual.png]({{ '/screenshots/economy-manual.png' | relative_url }}) | ![economy-auto.png]({{ '/screenshots/economy-auto.png' | relative_url }}) | ![economy-forced.png]({{ '/screenshots/economy-forced.png' | relative_url }}) |

The economy system can suggest costs for Customer and Supplier Spawner offers. Automatic costs are
enabled globally by default, but each spawner starts in manual-cost mode. The small cost toggle
at the right edge of the player inventory switches the selected Customer Spawner level or the
Supplier Spawner between manual and automatic costs. The green dollar icon means the configured
Cost slots are used; the crossed-out icon means costs are generated automatically.

When automatic costs are active, the interface uses the automatic-cost background and displays
generated cost items where the Cost slots normally appear. These displayed items are previews,
not stored inventory. Editing a sell stack updates its preview through the server. Any items
already stored in disabled Cost slots are dropped on top of the spawner.

The server `forceAutoCost` config makes every Customer and Supplier Spawner use automatic costs and hides the
per-spawner cost toggle.  This is for the case where you are the server admin and you want to
prevent exploits and/or force the Customers cost integration with your existing server currency.

Turning off `enableEconomy` disables all cost suggestions, including
forced and per-spawner automatic costs, without changing the saved per-spawner choices.

The item cost generation is made up of multiple providers to give flexibility for how costs are
managed, tweaked, and try to align with the existing server vanilla/server economy.
Cost providers are tried in this order:

1. Manual item-cost definitions from datapacks and server configuration
2. Village Shop System, when installed and enabled
3. ProjectE, when installed and enabled
4. Vanilla villager trades, wandering-trader trades, and crafting recipes
5. One emerald per item is the final fallback


### Economy topics

* [Manual Item Costs]({{ '/economy/manual-costs.html' | relative_url }})
* [Village Shop System and ProjectE]({{ '/economy/mod-integrations.html' | relative_url }})
* [Villager Trades and Recipe-Derived Costs]({{ '/economy/trades-and-recipes.html' | relative_url }})
* [Currency Conversion]({{ '/economy/currency-conversion.html' | relative_url }})
* [Default Costs and Customer Cost Behavior]({{ '/economy/default-costs.html' | relative_url }})
