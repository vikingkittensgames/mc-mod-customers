---
title: Manual Item Costs
parent_title: Economy and Item Cost Suggestions
parent_url: /economy/
---

### Manual Item Costs

Economy definitions can come from both datapacks and the server configuration directory. Datapacks
let modpack authors and server owners distribute an economy with a world or pack. Configuration
files provide a separate administrator-controlled layer, allowing an individual server to add or
override definitions without editing its installed datapacks. Both sources use the same JSON
format and are refreshed by `/reload`.

Datapack item-cost files belong at:

`data/<namespace>/customers/economy/items/<file>.json`

Server administrator files belong in:

`config/customers/economy/items/`

Splitting definitions into files such as `foods.json`,
`building_blocks.json`, and `modded_items.json` makes large economies easier to maintain without
changing their behavior.

Customers loads datapacks from lowest to highest pack priority, sorting files by resource ID
within each pack. It then loads configuration files alphabetically. Values normally merge, and a
later matching definition overrides an earlier definition. This lets a server configuration entry
override a distributed datapack value.

The optional root `replace` property defaults to `false`. When `replace` is `true`, Customers
discards every value accumulated from earlier files before adding that file's `values`. A
higher-priority datapack can therefore replace an economy supplied by a lower-priority pack, and a
server administrator can ignore all distributed definitions and maintain a strict local list.
Files processed after the replacing file can still add or override values normally. This explicit
reset is useful because otherwise an administrator would have to override every unwanted inherited
entry individually.

Each entry has exactly one vanilla-style `item` or `tag` matcher. `itemCount` describes the
priced batch and `costCount` describes its price; both default to 1. Calculated partial batches
round up. Cost items are ordinary item IDs and may be any item.

```json
{
  "replace": false,
  "values": [
    {
      "item": "minecraft:apple",
      "itemCount": 5,
      "costItem": "minecraft:emerald",
      "costCount": 2
    },
    {
      "tag": "minecraft:logs",
      "itemCount": 4,
      "costItem": "minecraft:gold_nugget",
      "costCount": 3
    }
  ]
}
```

Items that do not match continue to the next provider.

