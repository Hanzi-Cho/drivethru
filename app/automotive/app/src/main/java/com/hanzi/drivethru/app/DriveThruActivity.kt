package com.hanzi.drivethru.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanzi.drivethru.core.model.CarSignalReading
import com.hanzi.drivethru.core.model.CustomUiDestination
import com.hanzi.drivethru.core.model.CustomUiViewState
import com.hanzi.drivethru.core.model.DriveThruLanePoint
import com.hanzi.drivethru.core.model.DriveThruUiMode
import com.hanzi.drivethru.core.model.DriveThruZoneStage
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.StopStateReason
import com.hanzi.drivethru.di.DriveThruRuntime
import com.hanzi.drivethru.feature.customui.CustomUiFlowCoordinator

class DriveThruActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = DriveThruRuntime.get(applicationContext)
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            container.startRuntime()
            container.customUiFlowCoordinator.restartEntryProviders()
        }
        if (!hasRuntimeSensorPermissions()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                ),
            )
        } else {
            container.startRuntime()
        }
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF0E1318)) {
                    DriveThruCustomUiApp(
                        coordinator = container.customUiFlowCoordinator,
                        onLaunchTemplateApp = { launchTemplateApp() },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        DriveThruRuntime.get(applicationContext).stopRuntime()
        super.onDestroy()
    }

    private fun launchTemplateApp() {
        runCatching {
            startActivity(
                Intent().setClassName(
                    packageName,
                    "androidx.car.app.activity.CarAppActivity",
                ),
            )
        }
    }

    private fun hasRuntimeSensorPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
private fun DriveThruCustomUiApp(
    coordinator: CustomUiFlowCoordinator,
    onLaunchTemplateApp: () -> Unit,
) {
    val viewState by coordinator.viewStateFlow.collectAsState()
    var uiMode by remember { mutableStateOf(DriveThruUiMode.ENHANCED_CUSTOM) }

    val menuItems = viewState.menuItems
    val diagnostics = coordinator.getCarSignalReadings()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1318))
            .padding(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(viewState = viewState, uiMode = uiMode)
            Spacer(modifier = Modifier.height(14.dp))
            ModeSelector(
                currentMode = uiMode,
                onSelectMode = { uiMode = it },
                onLaunchTemplateApp = onLaunchTemplateApp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            DebugControlPanel(
                onGpsApproaching = {
                    coordinator.simulateGpsTrigger(DriveThruZoneStage.APPROACHING, DriveThruLanePoint.ENTRANCE)
                },
                onGpsReady = {
                    coordinator.simulateGpsTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)
                },
                onBeaconReady = {
                    coordinator.simulateBeaconTrigger(DriveThruZoneStage.ORDERING_READY, DriveThruLanePoint.MENU_BOARD)
                },
                onExitZone = {
                    coordinator.resetEntryTrigger()
                },
                onSetPark = {
                    coordinator.setGearState(GearState.PARK)
                    coordinator.setVehicleSpeed(0.0)
                },
                onSetDrive = {
                    coordinator.setGearState(GearState.DRIVE)
                },
                onSetStopped = {
                    coordinator.setVehicleSpeed(0.0)
                },
                onSetMoving = {
                    coordinator.setVehicleSpeed(3.5)
                },
            )
            Spacer(modifier = Modifier.height(14.dp))
            DiagnosticsPanel(viewState = viewState, diagnostics = diagnostics)
            Spacer(modifier = Modifier.height(14.dp))
            when (uiMode) {
                DriveThruUiMode.CAR_TEMPLATE -> TemplateReferencePanel(onLaunchTemplateApp = onLaunchTemplateApp)
                DriveThruUiMode.CLASSIC_CUSTOM -> ClassicScreen(
                    viewState = viewState,
                    menuItems = menuItems,
                    coordinator = coordinator,
                )
                DriveThruUiMode.ENHANCED_CUSTOM -> EnhancedScreen(
                    viewState = viewState,
                    menuItems = menuItems,
                    coordinator = coordinator,
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(viewState: CustomUiViewState, uiMode: DriveThruUiMode) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161D24)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "DriveThru IVI Studio",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = viewState.statusMessage,
                color = Color(0xFFD5DBE1),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("Mode ${uiMode.name}")
                StatusChip("Gear ${viewState.vehicleSignal.gearState.name}")
                StatusChip("Speed %.1f".format(viewState.vehicleSignal.speedMetersPerSecond))
                StatusChip(viewState.entryTriggerEvent?.stage?.name ?: "NO_TRIGGER")
            }
        }
    }
}

