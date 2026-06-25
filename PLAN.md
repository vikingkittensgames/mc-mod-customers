# PLAN.md

Implementation plan for the Customers Minecraft mod. This plan is based on the player-facing
functionality described in `README.md`.

## Ground Rules

- Keep all production code under `com.vikingkittens.mc.customers`.
- Organize packages by feature:
  - `com.vikingkittens.mc.customers.spawner` for the `CustomerSpawnerBlock`, its block entity,
    menu/container behavior, spawning rules, item selection, redstone behavior, and counter matching.
  - `com.vikingkittens.mc.customers.customer` for the `CustomerEntity`, customer profession,
    customer job block, customer AI, trade state, departure behavior, and monster customer variants.
  - `com.vikingkittens.mc.customers.client` for client-only setup.
  - `com.vikingkittens.mc.customers.client.customer` for client-only rendering, skins, sounds,
    model selection, and visual handling for customer entities.
- Match production package layout in tests under `src/test/java`.
- Read versions from `gradle.properties`. Do not hard-code Minecraft, NeoForge, or version range
  values in implementation guidance, docs, or generated metadata.
- Write Javadocs above every new class and every new method.
- Use JUnit for unit tests and Mockito when Minecraft or NeoForge classes need to be isolated.
- Keep Minecraft and NeoForge integration thin. Put rules and calculations in small Java classes
  that JUnit can test without launching the game.
- Follow TDD: write or update tests before implementation for each behavior change.
- Do not change tests only to make implementation pass. Change tests only when they do not match
  `README.md` or an approved functionality change.
- After each phase, run the most relevant Gradle task when possible, usually `./gradlew test`.

## Phase 1: Test Infrastructure and Project Cleanup

Goal: make the project ready for TDD and remove starter-template behavior without adding player
features yet.

### Hand-Written Work

- Add JUnit and Mockito dependencies to `build.gradle`.
- Add a normal Java test source set if Gradle does not already expose one cleanly.
- Create empty package folders only when needed by actual classes or tests.
- Remove or quarantine starter-template examples from `Customers`:
  - `example_block`
  - `example_block_item`
  - `example_item`
  - `example_tab`
  - template logging that talks about dirt blocks or magic numbers
- Keep the main mod class focused on registering feature registries and common setup.
- Keep `Config` only if it has real mod settings. Otherwise remove the template config registration
  as part of cleanup.

### Codex Prompt Work

Use this prompt for generated support code and build edits:

```text
Update this NeoForge Java 21 Gradle project to support JUnit and Mockito tests. Read versions from
gradle.properties where relevant. Remove the starter example block, item, creative tab, and sample
config behavior without adding gameplay features. Keep package names under
com.vikingkittens.mc.customers. Add Javadocs to any new classes and methods. Show the diff before
finalizing.
```

### JUnit-Testable At This Point

- A smoke test can load simple pure Java utility classes.
- No gameplay rules are expected yet.
- Build configuration should allow `./gradlew test` to run.

### Minecraft-Testable At This Point

- The mod should launch in a NeoForge client or server run without template items appearing.
- The mod metadata should still load with the `customers` mod id.

## Phase 2: Core Registration Structure

Goal: create stable registration classes for feature code without implementing full gameplay.

### Hand-Written Work

- Create small registration holder classes with Javadocs:
  - `com.vikingkittens.mc.customers.spawner.CustomerSpawnerBlocks`
  - `com.vikingkittens.mc.customers.spawner.CustomerSpawnerBlockEntities`
  - `com.vikingkittens.mc.customers.customer.CustomerEntities`
  - `com.vikingkittens.mc.customers.customer.CustomerProfessions`
  - `com.vikingkittens.mc.customers.customer.CustomerJobBlocks`
- Wire these classes into `Customers`.
- Keep registration names readable and player-facing where appropriate:
  - `customer_spawner`
  - `customer_job_block`
  - `customer`
- Make the customer job block unobtainable or hidden unless there is a debug reason to expose it.
- Add a Customers creative tab only when there are real player-facing items to show.

### Codex Prompt Work

