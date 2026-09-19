---
title: Default Costs and Customer Cost Behavior
parent_title: Economy and Item Cost Suggestions
parent_url: /economy/
---

### Diagnosing Default Costs

The final provider returns one emerald for each item in the sell stack. Because this fallback is
intended as a last resort, Customers remembers up to 20 unique item types that reach it. Once per
server day, or one hour after a restart if a day has not elapsed, it writes a warning to the Minecraft logs such as:

```text
Customers auto cost default used (limit 20): [minecraft:apple, example:cheese]
```

Server administrators can use this list to identify items that need manual cost entries.

### Customer Cost Behavior Change

Customer Cost slots now specify the price of the full configured sell stack. If random offer
generation asks for fewer items, the cost is reduced by the same ratio, rounded down, and kept at
a minimum of one cost item. Supplier item/cost pairs continue to describe complete stacks.

Release-notes paragraph:

> Customer Spawner costs now apply to the full configured item stack instead of each individual
> item. A row containing 5 apples with a cost of 2 emeralds is a 5-for-2 offer. If a customer
> randomly requests only 3 apples, the payment scales down to 1 emerald, with every nonempty offer
> retaining a minimum cost of one. Existing Customer Spawner cost rows may need adjustment.

