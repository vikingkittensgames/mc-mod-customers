# Events, Advancements, and Statistics

Customers keeps gameplay code independent from Forge, NeoForge, advancements, and optional integrations by publishing small internal events. Advancement and statistics handlers consume those events without changing the customer or supplier features that produced them.

## Internal Events

All internal events extend `InternalEvent`. A listener is a static method annotated with `@InternalEventHandler` and taking exactly one internal-event parameter:

```java
@InternalEventHandler
public static void onItemServed(CustomerInternalEvents.ItemServed event) {
    // Respond to the completed transaction.
}
```

Register the containing class once during common initialization:

```java
InternalEvents.register(MyInternalEventHandlers.class);
```

Publish an event only after the gameplay operation succeeds:

```java
InternalEvents.emit(event);
```

`InternalEvents` calls handlers whose parameter type accepts the emitted event. A handler for `InternalEvent` therefore sees every event, a handler for `ServedEvent` sees both item- and customer-served events, and a handler for `ItemServed` sees only that event. Registration is explicit so startup behavior does not depend on classpath scanning, and registering the same class more than once has no effect.

Add customer events as public static classes in `CustomerInternalEvents` and supplier events in `SupplierInternalEvents`. Event values should describe the completed operation and defensively copy mutable values such as `ItemStack`.

### Served events

`ServedEvent` contains the values shared by its two concrete event types: the server level, spawner position and mode, serving player ID, customer ID and profession, served item stack, cost/payment item stack, and whether the served item fed that customer's active pet.

`ItemServed` represents one successfully completed requested-item transaction. It is emitted after every committed customer offer. The serving player is the first player associated with the item stacks consumed from the pickup counter. Automated or unattributed transactions still emit `ItemServed`, but do not grant a player advancement or statistic.

`CustomerServed` is emitted only the first time a particular player serves a particular customer. The customer's persisted `tradedWithPlayers` data provides this check across save and reload. When one pickup-counter transaction consumes stacks contributed by multiple players, every contributor serving that customer for the first time receives a separate `CustomerServed` event. Automated or unattributed transactions do not emit `CustomerServed` because no player/customer association can be recorded.

`CustomerServed` means that the player supplied an item to the customer; it does not mean that every request belonging to that customer has been completed. Use a separate completion event if a future feature needs that meaning.

### Counter block placement events

`CounterBlockPlaced` is emitted when a player places a block matching the
configured counter block of a loaded Customer Spawner within that spawner's
maximum counter distance. One placement can emit an event for multiple nearby
spawners. Placing the block directly above a spawner changes that spawner's
counter type and does not emit the event for that same spawner.

`CustomerSpawnerCache` keeps the loaded server-side spawners indexed by level
and position. A spawner registers when its block entity receives its level and
unregisters when the block entity is removed, including chunk unloading.
Loader-specific placement and break listeners update counter types and publish
the common event. Cache lookup uses the same spherical distance boundary as
normal counter discovery.

## Custom Advancement Triggers

Register trigger types in `CustomersTriggers`. Each implementation is a top-level `CustomersTriggerX` class in the `advancements.triggers` package so its codec, matching rules, and dispatch entry point stay together. The `customers:item_served` trigger is dispatched by `CustomersAdvancementEvents`, which is registered with the internal event system during common initialization. Minecraft's advancement system handles client synchronization, so this trigger does not require a custom network payload.

The trigger accepts these optional conditions:

| Condition | Meaning |
| --- | --- |
| `player` | Vanilla player-context predicate |
| `spawner_location` | Exact Customer Spawner dimension and block position |
| `spawner_mode` | Customer spawner mode, such as `lunch` or `continuous` |
| `customer_profession` | Customer profession ID, such as `customers:customer_impatient` |
| `served_item` | Vanilla item predicate for the requested item that was supplied |
| `served_count` | Exact count or vanilla integer range for the supplied stack |
| `cost_item` | Vanilla item predicate for the payment stack |
| `cost_count` | Exact count or vanilla integer range for the payment stack |
| `is_pet_item` | Whether the served item fed the customer's active pet |
| `total_items_served` | Exact value or vanilla integer range for the player's persistent item-transaction total |
| `total_pet_items_served` | Exact value or vanilla integer range for the player's persistent pet-item transaction total |

Conditions are combined with AND semantics. Leaving every condition out matches any attributed customer transaction:

```json
{
  "trigger": "customers:item_served"
}
```

For example, this criterion requires an impatient lunch customer who wanted at least three apples and paid exactly two emeralds:

