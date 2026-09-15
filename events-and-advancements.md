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

`InternalEvents` calls handlers whose parameter type accepts the emitted event. A handler for `InternalEvent` therefore sees every event, while a handler for `ItemServed` sees only that event. Registration is explicit so startup behavior does not depend on classpath scanning, and registering the same class more than once has no effect.

Add customer events as public static classes in `CustomerInternalEvents` and supplier events in `SupplierInternalEvents`. Event values should describe the completed operation and defensively copy mutable values such as `ItemStack`.

### ItemServed

`ItemServed` represents one successfully completed requested-item transaction. It is emitted after the customer offer is committed. It contains the server level, spawner position and mode, serving player ID, customer ID and profession, served item stack, and cost/payment item stack.

The serving player is the first player associated with the item stacks consumed from the pickup counter. Automated or unattributed transactions still emit the internal event, but do not grant a player advancement or statistic.

Use a separate event such as `CustomerCompleted` if a future feature needs to represent satisfying every request belonging to one customer.

## Custom Advancement Triggers

Register trigger types in `CustomersTriggers`. Each trigger is a public static nested class so its codec, matching rules, and dispatch entry point stay together. The `customers:item_served` trigger is dispatched by `CustomersAdvancementEvents`, which is registered with the internal event system during common initialization. Minecraft's advancement system handles client synchronization, so this trigger does not require a custom network payload.

The trigger accepts these optional conditions:

| Condition | Meaning |
| --- | --- |
| `player` | Vanilla player-context predicate |
| `spawner_mode` | Customer spawner mode, such as `lunch` or `continuous` |
| `customer_profession` | Customer profession ID, such as `customers:customer_impatient` |
| `served_item` | Vanilla item predicate for the requested item that was supplied |
| `served_count` | Exact count or vanilla integer range for the supplied stack |
| `cost_item` | Vanilla item predicate for the payment stack |
| `cost_count` | Exact count or vanilla integer range for the payment stack |
| `total_items_served` | Exact value or vanilla integer range for the player's persistent item-transaction total |

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

To add another trigger:

1. Add the underlying internal event and emit it after the operation succeeds.
2. Add and register a nested trigger class in `CustomersTriggers`.
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

Register custom statistic IDs in `CustomersStatistics`, using one public static nested class per statistic. `CustomersAdvancementEvents` increments `customers:item_served` once for each attributed `ItemServed` event before dispatching the trigger. The trigger can therefore compare `total_items_served` with the updated persistent statistic on the same transaction. The statistic counts completed requested-item transactions, not the number of individual items in the served stack.

New statistics should be incremented by the same internal-event handler that dispatches their related trigger. This keeps gameplay code responsible only for reporting facts and keeps achievement bookkeeping in the advancements package.

## Forge and NeoForge

This initial implementation has no Forge- or NeoForge-specific event classes. Every new Java class is under the common module, and the advancement JSON, translations, and background texture are common resources. Both loaders use the same Minecraft advancement trigger and custom-statistic registries through the existing `IRegistrationHelper` compatibility service. Minecraft also performs advancement progress synchronization on both loaders, so no loader-specific payload is necessary.

The internal event system deliberately does not use either loader's public event bus. Its annotation, handler discovery, dispatch, and gameplay events therefore behave identically on Forge and NeoForge. Nothing was added under `forge/src` or `neoforge/src` for this work.

Keep future internal events, Customers advancement triggers, and statistic behavior in common whenever they use Minecraft APIs shared by both supported loaders. A class belongs in the Forge or NeoForge module only when it must subscribe to that loader's lifecycle or gameplay event bus, register through an API not covered by `IRegistrationHelper`, or integrate with a loader-specific API from another mod. In that case, put only the small adapter in the loader module and have it publish or consume a common internal event so the core behavior and tests remain shared.

## Advancement Icons

`CustomerAdvancements` owns the hidden `customers:advancement_icon` item used for custom advancement artwork. It has no recipe and is not added to a creative tab. Custom Model Data value `1` uses the emerald icon, while value `2` uses the served icon. Additional icons should add another model override and a matching model-number constant to `CustomerAdvancements`.

## Optional Integrations

### FTB Quests

The build exposes the FTB Quests 1.21.1 API as an optional compile-time dependency. FTB Quests 1.21.1 is available for NeoForge but not Forge, so its optional loader metadata and platform artifact are only included by the NeoForge module. `CustomersFTB` is the common availability guard and planning location. It does not register a custom FTB task yet.

Prefer FTB's existing Advancement Task for requirements represented by Customers advancements. Add a custom FTB task only for behavior that advancements cannot model cleanly, such as repeatable transaction totals, accumulated event values, or team-specific progress.

### Architectury Events

The build exposes Architectury API as an optional dependency on NeoForge. Architectury API does not publish a Forge artifact for Minecraft 1.21.1, so the Forge build retains the loader-neutral availability stub but cannot publish the planned Architectury events. `CustomersArchitecturyEvents` currently provides only the guarded integration entry point; it does not publish any events yet.

Future public events should listen to Customers internal events and republish immutable event details through non-cancellable Architectury loop events. Other mods will depend on Customers and Architectury and register listeners with the public event object.

An optional integration such as FTB Quests should register its own internal-event handler rather than modifying customer or supplier transaction code. Vanilla advancements can already be selected as FTB Quest tasks, so `customers:item_served` advancements provide a data-driven integration route without a direct dependency. A future direct task integration can consume the same internal event when it needs details that are not represented by a configured advancement.