```text
Create feature-scoped NeoForge registration classes for the Customers mod. Use packages
com.vikingkittens.mc.customers.spawner and com.vikingkittens.mc.customers.customer. Register a
customer_spawner block placeholder, a matching block item, a hidden customer_job_block placeholder,
and registration holders for the future customer entity and profession. Keep implementation minimal,
testable, and documented with Javadocs. Do not implement spawning or AI yet.
```

### JUnit-Testable At This Point

- Constants and pure naming helpers can be tested without Minecraft bootstrap.
- Any registration helper methods that construct resource names can be unit tested.

### Minecraft-Testable At This Point

- The customer spawner block appears with the expected name if added to a creative tab.
- The hidden customer job block is registered but not casually available to players.
- The game launches without registry errors.

## Phase 3: Spawner Mode Rules

Goal: implement the time and redstone rules for when a spawner is allowed to spawn.

### Hand-Written Work

- Create `CustomerSpawnerMode` enum in `com.vikingkittens.mc.customers.spawner`.
- Include modes from the README:
  - Continuous
  - Every Hour
  - Day Shift
  - Night Shift
  - Breakfast Shift
  - Lunch Shift
  - Dinner Shift
  - Manual
- Create a pure Java rule class, such as `CustomerSpawnerSchedule`, that answers:
  - whether a mode is active for a given Minecraft day time
  - whether a redstone signal disables automatic spawning
  - whether a redstone pulse triggers manual spawning
  - what the next mode is when a player right-clicks with a clock
- Represent Minecraft time as ticks in tests. Keep conversion helpers small and documented.

### Codex Prompt Work

```text
Implement the pure Java spawner schedule rules for CustomerSpawnerMode based on README.md. Include
JUnit tests first for all modes, redstone disabling, manual pulse spawning, and cycling modes with a
clock. Keep the rule code independent from Minecraft Level, BlockState, and BlockEntity classes.
Add Javadocs to all new classes and methods.
```

### JUnit-Testable At This Point

- Mode cycling order.
- Continuous mode is active unless redstone disables it.
- Every Hour is active only at the top of an in-game hour.
- Day Shift is active from 5:00am to 7:00pm.
- Night Shift is active from 7:00pm to 5:00am.
- Breakfast Shift is active from 5:30am to 10:30am.
- Lunch Shift is active from 11:30am to 3:30pm.
- Dinner Shift is active from 4:30pm to 9:00pm.
- Manual mode only spawns from a redstone pulse.

### Minecraft-Testable At This Point

- Right-clicking the spawner with a clock cycles modes.
- The block visibly stores its mode.
- Player feedback appears when the mode changes.
- Redstone power prevents automatic modes from spawning.
- Manual mode responds to a pulse.

## Phase 4: Customer Spawner Block and Block Entity

Goal: implement the block, persistent block entity state, and basic player interactions.

### Hand-Written Work

- Create `CustomerSpawnerBlock` extending the appropriate Minecraft block class.
- Create `CustomerSpawnerBlockEntity` for persistent state:
  - selected `CustomerSpawnerMode`
  - current spawned customer references or UUIDs
  - spawner inventory
  - target counter/table-top matcher item or block state
  - last redstone powered state for pulse detection
- Keep block entity tick logic small. Delegate rules to tested pure Java classes.
- Add a menu/container class only when the inventory needs to open for players.
- Make the block craftable from a bed surrounded by 8 emeralds using data generation or JSON recipe.

### Codex Prompt Work

```text
Implement CustomerSpawnerBlock and CustomerSpawnerBlockEntity for the Customers NeoForge mod. Use
the already-tested pure Java spawner schedule rules instead of duplicating logic. Add persistence
for mode and inventory state. Add a clock interaction to cycle modes and a basic container opening
path for the spawner inventory. Write tests first for serializable state helpers and pulse detection.
Add Javadocs to all new classes and methods.
```

### JUnit-Testable At This Point

- Serialization helpers for mode and compact state values.
- Redstone pulse detection from previous/current powered values.
- Any inventory interpretation helpers that do not require live registries.

### Minecraft-Testable At This Point

- Spawner block places and breaks correctly.
- Mode persists after world save and reload.
- Clock interaction cycles modes and sends a message.
- The spawner opens its inventory.
- Recipe crafts the block from a bed and 8 emeralds.

## Phase 5: Purchase Request Selection

Goal: turn the spawner's 6-row inventory rules into customer purchase requests.

### Hand-Written Work