@Composable
private fun ModeSelector(
    currentMode: DriveThruUiMode,
    onSelectMode: (DriveThruUiMode) -> Unit,
    onLaunchTemplateApp: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A20)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("UI mode", color = Color.White, fontWeight = FontWeight.SemiBold)
            DriveThruUiMode.entries.forEach { mode ->
                val selected = currentMode == mode
                val onClick = {
                    onSelectMode(mode)
                    if (mode == DriveThruUiMode.CAR_TEMPLATE) {
                        onLaunchTemplateApp()
                    }
                }
                if (selected) {
                    Button(onClick = onClick) { Text(mode.name) }
                } else {
                    OutlinedButton(onClick = onClick) { Text(mode.name) }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF25303A), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = Color(0xFFFFD166), fontSize = 12.sp)
    }
}

@Composable
private fun DebugControlPanel(
    onGpsApproaching: () -> Unit,
    onGpsReady: () -> Unit,
    onBeaconReady: () -> Unit,
    onExitZone: () -> Unit,
    onSetPark: () -> Unit,
    onSetDrive: () -> Unit,
    onSetStopped: () -> Unit,
    onSetMoving: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A20)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Simulation controls", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onGpsApproaching) { Text("GPS approach") }
                OutlinedButton(onClick = onGpsReady) { Text("GPS ready") }
                OutlinedButton(onClick = onBeaconReady) { Text("Beacon ready") }
                OutlinedButton(onClick = onExitZone) { Text("Exit zone") }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onSetPark) { Text("PARK + stop") }
                OutlinedButton(onClick = onSetDrive) { Text("DRIVE") }
                OutlinedButton(onClick = onSetStopped) { Text("Speed 0.0") }
                OutlinedButton(onClick = onSetMoving) { Text("Speed 3.5") }
            }
        }
    }
}

@Composable
private fun DiagnosticsPanel(
    viewState: CustomUiViewState,
    diagnostics: List<CarSignalReading>,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10171D)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Runtime diagnostics", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Firebase: ${viewState.firebaseStatus}",
                color = Color(0xFFD5DBE1),
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            diagnostics.forEach { reading ->
                Text(
                    "${reading.type.name} = ${reading.rawValue} (${reading.status.name}, ${reading.source.name})",
                    color = if (reading.status.name == "OK") Color(0xFFB7F5C5) else Color(0xFFFFD9A8),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun TemplateReferencePanel(onLaunchTemplateApp: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171E24)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Car App Template mode", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "This mode keeps the current Car App Library reference experience available for study and portfolio comparison.",
                color = Color(0xFFD5DBE1),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLaunchTemplateApp) {
                Text("Launch template app host")
            }
        }
    }
}

@Composable
private fun ClassicScreen(
    viewState: CustomUiViewState,
    menuItems: List<MenuItem>,
    coordinator: CustomUiFlowCoordinator,
) {
    when (viewState.destination) {
        CustomUiDestination.STANDBY -> {
            InfoPanel("Standby", "Waiting for GPS geofence, beacon, or adb inject input.")
        }

        CustomUiDestination.STORE_READY -> {
            InfoPanel(
                "Store detected",
                "Store: ${viewState.activeStore?.name ?: "Unknown"}. Shift to PARK to unlock full ordering.",
                primaryAction = "Open full menu",
            ) {
                coordinator.openFullMenu()
            }
        }

        CustomUiDestination.FULL_MENU -> {
            ClassicMenuPanel(viewState, menuItems, coordinator)
        }

        CustomUiDestination.CART_REVIEW -> {
            CartReviewPanel(viewState, coordinator)
        }

        CustomUiDestination.STOP_STATE -> {
            StopStatePanel(viewState.stopStateReason, viewState.orderDraft?.items?.sumOf { it.quantity } ?: 0, coordinator)
        }
    }
}

