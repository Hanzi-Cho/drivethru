$ErrorActionPreference = "Stop"

param(
    [string]$Source,
    [string]$Stage,
    [string]$LanePoint,
    [double]$Latitude,
    [double]$Longitude,
    [string]$BeaconId,
    [double]$SpeedMps,
    [string]$Gear,
    [Nullable[bool]]$Parking,
    [switch]$ResetSession
)

$args = @("shell", "am", "broadcast", "-a", "com.hanzi.drivethru.action.INJECT_DEBUG_EVENT")

if ($PSBoundParameters.ContainsKey("Source")) {
    $args += @("--es", "source", $Source)
}
if ($PSBoundParameters.ContainsKey("Stage")) {
    $args += @("--es", "stage", $Stage)
}
if ($PSBoundParameters.ContainsKey("LanePoint")) {
    $args += @("--es", "lane_point", $LanePoint)
}
if ($PSBoundParameters.ContainsKey("Latitude")) {
    $args += @("--ef", "latitude", [string]$Latitude)
}
if ($PSBoundParameters.ContainsKey("Longitude")) {
    $args += @("--ef", "longitude", [string]$Longitude)
}
if ($PSBoundParameters.ContainsKey("BeaconId")) {
    $args += @("--es", "beacon_id", $BeaconId)
}
if ($PSBoundParameters.ContainsKey("SpeedMps")) {
    $args += @("--ef", "speed_mps", [string]$SpeedMps)
}
if ($PSBoundParameters.ContainsKey("Gear")) {
    $args += @("--es", "gear", $Gear)
}
if ($PSBoundParameters.ContainsKey("Parking")) {
    $args += @("--ez", "parking", ($(if ($Parking) { "true" } else { "false" })))
}
if ($ResetSession.IsPresent) {
    $args += @("--ez", "reset_session", "true")
}

& adb @args
