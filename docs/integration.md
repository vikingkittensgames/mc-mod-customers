---
title: Integration
---

# Integrating with Customers

## Events

Customers publishes Architectury events when things happen to spawners, customers, and suppliers. Your mod can register listeners to add functionality when those events occur. These are server-side notifications; listeners receive event data and do not cancel the action. Register listeners during your mod initialization after adding Customers and Architectury API as dependencies.

```java
import com.vikingkittens.mc.customers.customer.CustomerEvents;

public final class MyCustomersIntegration {
    public static void initialize() {
        CustomerEvents.SHIFT_FINISHED.register(event -> {
            float score = event.percentage();
            int level = event.activeLevel();
            // React to the completed shift.
        });
    }
}
```

Customer events are fields on `CustomerEvents` in `com.vikingkittens.mc.customers.customer`; supplier events are fields on `SupplierEvents` in `com.vikingkittens.mc.customers.supplier`. Each field is an Architectury `Event<Consumer<Payload>>`: register a listener with `register`, and read the payload through its accessor methods. All customer payloads provide `level()` (`ServerLevel`), `spawnerPosition()` (`BlockPos`, possibly null), and `spawnerMode()` (`CustomerSpawnerMode`, possibly null). All supplier payloads provide `level()` and `spawnerPosition()` with the same types and nullability. `ItemStack` accessors return copies.

| Event field and payload | When it fires | Additional payload accessors |
| --- | --- | --- |
| `CustomerEvents.ITEM_SERVED` / `ItemServed` | A requested item transaction with a customer completes. | `playerId()` (nullable UUID), `customerId()` (UUID), `customerProfession()` (ResourceLocation), `servedItem()` and `costItem()` (ItemStack), `isPetItem()` (boolean). |
| `CustomerEvents.CUSTOMER_SERVED` / `CustomerServed` | A player completes service for a customer. | `playerId()` (nullable UUID), `customerId()` (UUID), `customerProfession()` (ResourceLocation), `servedItem()` and `costItem()` (ItemStack), `isPetItem()` (boolean). |
| `CustomerEvents.SHIFT_FINISHED` / `ShiftFinished` | A customer shift ends and its results are calculated. | `activeLevel()` (int), `percentage()` (float score), `totalCustomers()`, `numCustomersServed()`, `numCustomersGaveUp()`, `totalItemsWanted()`, `automatedItemsCrafted()`, `automatedItemsServed()` (ints), `playerItemsCrafted()` and `playerItemsServed()` (maps from player UUID to count). |
| `CustomerEvents.LEADERBOARD_CHANGED` / `LeaderboardChanged` | A customer leaderboard receives updated scores. | `leaderboardPosition()` (BlockPos), `activeLevel()` (int), `scores()` and `previousScores()` (maps from player UUID to float score), `changedPlayerIds()` and `affectedPlayerIds()` (sets of UUIDs), `leader()` and `previousLeader()` (nullable UUIDs), `leaderChanged()` (boolean). |
| `CustomerEvents.CUSTOMER_SPAWNER_CONFIG_CHANGED` / `CustomerSpawnerConfigChanged` | A player changes a Customer Spawner's configuration through its menu. | `playerId()` (UUID), `activeLevel()` (int), `rowSellItems()` (list of item rows), `rowCostItems()` (list of ItemStacks), `numSellItems()` and `numCostItems()` (ints), `requiredStars()` and `petPercentage()` (floats), `maxCustomers()` (int), `petTypesCustomized()` (boolean), `enabledPetTypes()` (set of IDs), `petFoods()` (map from pet ID to ItemStack), `autoCost()` (boolean), `enabledAppearances()` (list of IDs), `numAppearances()` (int). |
| `CustomerEvents.COUNTER_BLOCK_PLACED` / `CounterBlockPlaced` | A player places a matching counter block near a Customer Spawner. | `playerId()` (UUID), `counterBlockPosition()` (BlockPos), `counterBlockState()` (BlockState). |
| `SupplierEvents.SUPPLIES_PURCHASED` / `SuppliesPurchased` | A supplier trade completes. | `playerId()` (nullable UUID), `supplyItem()` and `costItem()` (ItemStack). |
| `SupplierEvents.SUPPLIER_SPAWNER_CONFIG_CHANGED` / `SupplierSpawnerConfigChanged` | A player changes a Supplier Spawner's configuration through its menu. | `playerId()` (UUID), `offers()` (list of `Offer` records with `item()` and `cost()` ItemStacks), `numSellItems()`, `numCostItems()` and `numAppearances()` (ints), `autoCost()` (boolean), `enabledAppearances()` (list of IDs). |

## Spawner Presets

Spawner presets let a mod or modpack provide ready-to-place Customer and Supplier Spawners with a name, icon, settings, and locked offer items that match its theme. Define them in data files so players can find them in the Customers creative tab. See [Customer Spawner presets]({{ '/customer-spawners.html' | relative_url }}) and [Supplier Spawner presets]({{ '/suppliers.html' | relative_url }}) for the file paths, formats, and examples.

## Appearances

Add Customer or Supplier appearances to make villagers look and sound at home in your mod or modpack. Spawners can select which appearances to use. See the [Appearances guide]({{ '/appearances/' | relative_url }}) for available appearance options and custom integration.

## Quests

You can build packages of quests around your Customers theme, such as a shop opening, a breakfast shift, or a supplier supply chain. Customers includes event-aware tasks for FTB Quests that can track those goals. See [FTB Quests Integration]({{ '/advancements/ftb-quests.html' | relative_url }}) for task setup and examples.
