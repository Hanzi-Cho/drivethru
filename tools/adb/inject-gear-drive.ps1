$ErrorActionPreference = "Stop"

Write-Host "[DriveThru] Injecting DRIVE gear event through VHAL..."
$result = cmd /c "adb shell cmd car_service inject-vhal-event 0x11400400 8 2>&1"
$resultText = ($result | Out-String).Trim()

if ($resultText -match "requires non-user build") {
    Write-Warning "The current emulator image blocks VHAL injection on user builds. Keep using the in-app fake gear toggle until a userdebug-capable image is available."
} elseif ($resultText) {
    Write-Host $resultText
}
