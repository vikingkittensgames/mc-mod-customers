param(
    [string] $MinecraftClientJar =
        "$env:USERPROFILE/.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem

$repository = Split-Path -Parent $PSScriptRoot
$resources = Join-Path $repository "common/src/main/resources"
$assets = Join-Path $resources "assets/customers"
$data = Join-Path $resources "data/customers"
$blockModelDirectory = Join-Path $assets "models/block"
$sourceModel = Join-Path $blockModelDirectory "display_case.json"
$templateTexture = Join-Path $assets "textures/block/display_case.png"

$variants = @(
    [pscustomobject]@{ Name = "iron"; Primary = "iron_block"; Secondary = "iron_ingot"; Side = "iron_block"; Top = "iron_block" },
    [pscustomobject]@{ Name = "copper"; Primary = "copper_block"; Secondary = "copper_ingot"; Side = "copper_block"; Top = "copper_block" },
    [pscustomobject]@{ Name = "gold"; Primary = "gold_block"; Secondary = "gold_ingot"; Side = "gold_block"; Top = "gold_block" },
    [pscustomobject]@{ Name = "oak"; Primary = "stripped_oak_log"; Secondary = "oak_planks"; Side = "stripped_oak_log"; Top = "oak_planks" },
    [pscustomobject]@{ Name = "spruce"; Primary = "stripped_spruce_log"; Secondary = "spruce_planks"; Side = "stripped_spruce_log"; Top = "spruce_planks" },
    [pscustomobject]@{ Name = "birch"; Primary = "stripped_birch_log"; Secondary = "birch_planks"; Side = "stripped_birch_log"; Top = "birch_planks" },
    [pscustomobject]@{ Name = "jungle"; Primary = "stripped_jungle_log"; Secondary = "jungle_planks"; Side = "stripped_jungle_log"; Top = "jungle_planks" },
    [pscustomobject]@{ Name = "acacia"; Primary = "stripped_acacia_log"; Secondary = "acacia_planks"; Side = "stripped_acacia_log"; Top = "acacia_planks" },
    [pscustomobject]@{ Name = "dark_oak"; Primary = "stripped_dark_oak_log"; Secondary = "dark_oak_planks"; Side = "stripped_dark_oak_log"; Top = "dark_oak_planks" },
    [pscustomobject]@{ Name = "mangrove"; Primary = "stripped_mangrove_log"; Secondary = "mangrove_planks"; Side = "stripped_mangrove_log"; Top = "mangrove_planks" },
    [pscustomobject]@{ Name = "cherry"; Primary = "stripped_cherry_log"; Secondary = "cherry_planks"; Side = "stripped_cherry_log"; Top = "cherry_planks" },
    [pscustomobject]@{ Name = "crimson"; Primary = "stripped_crimson_stem"; Secondary = "crimson_planks"; Side = "stripped_crimson_stem"; Top = "crimson_planks" },
    [pscustomobject]@{ Name = "warped"; Primary = "stripped_warped_stem"; Secondary = "warped_planks"; Side = "stripped_warped_stem"; Top = "warped_planks" },
    [pscustomobject]@{ Name = "bamboo"; Primary = "bamboo_block"; Secondary = "bamboo_planks"; Side = "bamboo_block"; Top = "bamboo_planks" },
    [pscustomobject]@{ Name = "stripped_bamboo"; Primary = "stripped_bamboo_block"; Secondary = "bamboo_planks"; Side = "stripped_bamboo_block"; Top = "bamboo_planks" }
)

function Write-Json([object] $Value, [string] $Path) {
    $directory = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $json = $Value | ConvertTo-Json -Depth 100
    [System.IO.File]::WriteAllText(
        $Path,
        $json + [Environment]::NewLine,
        (New-Object System.Text.UTF8Encoding $false)
    )
}

function New-GeometryModel([object] $Source, [string] $State) {
    $elements = @($Source.elements | Where-Object {
        $name = $_.name
        if ($name.StartsWith("glass_")) {
            return $false
        }
        if ($name -eq "bottom" -or $name -eq "top") {
            return $true
        }
        if ($State -eq "single") {
            return $name.EndsWith("_single") -or
                    $name -eq "glass_left" -or
                    $name -eq "glass_right"
        }
        if ($State -eq "left") {
            return $name.EndsWith("_left") -or
                    $name -eq "glass_left"
        }
        if ($State -eq "right") {
            return $name.EndsWith("_right") -or
                    $name -eq "glass_right"
        }
        return $name.EndsWith("_middle")
    })
    foreach ($element in $elements) {
        foreach ($face in $element.faces.psobject.Properties.Value) {
            if ($null -ne $face.texture) {
                $face.texture = "#display_case"
            }
        }
    }
    return [ordered]@{
        texture_size = @(64, 64)
        textures = [ordered]@{ particle = "#display_case" }
        elements = $elements
    }
}

function New-GlassModel([object] $Source, [string] $State) {
    $elements = @($Source.elements | Where-Object {
        $name = $_.name
        if (!$name.StartsWith("glass_")) {
            return $false
        }
        if ($State -eq "single") {
            return $name -eq "glass_front_single" -or
                    $name -eq "glass_left" -or
                    $name -eq "glass_right"
        }
        if ($State -eq "left") {
            return $name -eq "glass_front_left" -or
                    $name -eq "glass_left"
        }
        if ($State -eq "right") {
            return $name -eq "glass_front_right" -or
                    $name -eq "glass_right"
        }
        return $name -eq "glass_front_middle"
    })
    foreach ($element in $elements) {
        foreach ($face in $element.faces.psobject.Properties.Value) {
            if ($null -ne $face.texture) {
                $face.texture = "#display_case"
            }
        }
    }
    return [ordered]@{
        texture_size = @(64, 64)
        textures = [ordered]@{
            display_case = "customers:block/display_case"
            particle = "customers:block/display_case"
        }
        elements = $elements
    }
}

function Get-ModelState([bool] $ExtendLeft, [bool] $ExtendRight) {
    if ($ExtendLeft -and $ExtendRight) {
        return "middle"
    }
    if ($ExtendLeft) {
        return "right"
    }
    if ($ExtendRight) {
        return "left"
    }
    return "single"
}

function New-BlockState([string] $Name) {
    $states = [ordered]@{}
    $rotations = [ordered]@{
        north = 0
        east = 90
        south = 180
        west = 270
    }
    foreach ($extendLeft in @($false, $true)) {
        foreach ($extendRight in @($false, $true)) {
            $modelState = Get-ModelState $extendLeft $extendRight
            foreach ($facing in $rotations.Keys) {
                $key = "extend_left=$($extendLeft.ToString().ToLower()),extend_right=$($extendRight.ToString().ToLower()),facing=$facing"
                $model = [ordered]@{
                    model = "customers:block/${Name}_customers_display_case_$modelState"
                }
                if ($rotations[$facing] -ne 0) {
                    $model.y = $rotations[$facing]
                }
                $states[$key] = $model
            }
        }
    }
    return [ordered]@{ variants = $states }
}

function Get-Texture([System.IO.Compression.ZipArchive] $Archive, [string] $Name) {
    $entry = $Archive.GetEntry("assets/minecraft/textures/block/$Name.png")
    if ($null -eq $entry) {
        throw "Minecraft texture not found: $Name"
    }
    $stream = $entry.Open()
    try {
        $source = [System.Drawing.Bitmap]::FromStream($stream)
        try {
            return New-Object System.Drawing.Bitmap $source
        } finally {
            $source.Dispose()
        }
    } finally {
        $stream.Dispose()
    }
}

function New-DisplayCaseTexture(
    [System.IO.Compression.ZipArchive] $Archive,
    [object] $Variant
) {
    $template = New-Object System.Drawing.Bitmap $templateTexture
    $side = Get-Texture $Archive $Variant.Side
    $top = Get-Texture $Archive $Variant.Top
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($template)
        try {
            $graphics.CompositingMode =
                    [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
            $graphics.DrawImageUnscaled($side, 0, 0)
            $graphics.DrawImageUnscaled($top, 16, 0)
        } finally {
            $graphics.Dispose()
        }
        $path = Join-Path $assets (
            "textures/block/$($Variant.Name)_customers_display_case.png"
        )
        $template.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $template.Dispose()
        $side.Dispose()
        $top.Dispose()
    }
}

if (!(Test-Path $MinecraftClientJar)) {
    throw "Minecraft client JAR not found: $MinecraftClientJar"
}
if (!(Test-Path $sourceModel)) {
    throw "Display-case source model not found: $sourceModel"
}

$modelSource = Get-Content -Raw $sourceModel | ConvertFrom-Json
foreach ($state in @("single", "left", "middle", "right")) {
    $fileName = "display_case_$state.json"
    Write-Json (
        New-GeometryModel $modelSource $state
    ) (Join-Path $blockModelDirectory $fileName)
    $glassFileName = "display_case_glass_$state.json"
    Write-Json (
        New-GlassModel $modelSource $state
    ) (Join-Path $blockModelDirectory $glassFileName)
}

$archive = [System.IO.Compression.ZipFile]::OpenRead($MinecraftClientJar)
try {
    foreach ($variant in $variants) {
        $blockName = "$($variant.Name)_customers_display_case"
        New-DisplayCaseTexture $archive $variant

        foreach ($state in @("single", "left", "middle", "right")) {
            $parent = "customers:block/display_case_$state"
            Write-Json ([ordered]@{
                parent = $parent
                textures = [ordered]@{
                    display_case = "customers:block/$blockName"
                    particle = "customers:block/$blockName"
                }
            }) (Join-Path $blockModelDirectory "${blockName}_$state.json")
        }

        Write-Json (
            New-BlockState $variant.Name
        ) (Join-Path $assets "blockstates/$blockName.json")
        Write-Json ([ordered]@{
            parent = "customers:block/${blockName}_single"
            display = [ordered]@{
                ground = [ordered]@{
                    scale = @(0.5, 0.5, 0.5)
                    translation = @(0, 3, 0)
                }
                gui = [ordered]@{
                    rotation = @(45, 225, 0)
                    scale = @(0.6, 0.6, 0.6)
                    translation = @(0, 1, 0)
                }
            }
        }) (Join-Path $assets "models/item/$blockName.json")
        Write-Json ([ordered]@{
            type = "minecraft:crafting_shaped"
            category = "misc"
            key = [ordered]@{
                G = [ordered]@{ item = "minecraft:glass_pane" }
                T = [ordered]@{ item = "minecraft:$($variant.Secondary)" }
                V = [ordered]@{ item = "minecraft:$($variant.Primary)" }
            }
            pattern = @("VTT", "GGG")
            result = [ordered]@{
                count = 1
                id = "customers:$blockName"
            }
        }) (Join-Path $data "recipe/$blockName.json")
        Write-Json ([ordered]@{
            type = "minecraft:block"
            pools = @([ordered]@{
                bonus_rolls = 0.0
                conditions = @([ordered]@{
                    condition = "minecraft:survives_explosion"
                })
                entries = @([ordered]@{
                    type = "minecraft:item"
                    name = "customers:$blockName"
                })
                rolls = 1.0
            })
            random_sequence = "customers:blocks/$blockName"
        }) (Join-Path $data "loot_table/blocks/$blockName.json")
        Write-Json ([ordered]@{
            parent = "minecraft:recipes/root"
            criteria = [ordered]@{
                has_material = [ordered]@{
                    conditions = [ordered]@{
                        items = @([ordered]@{
                            items = "minecraft:$($variant.Primary)"
                        })
                    }
                    trigger = "minecraft:inventory_changed"
                }
                has_the_recipe = [ordered]@{
                    conditions = [ordered]@{
                        recipe = "customers:$blockName"
                    }
                    trigger = "minecraft:recipe_unlocked"
                }
            }
            requirements = @(, @("has_material", "has_the_recipe"))
            rewards = [ordered]@{
                recipes = @("customers:$blockName")
            }
        }) (Join-Path $data "advancement/recipes/decorations/$blockName.json")
    }
} finally {
    $archive.Dispose()
}