- Create pure domain classes in `com.vikingkittens.mc.customers.spawner`, such as:
  - `CustomerPurchaseOption`
  - `CustomerPurchaseRequest`
  - `CustomerPurchaseRow`
  - `CustomerPurchaseSelector`
- Model the README rules:
  - 6 rows, 9 slots per row.
  - Each row is one possible purchase slot.
  - Emeralds in a row define price per item.
  - Without emeralds, price is 1 emerald per item.
  - The stack size defines the maximum requested amount for that item.
  - Customer chooses 1 through the number of populated rows.
  - Customer chooses one non-emerald item from each selected row.
- Keep random selection injectable so tests can use deterministic random values.

### Codex Prompt Work

```text
Write JUnit tests first for CustomerPurchaseSelector using the purchase rules in README.md. Then
implement the pure Java selector and request classes in com.vikingkittens.mc.customers.spawner.
Keep Minecraft ItemStack interaction behind a small adapter so most selection logic can be tested
without a running game. Add Javadocs to every class and method.
```

### JUnit-Testable At This Point

- Empty rows are ignored.
- Emerald-only rows do not create purchase options.
- A single apple creates one request for one apple at one emerald.
- A stack of 5 apples requests 1 to 5 apples.
- Multiple items in one row choose one of the items.
- A row with emeralds prices every item in the row at that emerald stack size.
- Multiple populated rows produce between 1 and the populated row count requests.
- Deterministic random inputs produce stable expected requests.

### Minecraft-Testable At This Point

- Putting items in the spawner changes what spawned customers request.
- Emeralds in a row change the offered price.
- Empty rows do not produce requested items.

## Phase 6: Counter and Table-Top Target Matching

Goal: allow spawned customers to find valid buying locations near the spawner.

### Hand-Written Work

- Create `CustomerTargetMatcher` and target descriptor classes in
  `com.vikingkittens.mc.customers.spawner`.
- Split matching rules into small matchers:
  - carpet or wool block type and color
  - sign wood type and text
  - named containers or banners by block type and custom name
  - lecterns by named book
  - exact block match for other items
- Create a selection helper that shuffles candidate targets and sorts by nearby customer count.
- Keep the world scan itself as a thin Minecraft integration layer around tested matching logic.

### Codex Prompt Work

```text
Implement target matching for CustomerSpawnerBlock according to README.md. Start with JUnit tests
for pure target descriptor matching and target selection ordering. Then add a thin Minecraft world
scanner that finds matching blocks within 64 blocks of the spawner and chooses a random valid target
with the fewest nearby customers. Add Javadocs to all new classes and methods.
```

### JUnit-Testable At This Point

- Carpet and wool descriptors match type and color.
- Signs match wood type and text.
- Named containers and banners match block type and custom name.
- Lecterns match named book values through an adapter.
- Exact block descriptors match only exact block values.
- Candidate target sorting prefers fewer customers within 2 blocks.
- Randomized tie breaking can be made deterministic in tests.

### Minecraft-Testable At This Point

- Placing a marker item/block on top of the spawner defines the target type.
- Customers walk toward matching counters/tables within 64 blocks.
- Customers distribute across multiple matching targets instead of stacking at one spot.

## Phase 7: Customer Entity, Profession, and Trades

Goal: create the villager customer, custom profession, job block, and trade behavior.

### Hand-Written Work

- Create `CustomerEntity` in `com.vikingkittens.mc.customers.customer`.
- Register the customer entity type and customer profession.
- Register `customer_job_block` as the profession point of interest if NeoForge/Minecraft requires it.
- Keep trade state in small classes:
  - `CustomerTradeRequest`
  - `CustomerTradeListBuilder`
  - `CustomerVisitState`
- Generate villager trades from purchase requests.
- After a requested item is sold, remove that trade from the active list.
- When all trades are complete, mark the customer as ready to thank the player and leave.

### Codex Prompt Work

```text
Implement the CustomerEntity, customer profession, customer job block integration, and trade list
builder for the Customers mod. Write JUnit tests first for converting purchase requests into trades,
removing completed trades, and detecting when all trades are finished. Use Mockito only where
Minecraft trade classes cannot be constructed directly in JUnit. Keep game integration thin and add
Javadocs to all classes and methods.
```

### JUnit-Testable At This Point

