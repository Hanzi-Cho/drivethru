package com.hanzi.drivethru.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanzi.drivethru.core.model.CustomUiDestination
import com.hanzi.drivethru.core.model.CustomUiViewState
import com.hanzi.drivethru.core.model.GearState
import com.hanzi.drivethru.core.model.MenuItem
import com.hanzi.drivethru.core.model.StopStateReason
import com.hanzi.drivethru.data.menu.FakeMenuRepository
import com.hanzi.drivethru.di.AppContainer
import com.hanzi.drivethru.feature.customui.CustomUiFlowCoordinator

class DriveThruActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = AppContainer()
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF101418)) {
                    DriveThruCustomUiApp(
                        coordinator = container.customUiFlowCoordinator,
                        menuItems = FakeMenuRepository().getAllMenuItems(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveThruCustomUiApp(
    coordinator: CustomUiFlowCoordinator,
    menuItems: List<MenuItem>,
) {
    var viewState by remember { mutableStateOf(coordinator.getViewState()) }

    fun refreshState() {
        viewState = coordinator.getViewState()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            HeaderSection(viewState = viewState)
            Spacer(modifier = Modifier.height(16.dp))
            DebugControlPanel(
                onEnterDemoStore = {
                    coordinator.enterDemoStore()
                    refreshState()
                },
                onSetPark = {
                    coordinator.setGearState(GearState.PARK)
                    refreshState()
                },
                onSetDrive = {
                    coordinator.setGearState(GearState.DRIVE)
                    refreshState()
                },
                onSetStopped = {
                    coordinator.setVehicleSpeed(0.0)
                    refreshState()
                },
                onSetMoving = {
                    coordinator.setVehicleSpeed(3.5)
                    refreshState()
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            CurrentScreen(
                viewState = viewState,
                menuItems = menuItems,
                onOpenFullMenu = {
                    coordinator.openFullMenu()
                    refreshState()
                },
                onAddMenuItem = { itemId ->
                    coordinator.addMenuItem(itemId)
                    refreshState()
                },
                onOpenCartReview = {
                    coordinator.openCartReview()
                    refreshState()
                },
                onResumeOrdering = {
                    coordinator.resumeOrdering()
                    refreshState()
                },
                onCloseSession = {
                    coordinator.closeSession()
                    refreshState()
                },
            )
        }
    }
}

@Composable
private fun HeaderSection(viewState: CustomUiViewState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2128)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "DriveThru Custom UI Preview",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = viewState.statusMessage,
                color = Color(0xFFD5DBE1),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("Gear ${viewState.vehicleSignal.gearState.name}")
                StatusChip("Speed %.1f m/s".format(viewState.vehicleSignal.speedMetersPerSecond))
                StatusChip(viewState.destination.name.replace('_', ' '))
            }
        }
    }
}

@Composable
private fun StatusChip(label: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF2B343D), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = Color(0xFFFFD166), fontSize = 12.sp)
    }
}

@Composable
private fun DebugControlPanel(
    onEnterDemoStore: () -> Unit,
    onSetPark: () -> Unit,
    onSetDrive: () -> Unit,
    onSetStopped: () -> Unit,
    onSetMoving: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171E24)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Debug input", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onEnterDemoStore) { Text("Enter demo store") }
                OutlinedButton(onClick = onSetPark) { Text("Set PARK") }
                OutlinedButton(onClick = onSetDrive) { Text("Set DRIVE") }
                OutlinedButton(onClick = onSetStopped) { Text("Speed 0.0") }
                OutlinedButton(onClick = onSetMoving) { Text("Speed 3.5") }
            }
        }
    }
}

@Composable
private fun CurrentScreen(
    viewState: CustomUiViewState,
    menuItems: List<MenuItem>,
    onOpenFullMenu: () -> Unit,
    onAddMenuItem: (String) -> Unit,
    onOpenCartReview: () -> Unit,
    onResumeOrdering: () -> Unit,
    onCloseSession: () -> Unit,
) {
    when (viewState.destination) {
        CustomUiDestination.STANDBY -> {
            InfoPanel(
                title = "Standby",
                body = "Waiting for a fake entry trigger. Use the debug panel to simulate store entry.",
            )
        }

        CustomUiDestination.STORE_READY -> {
            InfoPanel(
                title = "Store detected",
                body = "Active store: ${viewState.activeStore?.name ?: "Unknown"}. Full ordering opens automatically or manually once the vehicle is in PARK.",
                primaryAction = "Open full menu",
                onPrimaryAction = onOpenFullMenu,
            )
        }

        CustomUiDestination.FULL_MENU -> {
            MenuPanel(
                storeName = viewState.activeStore?.name ?: "Unknown store",
                menuItems = menuItems,
                draftItemCount = viewState.orderDraft?.items?.sumOf { it.quantity } ?: 0,
                onAddMenuItem = onAddMenuItem,
                onOpenCartReview = onOpenCartReview,
            )
        }

        CustomUiDestination.CART_REVIEW -> {
            CartReviewPanel(
                viewState = viewState,
                onResumeOrdering = onResumeOrdering,
                onCloseSession = onCloseSession,
            )
        }

        CustomUiDestination.STOP_STATE -> {
            StopStatePanel(
                stopStateReason = viewState.stopStateReason,
                draftItemCount = viewState.orderDraft?.items?.sumOf { it.quantity } ?: 0,
                onResumeOrdering = onResumeOrdering,
                onCloseSession = onCloseSession,
            )
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2128)),
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
private fun MenuPanel(
    storeName: String,
    menuItems: List<MenuItem>,
    draftItemCount: Int,
    onAddMenuItem: (String) -> Unit,
    onOpenCartReview: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2128)),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(storeName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Safe full ordering state", color = Color(0xFFD5DBE1))
                }
                Button(onClick = onOpenCartReview) {
                    Text("Review cart ($draftItemCount)")
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
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("KRW ${item.price}", color = Color(0xFFFFD166))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(onClick = { onAddMenuItem(item.id) }) {
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
    onResumeOrdering: () -> Unit,
    onCloseSession: () -> Unit,
) {
    val draft = viewState.orderDraft
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2128)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Cart review", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            draft?.items?.forEach { item ->
                Text(
                    "${item.menuItem.name} x${item.quantity} · KRW ${item.menuItem.price * item.quantity}",
                    color = Color(0xFFD5DBE1),
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Total KRW ${draft?.totalPrice ?: 0}", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onResumeOrdering) { Text("Back to ordering") }
                OutlinedButton(onClick = onCloseSession) { Text("Close session") }
            }
        }
    }
}

@Composable
private fun StopStatePanel(
    stopStateReason: StopStateReason?,
    draftItemCount: Int,
    onResumeOrdering: () -> Unit,
    onCloseSession: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1717)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("STOP_STATE", color = Color(0xFFFFD0D0), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Reason: ${stopStateReason?.name ?: "UNKNOWN"}",
                color = Color(0xFFFFE2A8),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The draft is preserved with $draftItemCount item(s). Return to PARK, then choose to continue.",
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onResumeOrdering) { Text("Continue ordering") }
                OutlinedButton(onClick = onCloseSession) { Text("Close session") }
            }
        }
    }
}