```json
{
  "trigger": "customers:item_served",
  "conditions": {
    "spawner_mode": "lunch",
    "customer_profession": "customers:customer_impatient",
    "served_item": {
      "items": [
        "minecraft:apple"
      ]
    },
    "served_count": {
      "min": 3
    },
    "cost_item": {
      "items": [
        "minecraft:emerald"
      ]
    },
    "cost_count": 2
  }
}
```

For example, this criterion only counts transactions from one Customer Spawner:

```json
{
  "trigger": "customers:item_served",
  "conditions": {
    "spawner_location": {
      "dimension": "minecraft:overworld",
      "position": [120, 64, -350]
    }
  }
}
```

For example, this criterion matches when the player's persistent total reaches 100 completed item transactions:

```json
{
  "trigger": "customers:item_served",
  "conditions": {
    "total_items_served": {
      "min": 100
    }
  }
}
```

The `customers:counter_placed` trigger listens to `CounterBlockPlaced`. Its
optional `spawner_location`, `spawner_mode`, `counter_location`, and
`counter_block` conditions can identify both the configured Customer Spawner
and the matching block the player placed. `counter_block` is a block ID such as
`minecraft:oak_planks`.

To add another trigger:

1. Add the underlying internal event and emit it after the operation succeeds.
2. Add a `CustomersTriggerX` class in `advancements.triggers` and register it in `CustomersTriggers`.
3. Put all optional criterion fields in the trigger instance codec.
4. Add a matching method that treats omitted fields as unrestricted.
5. Add a static handler in `CustomersAdvancementEvents` that resolves the affected server player and dispatches the trigger.
6. Add focused tests for unrestricted, matching, and rejected criteria.

## Advancement Tree

Advancement definitions live in `data/customers/advancement`. The initial tree is:

```text
Customers
├── Builder
│   ├── Customer
│   └── Supplier
└── Server
    ├── Customer
    │   └── First Item Served
    │       └── 100 Items Served
    └── Supplier
```

The Builder advancements use Minecraft's `recipe_crafted` trigger for the Customer and Supplier Spawner recipes. The organizational root, Builder, Server, Customer, and Supplier nodes suppress chat announcements and toast notifications. `First Item Served` and `100 Items Served` use `customers:item_served`.

When adding an advancement, create its JSON below the appropriate branch, point `parent` at the preceding node, add its English title and description to `assets/customers/lang/en_us.json`, and validate it in the game advancement screen.

## Statistics

Register custom statistic IDs in `CustomersStatistics`, using one public static nested class per statistic. `CustomersAdvancementEvents` increments `customers:item_served` once for each attributed `ItemServed` event and increments `customers:pet_item_served` when that event supplied an item to a customer pet before dispatching the trigger. The trigger can therefore compare `total_items_served` and `total_pet_items_served` with the updated persistent statistics on the same transaction. These statistics count completed requested-item transactions, not the number of individual items in the served stack.

New statistics should be incremented by the same internal-event handler that dispatches their related trigger. This keeps gameplay code responsible only for reporting facts and keeps achievement bookkeeping in the advancements package.

## Forge and NeoForge

Most event, trigger, and statistic behavior remains in common. Both loaders use
the same Minecraft advancement trigger and custom-statistic registries through
the existing `IRegistrationHelper` compatibility service. Minecraft also
performs advancement progress synchronization on both loaders, so advancement
triggers do not require loader-specific payloads.

The internal event system deliberately does not use either loader's public
event bus. Its annotation, handler discovery, dispatch, and gameplay events
therefore behave identically on Forge and NeoForge. Block placement and break
notifications are the exception: small adapters in `CustomerForgeEvents` and
`CustomerEvents` subscribe to their loader's block events, restrict handling to
the server, and delegate to the common `CustomerSpawnerCache` implementation.

Keep future internal events, Customers advancement triggers, and statistic behavior in common whenever they use Minecraft APIs shared by both supported loaders. A class belongs in the Forge or NeoForge module only when it must subscribe to that loader's lifecycle or gameplay event bus, register through an API not covered by `IRegistrationHelper`, or integrate with a loader-specific API from another mod. In that case, put only the small adapter in the loader module and have it publish or consume a common internal event so the core behavior and tests remain shared.

## Advancement Icons

`CustomerAdvancements` owns the hidden `customers:advancement_icon` item used for custom advancement artwork. It has no recipe and is not added to a creative tab. Custom Model Data value `1` uses the emerald icon, while value `2` uses the served icon. Additional icons should add another model override and a matching model-number constant to `CustomerAdvancements`.

