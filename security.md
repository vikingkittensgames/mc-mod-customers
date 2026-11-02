# Security Review

## Scope

This review covers player-controlled and persisted item transfers involving
customer spawners, supplier spawners, pickup counters, payment boxes, customer
trades, counter automation, block removal, unwanted-item returns, and block
break confirmation networking.

The review focuses on item duplication or loss, malformed-save crashes,
automation transaction failures, unauthorized remote actions, and incomplete
save behavior. Static resource files and the `logs/` directory are outside the
scope of this review.

## Findings and Fixes

### Fixed inventory sizes trusted serialized data

`PersistedContainer` previously replaced its configured size with the `Size`
stored in NBT. A malformed value could allocate excessive memory or shrink a
counter or spawner inventory below slots that production logic assumes exist.
Current customer-spawner inventory reads could also allocate the serialized
size before limiting the number of copied slots.

Fixed containers now retain their configured size. Persistence readers also
provide a bounded item-stack read, which customer spawner inventories and pet
food use before allocation.

Regression coverage:

- `PersistedInventorySecurityTest`

### Fabric transactions could overbook pickup-counter demand

Fabric pickup-counter storage previously tracked reservations independently
for each outer transaction. Two simultaneously open transactions could both
simulate against the same remaining customer demand. The later final commit
could fail with `IllegalStateException`, allowing automation to crash a server
tick.

Only one pickup-counter target may hold reservations at a time. Repeated
inserts are permitted only for the same target in the same outer transaction.
This prevents different connected counters from reserving the same customer
demand. Aborting or closing the transaction releases the reservation.

Regression coverage:

- `CustomerPickupCounterFabricSecurityGameTests`

### Break confirmations could be accepted remotely

A confirmation token was bound to the player, block position, block entity,
and expiration time, but confirmation did not recheck interaction distance.
A player could request confirmation, move away, and confirm the original
break remotely while its chunk remained loaded.

Confirmation now requires the player to remain within six blocks of the
block center. Existing token ownership, expiration, block-entity identity,
and normal block-breaking permission checks remain in effect.

Regression coverage:

- `BlockBreakConfirmationSecurityTest`

### Supplier saves could silently omit inventory

Supplier spawner saving caught every inventory serialization failure and
continued writing the remaining block-entity data. If serialization failed,
Minecraft could persist a valid supplier spawner tag with its inventory
missing, permanently deleting configured items on the next load.

Inventory serialization failures now propagate to Minecraft's save handling
instead of producing an incomplete successful block-entity save.

## Item-Conservation Results

The following reviewed paths preserve item counts under normal server-tick
execution:

- A pickup-counter offer is removed only after the connected counter network
  contains the full requested count.
- Offers split across connected counters remove exactly the requested count.
- An incomplete offer leaves every counter unchanged.
- Revalidation retains currently wanted items and returns exactly the excess.
- Payment-box insertion checks total capacity before changing any slot, so a
  failed full-payment insertion cannot leave a partial duplicate or partial
  payment.
- Counter block removal drops copies of all stored stacks once, immediately
  before the block entity is removed.
- Manual and automated counter insertion use returned remainders to represent
  the only items not transferred.

Regression coverage:

- `CustomerItemConservationSecurityTest`
- `CustomerPaymentBoxBlockEntityTest`
- `CustomerPickupCounterDroppedItemTest`
- `CustomerPickupCounterInsertionTargetTest`

## Save and Crash Boundaries

Item movement, inventory mutation, entity creation, and dirty-state updates
run on the server thread. No reviewed path intentionally keeps two durable
owners for the same item stack.

A process crash can still roll the world back to Minecraft's last completed
save. Cross-object operations such as moving an item from a player or item
entity into a block entity are governed by Minecraft's chunk and entity save
boundaries; the mod cannot provide a database-style transaction across an
unexpected process termination. The changes in this review prevent malformed
inventory sizes and incomplete successful supplier saves from turning those
normal rollback boundaries into persistent invalid state.

## Remaining Review Areas

- Run the security tests on every supported Minecraft branch because
  persistence adapters differ between 1.20.1, 1.21.1, and 1.21.11.
- Exercise forced process termination during active hopper, pipe, and Fabric
  transaction activity in a disposable world to validate loader-level save
  behavior that regular JUnit tests cannot reproduce.
- Repeat multiplayer testing with claim or protection mods because their
  block-break event ordering is outside this mod's control.
