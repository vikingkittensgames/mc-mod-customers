---
title: Currency Conversion
parent_title: Economy and Item Cost Suggestions
parent_url: /economy/
---

### Currency Conversion

![economy-conversions.png]({{ '/screenshots/economy-conversions.png' | relative_url }})

If you don't want your server to use emeralds for currency,
that's where currency conversions come in.  Automatic
item costs will first be calculated in emeralds, and then you
can provide a conversion from emeralds to your currency(s) of
choice.

Currency conversions use the same merging, override, and `replace` behavior as manual item costs.
Datapack files belong at:

`data/<namespace>/customers/economy/conversions/<file>.json`

Administrators can use:

`config/customers/economy/conversions.json`

or place multiple files in:

`config/customers/economy/conversions/`

The standalone file loads before files in the directory.
A later definition overrides an earlier definition with the same source item or tag and
`itemCount`.

```json
{
  "replace": false,
  "values": [
    {
      "item": "minecraft:emerald",
      "itemCount": 5,
      "costItem": "minecraft:gold_ingot",
      "costCount": 1
    },
    {
      "item": "minecraft:emerald",
      "costItem": "minecraft:gold_nugget",
      "costCount": 3
    }
  ]
}
```

Customers considers every conversion whose source item or tag matches the calculated cost. It
first prefers a conversion that produces a whole number of the target currency. When multiple
conversions produce whole numbers, the conversion with the largest `itemCount` wins. This greedy
choice favors larger currency denominations.

When no conversion produces a whole number, Customers chooses the result closest to a whole
number. If multiple results are equally close, the conversion with the largest `itemCount` wins.
The selected amount is then rounded to the nearest whole item: fractional amounts below `.5`
round down and amounts of `.5` or greater round up. A converted nonempty cost always contains at
least one item.

For the following examples, `5 emeralds -> 1 gold ingot` and
`1 emerald -> 3 gold nuggets` are available unless the row specifies otherwise.

| Calculated cost | Available conversions | Selected result | Reason |
| --- | --- | --- | --- |
| 10 emeralds | 5 -> 1 ingot; 1 -> 3 nuggets | 2 gold ingots | Both results are whole, so the larger source batch wins. |
| 6 emeralds | 5 -> 1 ingot; 1 -> 3 nuggets | 18 gold nuggets | 1.2 ingots is fractional while 18 nuggets is whole. |
| 4 emeralds | 5 -> 2 ingots; 3 -> 1 diamond | 1 diamond | 1.333 diamonds is closer to a whole item than 1.6 ingots. |
| 7 emeralds | 5 -> 1 ingot only | 1 gold ingot | 1.4 rounds down to 1. |
| 8 emeralds | 5 -> 1 ingot only | 2 gold ingots | 1.6 rounds up to 2. |

A conversion source can be any item or item tag, not only emeralds, so if there is a
datapack that is specifying automatic costs with a currency other than emeralds,
you can also define a conversion from that item to your currency(s) of choice.