@Composable
private fun EnhancedScreen(
    viewState: CustomUiViewState,
    menuItems: List<MenuItem>,
    coordinator: CustomUiFlowCoordinator,
) {
    if (viewState.destination == CustomUiDestination.STANDBY || viewState.destination == CustomUiDestination.STORE_READY) {
        ClassicScreen(viewState, menuItems, coordinator)
        return
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(BorderStroke(1.dp, Color(0xFF25303A)), MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF161E25), Color(0xFF0F141A)),
                    ),
                )
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EnhancedCategoryRail()
            EnhancedMenuPanel(
                viewState = viewState,
                menuItems = menuItems,
                coordinator = coordinator,
                modifier = Modifier.weight(1.6f),
            )
            EnhancedSummaryPanel(
                viewState = viewState,
                coordinator = coordinator,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EnhancedCategoryRail() {
    Card(
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222A)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Browse", color = Color.White, fontWeight = FontWeight.Bold)
            listOf("Popular", "Burgers", "Drinks", "Sides", "Desserts").forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index == 0) Color(0xFFFFD166) else Color(0xFF24303A),
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        item,
                        color = if (index == 0) Color(0xFF11161A) else Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedMenuPanel(
    viewState: CustomUiViewState,
    menuItems: List<MenuItem>,
    coordinator: CustomUiFlowCoordinator,
    modifier: Modifier = Modifier,
) {
    if (viewState.destination == CustomUiDestination.STOP_STATE) {
        StopStatePanel(viewState.stopStateReason, viewState.orderDraft?.items?.sumOf { it.quantity } ?: 0, coordinator)
        return
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C22)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(viewState.activeStore?.name ?: "DriveThru Store", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Enhanced portfolio ordering UI", color = Color(0xFFB8C6D1))
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(menuItems) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2932))) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.description, color = Color(0xFFB7C3CE), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("KRW ${item.price}", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = {
                                coordinator.addMenuItem(item.id)
                            }) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedSummaryPanel(
    viewState: CustomUiViewState,
    coordinator: CustomUiFlowCoordinator,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222A)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Order summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Trigger source: ${viewState.entryTriggerEvent?.source?.name ?: "NONE"}", color = Color(0xFFB7C3CE))
            Text("Zone stage: ${viewState.entryTriggerEvent?.stage?.name ?: "OUTSIDE"}", color = Color(0xFFB7C3CE))
            Spacer(modifier = Modifier.height(4.dp))
            viewState.orderDraft?.items?.forEach { item ->
                Text("${item.menuItem.name} x${item.quantity}", color = Color.White)
            }
            Text(
                "Total KRW ${viewState.orderDraft?.totalPrice ?: 0}",
                color = Color(0xFFFFD166),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    coordinator.openCartReview()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Review current draft")
            }
            OutlinedButton(
                onClick = {
                    coordinator.resumeOrdering()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Resume / keep ordering")
            }
            OutlinedButton(
                onClick = {
                    coordinator.closeSession()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close session")
            }
        }
    }
}

@Composable
private fun InfoPanel(
    title: String,
    body: String,
    primaryAction: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171E24)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(body, color = Color(0xFFD5DBE1), fontSize = 15.sp)
            if (primaryAction != null && onPrimaryAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onPrimaryAction) { Text(primaryAction) }
            }
        }
    }
}

@Composable
private fun ClassicMenuPanel(
    viewState: CustomUiViewState,
    menuItems: List<MenuItem>,
    coordinator: CustomUiFlowCoordinator,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171E24)), modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(viewState.activeStore?.name ?: "DriveThru Store", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Classic custom UI", color = Color(0xFFD5DBE1))
                }
                Button(onClick = {
                    coordinator.openCartReview()
                }) {
                    Text("Review cart (${viewState.orderDraft?.items?.sumOf { it.quantity } ?: 0})")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(menuItems) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF232D36))) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.description, color = Color(0xFFB6C2CD), fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(onClick = {
                                coordinator.addMenuItem(item.id)
                            }) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartReviewPanel(
    viewState: CustomUiViewState,
    coordinator: CustomUiFlowCoordinator,
) {
    val draft = viewState.orderDraft
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171E24)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Cart review", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            draft?.items?.forEach { item ->
                Text("${item.menuItem.name} x${item.quantity} · KRW ${item.menuItem.price * item.quantity}", color = Color(0xFFD5DBE1))
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Total KRW ${draft?.totalPrice ?: 0}", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    coordinator.resumeOrdering()
                }) { Text("Back to ordering") }
                OutlinedButton(onClick = {
                    coordinator.closeSession()
                }) { Text("Close session") }
            }
        }
    }
}

@Composable
private fun StopStatePanel(
    stopStateReason: StopStateReason?,
    draftItemCount: Int,
    coordinator: CustomUiFlowCoordinator,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1717)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("STOP_STATE", color = Color(0xFFFFD0D0), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Reason: ${stopStateReason?.name ?: "UNKNOWN"}", color = Color(0xFFFFE2A8))
            Spacer(modifier = Modifier.height(8.dp))
            Text("The draft is preserved with $draftItemCount item(s). Return to PARK to continue, or accelerate/exit to close the session.", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    coordinator.resumeOrdering()
                }) { Text("Continue ordering") }
                OutlinedButton(onClick = {
                    coordinator.closeSession()
                }) { Text("Close session") }
            }
        }
    }
}
