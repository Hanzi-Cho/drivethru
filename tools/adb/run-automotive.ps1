$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $repoRoot

Write-Host "[DriveThru] Launching com.hanzi.drivethru on the connected device..."
adb shell monkey -p com.hanzi.drivethru -c android.intent.category.LAUNCHER 1
