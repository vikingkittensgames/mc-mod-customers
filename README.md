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
* 1.20.1 - Forge & Fabric
* 1.21.1 - NeoForge & Fabric
* 1.21.11 - NeoForge & Fabric

## Documentation

Check out the full Customers documentation here:

https://vikingkittensgames.github.io/mc-mod-customers/

## Building and Testing

### NeoForge

For Minecraft 1.21.1+ use `buildNeoForge` to build the NeoForge release JAR in `neoforge/build/libs`, and
`runClientNeoForge` to test it locally.

```powershell
.\gradlew.bat buildNeoForge
.\gradlew.bat runClientNeoForge
```

### Fabric

Use `buildFabric` to build the Fabric release JAR in `fabric/build/libs`, and
`runClientFabric` to test it locally.

```powershell
.\gradlew.bat buildFabric
.\gradlew.bat runClientFabric
```

### Forge

For Minecraft 1.20.1 use `buildForge` to build the Forge release JAR in `forge/build/libs`, and
`runClientForge` to test it locally.

```powershell
.\gradlew.bat buildForge
.\gradlew.bat runClientForge
```

## Building Documentation

To build and test the documentation locally you can use the `docs_setup.ps1`
script one time to setup the environment and then run `docs_test.ps1` to
build the docs using the Jekyll template and serve them at http://localhost:4000/
