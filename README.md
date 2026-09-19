# Customers Minecraft Mod

![resturant.png](docs/screenshots/resturant.png)

## Overview

This mod provides Customer Villagers that will spawn, decide they want to buy some items from you,
and go to where they think they can buy them from you.  Once you sell the items to them they will
pay you, say thank you, and go on their merry way.  It provides the basic villager AI, spawning blocks,
and controls to crafter and customer type gameplay experiences like a fast-paced diner or a cozy
rode-side farm stand.

This has been a big passion project.  If you enjoy this mod, think about buying me a coffee to fuel
me adding more features:

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/yellow_img.png)](https://www.buymeacoffee.com/clubycoder)

<img src="buymeacoffee-qr-code.png" width="50%" height="50%" />

## Supported Minecraft Versions and Mod Loaders

For now we support Minecraft versions:
* 1.20.1 - Forge
* 1.21.1 - Forge & NeoForge
* 1.21.11 (delayed update)

## Documentation

Check out the full Customers documentation here:

https://vikingkittensgames.github.io/mc-mod-customers/

## Building and Testing

### NeoForge

Use the gradle NeoForge runDataNeoForge & buildNeoForge tools to build a release JAR which
will be in the neoforge/build/libs folder.  To test use the runClientNeoForge task.

```powershell
# Building data
.\gradlew.bat runDataNeoForge

# Building release JAR
.\gradlew.bat buildNeoForge

# Testing locally
.\gradlew.bat runClientNeoForge
```

### Forge

Use the gradle Forge runDataForge & buildForge tools to build a release JAR which
will be in the forge/build/libs folder.  To test use the runClientForge task.

```powershell
# Building data
.\gradlew.bat runDataForge

# Building release JAR
.\gradlew.bat buildForge

# Testing locally
.\gradlew.bat runClientForge
```

## Building Documentation

To build and test the documentation locally you can use the `docs_setup.ps1`
script one time to setup the environment and then run `docs_test.ps1` to
build the docs using the Jekyll template and serve them at http://localhost:4000/
