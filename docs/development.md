---
title: Build Commands
---

## Build Commands

Build commands provide information about customer and supplier spawners near the player. They are
disabled by default and can be enabled with the `enableBuildCommands` configuration option.

* `/suppliers spawners` lists supplier spawners in a 64x64 area centered on the player and
  shows whether each spawner is enabled.
* `/customers spawners` lists customer spawners in a 64x64 area centered on the player and
  shows whether each spawner is enabled and its spawning mode.
* `/customers spawners counters` also lists the matching counter blocks found for each customer
  spawner and displays a rotating mode icon above each counter for 90 seconds.

![command-spawners-counters.png]({{ '/screenshots/command-spawners-counters.png' | relative_url }})

