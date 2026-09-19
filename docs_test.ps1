[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$bundleCommand = Get-Command bundle -ErrorAction SilentlyContinue
if ($null -eq $bundleCommand) {
    throw "Bundler is not available. Run .\docs_setup.ps1 first."
}

$docsPath = Join-Path $PSScriptRoot "docs"
$localUrl = "http://127.0.0.1:4000/"

Push-Location $docsPath
try {
    Write-Host "Building the documentation..."
    & $bundleCommand.Source exec jekyll build
    if ($LASTEXITCODE -ne 0) {
        throw "The Jekyll build failed with exit code $LASTEXITCODE."
    }

    Write-Host ""
    Write-Host "Documentation build succeeded."
    Write-Host "Open $localUrl to view the documentation."
    Write-Host "Press Ctrl+C to stop the documentation server."
    Write-Host ""

    & $bundleCommand.Source exec jekyll serve `
        --host 127.0.0.1 `
        --port 4000
    if ($LASTEXITCODE -ne 0) {
        throw "The Jekyll server stopped with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
