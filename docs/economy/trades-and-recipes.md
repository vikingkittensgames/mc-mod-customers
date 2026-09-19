---
title: Villager Trades and Recipe-Derived Costs
parent_title: Economy and Item Cost Suggestions
parent_url: /economy/
---

### Villager Trades and Recipe-Derived Costs

Customers scans all standard villager professions and wandering-trader offer tables when the
server starts. An emerald-to-item offer establishes what that item costs; an item-to-emerald
offer establishes what the item is worth. When several direct rates exist, the least expensive
emerald-per-item rate is used.

Crafting recipes extend those direct values in both directions. If every ingredient has a known
value, their values are added and divided across the recipe output. If an output is known but an
ingredient is not, the output value is distributed across the ingredient units. Direct trade
prices remain anchors and are not replaced by recipe-derived prices.

For example, a Farmer buying 20 wheat for one emerald directly values a 20-wheat stack at one
emerald. A Fletcher buying 32 sticks supplies another direct anchor. The plank-to-stick and
log-to-plank recipes carry that value backward, producing a derived cost for logs even though no
villager trades logs directly. Final fractional emerald values round up when an offer is built.

