package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthRepository
import com.example.data.DetectionRepository
import com.example.model.AnimalSpecies
import com.example.model.Detection
import com.example.model.ThreatLevel
import com.example.model.User
import com.example.model.UserType
import com.example.ui.components.RadarView
import com.example.ui.components.WildlifeCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CrimsonCritical
import com.example.ui.theme.CyanRadar
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ForestCard
import com.example.ui.theme.ForestCardBorder
import com.example.ui.theme.ForestObsidian
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { DetectionRepository.getInstance() }
    val detections by repository.detections.collectAsState()
    val isFirestoreConnected by repository.isFirestoreConnected.collectAsState()
    val lastAlert by repository.lastAlert.collectAsState()

    var selectedFilter by remember { mutableStateOf<ThreatLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDetailDetection by remember { mutableStateOf<Detection?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showRadar by remember { mutableStateOf(true) }

    // Haptic vibration on new critical/high alert
    LaunchedEffect(lastAlert) {
        lastAlert?.let { alert ->
            if (alert.threatLevel == ThreatLevel.CRITICAL || alert.threatLevel == ThreatLevel.HIGH) {
                try {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(400)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Filter detections
    val filteredDetections = detections.filter { detection ->
        val matchesThreat = selectedFilter == null || detection.threatLevel == selectedFilter
        val matchesSearch = searchQuery.isBlank() ||
                detection.speciesName.contains(searchQuery, ignoreCase = true) ||
                detection.direction.contains(searchQuery, ignoreCase = true) ||
                detection.locationName.contains(searchQuery, ignoreCase = true)
        matchesThreat && matchesSearch
    }

    val criticalCount = detections.count { it.threatLevel == ThreatLevel.CRITICAL }
    val nearestDistance = detections.minOfOrNull { it.distanceMeters } ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ForestObsidian,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("fab_add_detection")
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (user.userType == UserType.SYSTEM_OPERATOR) "Trigger detection" else "Report sighting",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Header with Status
            DashboardHeader(
                user = user,
                isFirestoreConnected = isFirestoreConnected,
                onLocationClick = { showLocationPicker = true },
                onSignOut = onSignOut,
                onToggleRole = {
                    val newRole = if (user.userType == UserType.SYSTEM_OPERATOR) {
                        UserType.COMMUNITY_USER
                    } else {
                        UserType.SYSTEM_OPERATOR
                    }
                    AuthRepository.getInstance().switchUserType(newRole)
                }
            )

            // Critical Alert Banner (if any recent high/critical alert)
            AnimatedVisibility(
                visible = lastAlert != null && (lastAlert?.threatLevel == ThreatLevel.CRITICAL || lastAlert?.threatLevel == ThreatLevel.HIGH),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                lastAlert?.let { alert ->
                    EmergencyAlertBanner(
                        alert = alert,
                        onView = { selectedDetailDetection = alert },
                        onDismiss = { repository.clearLastAlert() }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Key Metrics Bar
                item {
                    ThreatSummaryRow(
                        totalCount = detections.size,
                        criticalCount = criticalCount,
                        nearestDistance = nearestDistance,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // 1-Tap Fast Scenarios for Demonstrations
                item {
                    HackathonQuickTriggerRow(
                        onTrigger = { species, threat, dist, dir, angle ->
                            repository.triggerPresetScenario(species, threat, dist, dir, angle)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Radar View Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Perimeter radar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = if (showRadar) "Hide radar" else "Show radar",
                                color = EmeraldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { showRadar = !showRadar }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (showRadar) {
                            RadarView(
                                detections = detections,
                                onDetectionClick = { selectedDetailDetection = it }
                            )
                        }
                    }
                }

                // Feed Section Header & Filters
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stream,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active detections",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${filteredDetections.size} total",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Threat Level Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedFilter == null,
                                    onClick = { selectedFilter = null },
                                    shape = RoundedCornerShape(4.dp),
                                    label = { Text("All (${detections.size})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ForestCardBorder,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = ForestSurface,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedFilter == null,
                                        borderColor = ForestCardBorder,
                                        selectedBorderColor = TextSecondary
                                    )
                                )
                            }
                            items(ThreatLevel.values()) { level ->
                                val count = detections.count { it.threatLevel == level }
                                val isSelected = selectedFilter == level
                                val lvlColor = Color(level.hexColor)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = if (isSelected) null else level },
                                    shape = RoundedCornerShape(4.dp),
                                    label = { Text("${level.title} ($count)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = lvlColor.copy(alpha = 0.2f),
                                        selectedLabelColor = lvlColor,
                                        containerColor = ForestSurface,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = ForestCardBorder,
                                        selectedBorderColor = lvlColor
                                    )
                                )
                            }
                        }
                    }
                }

                // Detections List
                if (filteredDetections.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No detections matching current filter",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDetections, key = { it.id }) { detection ->
                        WildlifeCard(
                            detection = detection,
                            onClick = { selectedDetailDetection = detection },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modals
    if (showAddDialog) {
        AddDetectionDialog(
            onDismiss = { showAddDialog = false },
            onAddDetection = { detection ->
                repository.addDetection(detection)
            }
        )
    }

    selectedDetailDetection?.let { detection ->
        DetectionDetailDialog(
            detection = detection,
            userType = user.userType,
            onDismiss = { selectedDetailDetection = null },
            onAcknowledge = { id -> repository.acknowledgeDetection(id) },
            onDispatch = { id -> repository.dispatchRangers(id) }
        )
    }

    if (showLocationPicker) {
        MapLocationPickerModal(
            currentLocation = user.location,
            onLocationSelected = { newLoc ->
                AuthRepository.getInstance().updateLocation(newLoc)
            },
            onDismiss = { showLocationPicker = false }
        )
    }
}

@Composable
private fun DashboardHeader(
    user: User,
    isFirestoreConnected: Boolean,
    onLocationClick: () -> Unit,
    onSignOut: () -> Unit,
    onToggleRole: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ForestSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top row: App title & Streaming sync badge & Sign Out
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WildGuard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Clean status tag
                        Text(
                            text = if (isFirestoreConnected) "Firestore stream connected" else "Live stream connected",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "Perimeter monitoring console",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            IconButton(onClick = onSignOut) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign out",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second row: User Role & Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Role Tag (Clickable to toggle for hackathon demo)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ForestCard)
                    .border(1.dp, ForestCardBorder, RoundedCornerShape(4.dp))
                    .clickable { onToggleRole() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("role_badge_toggle")
            ) {
                Icon(
                    imageVector = if (user.userType == UserType.SYSTEM_OPERATOR) Icons.Default.Security else Icons.Default.Warning,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${user.userType.label} (Switch)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            // Location Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ForestCard)
                    .border(1.dp, ForestCardBorder, RoundedCornerShape(4.dp))
                    .clickable { onLocationClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.location.name.split(" ").take(2).joinToString(" "),
                    fontSize = 11.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmergencyAlertBanner(
    alert: Detection,
    onView: () -> Unit,
    onDismiss: () -> Unit
) {
    val isCritical = alert.threatLevel == ThreatLevel.CRITICAL
    val bgColor = if (isCritical) CrimsonCritical else AmberAlert

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Emergency,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "${alert.threatLevel.title} alert: ${alert.speciesName}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${alert.distanceMeters}m ${alert.direction} from perimeter",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "View",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x33000000))
                    .clickable { onView() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "✕",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun ThreatSummaryRow(
    totalCount: Int,
    criticalCount: Int,
    nearestDistance: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            title = "Active alerts",
            value = "$totalCount",
            accentColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Critical threats",
            value = "$criticalCount",
            accentColor = if (criticalCount > 0) CrimsonCritical else TextSecondary,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Nearest sighting",
            value = if (nearestDistance > 0) "${nearestDistance}m" else "--",
            accentColor = if (nearestDistance in 1..299) CrimsonCritical else TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = ForestCard),
        modifier = modifier.border(1.dp, ForestCardBorder, RoundedCornerShape(4.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun HackathonQuickTriggerRow(
    onTrigger: (AnimalSpecies, ThreatLevel, Int, String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Simulate intrusion (<1s broadcast)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                DemoScenarioButton(
                    label = "🐅 Tiger 140m N",
                    onClick = {
                        onTrigger(AnimalSpecies.TIGER, ThreatLevel.CRITICAL, 140, "North", 0f)
                    }
                )
            }
            item {
                DemoScenarioButton(
                    label = "🐘 Elephant 280m E",
                    onClick = {
                        onTrigger(AnimalSpecies.ELEPHANT, ThreatLevel.HIGH, 280, "East", 90f)
                    }
                )
            }
            item {
                DemoScenarioButton(
                    label = "🐆 Leopard 420m SW",
                    onClick = {
                        onTrigger(AnimalSpecies.LEOPARD, ThreatLevel.HIGH, 420, "South-West", 225f)
                    }
                )
            }
            item {
                DemoScenarioButton(
                    label = "🐻 Bear 95m W",
                    onClick = {
                        onTrigger(AnimalSpecies.SLOTH_BEAR, ThreatLevel.CRITICAL, 95, "West", 270f)
                    }
                )
            }
        }
    }
}

@Composable
private fun DemoScenarioButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ForestCard)
            .border(1.dp, ForestCardBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
