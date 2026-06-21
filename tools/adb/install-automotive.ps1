$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $repoRoot

Write-Host "[DriveThru] Installing debug APK to the connected Automotive emulator..."
cmd /c gradlew.bat :app:automotive:installDebug
