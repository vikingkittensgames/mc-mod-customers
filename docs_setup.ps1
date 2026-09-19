[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Update-ProcessPath {
    $machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $env:Path = "$machinePath;$userPath"
}

function Confirm-NativeCommand {
    param(
        [Parameter(Mandatory)]
        [string] $Description
    )

    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

$docsPath = Join-Path $PSScriptRoot "docs"

$rubyCommand = Get-Command ruby -ErrorAction SilentlyContinue
if ($null -eq $rubyCommand) {
    $wingetCommand = Get-Command winget -ErrorAction SilentlyContinue
    if ($null -eq $wingetCommand) {
        throw "Ruby is not installed and winget is unavailable. Install Ruby 3.1 with DevKit, then run this script again."
    }

    Write-Host "Ruby was not found. Installing Ruby 3.1 with DevKit..."
    & $wingetCommand.Source install `
        --exact `
        --id RubyInstallerTeam.RubyWithDevKit.3.1 `
        --accept-package-agreements `
        --accept-source-agreements
    Confirm-NativeCommand "Ruby installation"
    Update-ProcessPath

    $rubyCommand = Get-Command ruby -ErrorAction SilentlyContinue
    if ($null -eq $rubyCommand) {
        throw "Ruby was installed but is not available in this terminal. Open a new PowerShell terminal and run this script again."
    }
}

Write-Host "Using $(& $rubyCommand.Source --version)"

$gemCommand = Get-Command gem -ErrorAction SilentlyContinue
if ($null -eq $gemCommand) {
    throw "RubyGems was not found alongside Ruby."
}

$bundleCommand = Get-Command bundle -ErrorAction SilentlyContinue
if ($null -eq $bundleCommand) {
    Write-Host "Installing Bundler..."
    & $gemCommand.Source install bundler
    Confirm-NativeCommand "Bundler installation"
    Update-ProcessPath
    $bundleCommand = Get-Command bundle -ErrorAction Stop
}

Push-Location $docsPath
try {
    Write-Host "Configuring documentation dependencies under docs/vendor/bundle..."
    & $bundleCommand.Source config set --local path vendor/bundle
    Confirm-NativeCommand "Bundler configuration"

    Write-Host "Installing documentation dependencies..."
    & $bundleCommand.Source install
    Confirm-NativeCommand "Documentation dependency installation"
}
finally {
    Pop-Location
}

Write-Host ""
Write-Host "Documentation setup is complete."
Write-Host "Run .\docs_test.ps1 to build and serve the documentation."