- Purchase requests convert into expected emerald prices.
- Completing one trade removes only that item from the trade list.
- Completing all trades changes visit state to ready-to-leave.
- Trade state survives simple serialization if stored outside vanilla trade data.

### Minecraft-Testable At This Point

- Customer villagers spawn with the customer profession.
- Right-clicking a customer opens a villager-style trade UI.
- Selling a requested item removes that trade.
- Completing all trades triggers the goodbye path.

## Phase 8: Customer AI and Lifecycle

Goal: implement customer movement from spawn, to counter/table, back to spawner, and then away to
despawn.

### Hand-Written Work

- Add AI goal classes in `com.vikingkittens.mc.customers.customer`, such as:
  - `MoveToCustomerTargetGoal`
  - `ReturnToCustomerSpawnerGoal`
  - `LeaveCustomerSpawnerGoal`
- Keep path destination decisions in pure helper classes when possible.
- Store the source spawner position on the customer.
- On spawn:
  - pick purchase requests from the spawner
  - find a counter/table target
  - path to that target
- After trades finish:
  - send thank-you/goodbye chat
  - path back to the source spawner
  - choose a reachable block about 32 blocks away with 2 air blocks above it
  - path there and despawn

### Codex Prompt Work

```text
Add CustomerEntity lifecycle AI for moving to the selected counter/table target, returning to the
source spawner after trades complete, choosing a valid exit location about 32 blocks away with two
air blocks above it, and despawning after arrival. Write JUnit tests first for pure destination
selection and visit state transitions. Use Mockito for Minecraft path/navigation dependencies where
needed. Add Javadocs to all new classes and methods.
```

### JUnit-Testable At This Point

- Visit state transitions from spawned, to shopping, to returning, to leaving, to done.
- Exit location selection rejects blocked or unreachable candidates through a mocked adapter.
- Customer source spawner position is preserved in serialized state helpers.

### Minecraft-Testable At This Point

- Customers spawn from the spawner.
- Customers walk to a matching counter/table target.
- Customers return to the spawner after trades complete.
- Customers walk away and despawn.
- Customers do not remain forever after successful service.

## Phase 9: Shift Messages, Progress Bars, and Scores

Goal: implement player feedback for time-restricted shift modes.

### Hand-Written Work

- Create shift display helpers in `com.vikingkittens.mc.customers.spawner`.
- Keep calculations pure:
  - current shift name
  - shift start and end tick
  - progress ratio
  - remaining time
  - whether players within 64 blocks should receive updates
- Keep actual chat, boss bar, action bar, or scoreboard integration in a thin Minecraft layer.
- Align all wording with the README and player-facing mechanics.

### Codex Prompt Work

```text
Implement player feedback calculations for Customer Spawner shift modes. Write JUnit tests first
for shift progress, remaining time, and active/inactive boundaries. Then add a thin Minecraft
integration that shows nearby players within 64 blocks messages, progress bars, and scores for
time-restricted shift modes. Add Javadocs to all new classes and methods.
```

### JUnit-Testable At This Point

- Shift progress is 0 at shift start and 1 at shift end.
- Remaining time decreases as world time advances.
- Only time-restricted shift modes produce shift feedback.
- Boundary times match the README exactly.

### Minecraft-Testable At This Point

- Nearby players see shift messages.
- Nearby players see progress bars and scores.
- Players outside 64 blocks do not receive spawner feedback.

## Phase 10: Special Night Shift Monster Customers

Goal: add friendly monster-looking customer variants when a night shift spawner has a lit
jack-o-lantern next to it.

### Hand-Written Work

- Keep server behavior identical to normal customers unless a real gameplay rule says otherwise.
- Store a `CustomerAppearance` or similar variant value on the entity.
- In `com.vikingkittens.mc.customers.client.customer`, implement client-only rendering/model/sound
  selection for:
  - zombie
  - skeleton
  - witch
  - pillager
  - vindicator
  - evoker
  - illusioner
- Make the variants friendly and trade-capable. Do not use hostile mob AI.

### Codex Prompt Work