## Optional Integrations

### FTB Quests

FTB Quests 1.21.1 is available for NeoForge but not Forge, so the complete
`advancements.ftb` package and its optional dependency live in the NeoForge
module. `CustomersFTB` checks availability before registering task types and
the independent `CustomersFTBEvents` internal-event consumer.

Prefer FTB's Advancement Task for existing Customers advancements and its Stat
Task for arbitrary per-player `customers:item_served` or
`customers:shift_finished` totals. The single `customers:customers_task` entry
in FTB's task menu opens a Customers submenu containing `Item Served`,
`Customer Served`, `Shift Finished`, `Leaderboard Changed`, `Customer Spawner
Changed`, `Supplier Spawner Changed`, and `Counter Block Placed`. Pet-item quests
use an Item Served task with `Is Pet Item` set to true. Item and Customer Served
tasks can filter by spawner location and mode, customer profession, served item
or tag, served stack count, cost item or tag, cost stack count, and pet-item
status. Shift Finished tasks can filter by spawner location and mode, active
level, score percentage, customer and item totals, and participating player
counts.
Leaderboard Changed tasks can filter by spawner and leaderboard locations,
spawner mode, active level, a changed player's previous and new scores, whether
that player was or is the leader, and whether the overall leader changed. The
event only identifies a current or previous leader when that score list contains
more than one player.
Customer Spawner Changed tasks can filter by spawner location and mode, active
level, required stars, maximum customers, pet percentage, whether the pet-type
selection was customized, automatic-cost state, and configured sell-item,
cost-item, and appearance counts. Supplier Spawner Changed tasks expose the
spawner location, automatic-cost state, and those same three counts. Their
internal events retain the complete inventory, pet, appearance, and offer
lists, but those lists are not trigger or task conditions yet.
Counter Block Placed tasks expose the nearby spawner location and mode together
with the placed counter's location and block ID. They progress for the placing
player when the block matches a nearby Customer Spawner's configured counter.

Custom Customers tasks listen directly to Customers internal events and add one
to the serving player's FTB team progress for each matching item or customer
event. A matching shift adds progress once to every participating FTB team,
even when multiple members of that team participated. A leaderboard change
also adds at most one progress point to each affected team and evaluates
player-specific conditions against members whose score or leadership status
changed. Spawner configuration changes add progress to the team of the player
who made the change. An inventory gesture produces at most one event, and
loading, opening, or automatic spawner maintenance does not produce one. These
tasks do not read player lifetime statistics, so their
configured count belongs to that task and team. They also do not award
historical progress: an event counts only while the quest is eligible to
progress. Each task delegates event matching to its corresponding Customers
advancement trigger instance. Conditions based on a player's lifetime totals
remain advancement-specific and are not exposed in the FTB task editor.

Item predicate filters use FTB Library's native item selector plus an optional
tag text field. A non-empty tag takes precedence over the selected item.

Trigger records intended for task reuse define a `CustomersTriggerSchema` next
to the record. Each schema property supplies its serialized name, codec,
translated component, editor type, optional editor choices, and whether it is
available to tasks. The schema generates the advancement codec and is also used
by `CustomersFTBTriggerSchema` for task persistence, network synchronization,
and FTB editor fields. Add a condition to the trigger schema rather than
copying the property through each FTB task method. Property translations use
the `advancements.triggers.property.` prefix; range editors append `.min` and
`.max` to the property key.

Quest organization remains under modpack-author control. A `Customers` chapter
group can contain `Builder`, `Customer Service`, and `Supplier Service`
chapters. Quest dependencies can mirror advancement parent relationships, and
quests may combine multiple tasks or require their tasks sequentially.

### Architectury Events

The build exposes Architectury API as an optional dependency on NeoForge. Architectury API does not publish a Forge artifact for Minecraft 1.21.1, so the Forge build retains the loader-neutral availability stub but cannot publish the planned Architectury events. `CustomersArchitecturyEvents` currently provides only the guarded integration entry point; it does not publish any events yet.

Future public events should listen to Customers internal events and republish immutable event details through non-cancellable Architectury loop events. Other mods will depend on Customers and Architectury and register listeners with the public event object.

Optional integrations register their own internal-event handlers rather than
modifying customer or supplier transaction code. Vanilla advancements remain
the preferred FTB integration route when they fully represent a requirement;
custom tasks consume the same internal events only for event values or shared
team progress that advancements and statistics cannot represent.