```text
Implement special night shift customer appearance variants. Server-side customers should keep normal
friendly customer behavior, but when a Customer Spawner is in Night Shift and has a lit
jack-o-lantern next to it, spawned customers may receive a monster appearance variant. Add JUnit
tests first for the pure variant selection rules. Put client-only renderer and sound selection under
com.vikingkittens.mc.customers.client.customer. Add Javadocs to all classes and methods.
```

### JUnit-Testable At This Point

- Monster variants are eligible only in Night Shift.
- A lit jack-o-lantern adjacent to the spawner enables monster variants.
- Non-night modes never select monster variants.
- Variant selection can be deterministic with injected random input.

### Minecraft-Testable At This Point

- Night Shift spawners next to lit jack-o-lanterns sometimes spawn monster-looking customers.
- Monster-looking customers are friendly and trade like normal customers.
- Monster-looking customers use the expected client-side model, skin, and sound.

## Phase 11: Data, Assets, and Player-Facing Polish

Goal: complete the player-facing experience.

### Hand-Written Work

- Add or update:
  - block models
  - blockstates
  - item models
  - textures for each spawner mode
  - language entries
  - recipe JSON or data generation
  - loot tables
  - tags if needed
- Keep language strings player-facing and consistent with `README.md`.
- Update `README.md` only for approved player-facing behavior changes.

### Codex Prompt Work

```text
Add the data and asset wiring for the Customers mod's customer spawner block, mode-specific block
appearance, recipe, loot table, language entries, and customer profession display text. Keep wording
player-facing and aligned with README.md. Do not add implementation details to README.md. Show the
diff for review.
```

### JUnit-Testable At This Point

- Resource path helper tests, if helper methods exist.
- No broad asset behavior is expected to be covered by JUnit.

### Minecraft-Testable At This Point

- Blocks and items have proper names.
- The customer spawner has the expected recipe.
- Breaking the spawner drops the expected item.
- Mode changes visibly alter the spawner texture.
- Missing texture or missing translation warnings are resolved.

## Phase 12: Integration Pass and Balancing

Goal: verify the complete loop in Minecraft and adjust only player-facing values that need tuning.

### Hand-Written Work

- Create a manual test checklist world:
  - one continuous spawner
  - one redstone-controlled spawner
  - one manual spawner
  - one day shift spawner
  - one night shift spawner with a lit jack-o-lantern
  - multiple counter/table marker types
- Confirm customer cap behavior: each spawner tries to keep up to 4 active customers.
- Confirm multiple spawners allow more customers.
- Confirm a completed customer leaves and despawns.
- Update `README.md` if approved mechanics changed during implementation.

### Codex Prompt Work

```text
Review the Customers mod for gaps against README.md. Focus on customer spawner modes, redstone
behavior, purchase selection, target matching, customer trades, departure behavior, and night shift
monster appearances. Identify missing tests first, then propose minimal code changes. Do not change
README.md unless the player-facing mechanics were intentionally changed.
```

### JUnit-Testable At This Point

- Full rule coverage for pure domain behavior.
- Regression tests for bugs found during manual Minecraft testing.

### Minecraft-Testable At This Point

- Full gameplay loop:
  - craft spawner
  - configure purchases
  - configure target marker
  - spawn customers
  - customers path to target
  - players trade requested items
  - customers thank the player
  - customers return, leave, and despawn
- All spawning modes behave as documented.
- Redstone disabling and manual pulses behave as documented.
- Night shift monster customers behave as documented.

## Suggested 50/50 Work Split

The junior developer should hand-write the code that teaches the project shape and core domain:

- test setup and first tests
- package structure
- enum and pure rule classes
- purchase request domain model
- target descriptor model
- visit state model
- README-aligned language and manual test checklist

Use Codex for repetitive or integration-heavy work after tests define the behavior:

- Gradle dependency wiring
- NeoForge registration boilerplate
- serialization boilerplate
- menu/container wiring
- data and asset JSON
- Mockito-heavy integration tests
- renderer and client-only wiring
- review passes against `README.md`

For each phase, the junior developer should:

1. Read the relevant `README.md` section.
2. Write the JUnit tests for the pure behavior.
3. Hand-write the simplest implementation for the core rule.
4. Ask Codex to generate the NeoForge wiring or repetitive support code using the phase prompt.
5. Run `./gradlew test`.
6. Launch Minecraft for the phase's manual checks when the feature reaches game integration.
7. Review the diff before committing or moving to the next phase.
