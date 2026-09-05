package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimalSpecies
import com.example.model.Detection
import com.example.model.ThreatLevel
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ForestCard
import com.example.ui.theme.ForestCardBorder
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDetectionDialog(
    onDismiss: () -> Unit,
    onAddDetection: (Detection) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedSpecies by remember { mutableStateOf(AnimalSpecies.TIGER) }
    var selectedThreat by remember { mutableStateOf(ThreatLevel.CRITICAL) }
    var distanceMeters by remember { mutableIntStateOf(160) }
    var selectedDirection by remember { mutableStateOf("North-West") }
    var directionAngle by remember { mutableFloatStateOf(315f) }
    var sensorId by remember { mutableStateOf("CAM-FIELD-ALPHA") }
    var locationName by remember { mutableStateOf("North Boundary Forest Ridge") }
    var notes by remember { mutableStateOf("Camera sensor detected movement at boundary.") }

    val directions = listOf(
        Pair("North", 0f),
        Pair("North-East", 45f),
        Pair("East", 90f),
        Pair("South-East", 135f),
        Pair("South", 180f),
        Pair("South-West", 225f),
        Pair("West", 270f),
        Pair("North-West", 315f)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ForestSurface,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_detection_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trigger intrusion alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Broadcasts to all connected monitors in under 1 second",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Scenarios for testing
            Text(
                text = "Quick test scenarios",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickScenarioChip(
                        title = "🐅 Tiger 140m",
                        onClick = {
                            selectedSpecies = AnimalSpecies.TIGER
                            selectedThreat = ThreatLevel.CRITICAL
                            distanceMeters = 140
                            selectedDirection = "North"
                            directionAngle = 0f
                            locationName = "North Buffer Ravine"
                            notes = "Apex predator observed at perimeter line."
                        }
                    )
                }
                item {
                    QuickScenarioChip(
                        title = "🐘 Elephants 280m",
                        onClick = {
                            selectedSpecies = AnimalSpecies.ELEPHANT
                            selectedThreat = ThreatLevel.HIGH
                            distanceMeters = 280
                            selectedDirection = "East"
                            directionAngle = 90f
                            locationName = "Canal Sector 2"
                            notes = "Herd moving along canal corridor."
                        }
                    )
                }
                item {
                    QuickScenarioChip(
                        title = "🐆 Leopard 420m",
                        onClick = {
                            selectedSpecies = AnimalSpecies.LEOPARD
                            selectedThreat = ThreatLevel.HIGH
                            distanceMeters = 420
                            selectedDirection = "South-West"
                            directionAngle = 225f
                            locationName = "Granite Ridge Outcrop"
                            notes = "Solitary animal sighted on ridge."
                        }
                    )
                }
                item {
                    QuickScenarioChip(
                        title = "🐗 Wild Boar 750m",
                        onClick = {
                            selectedSpecies = AnimalSpecies.WILD_BOAR
                            selectedThreat = ThreatLevel.LOW
                            distanceMeters = 750
                            selectedDirection = "South-East"
                            directionAngle = 135f
                            locationName = "Outer Buffer Grassland"
                            notes = "Foraging group in grassland."
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Select Species
            Text(
                text = "Wildlife species",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AnimalSpecies.values()) { species ->
                    val isSelected = species == selectedSpecies
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) ForestCardBorder else ForestCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) TextPrimary else ForestCardBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                selectedSpecies = species
                                selectedThreat = species.defaultThreat
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = species.iconEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = species.commonName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Select Threat Level
            Text(
                text = "Threat level",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThreatLevel.values().forEach { level ->
                    val isSelected = level == selectedThreat
                    val lvlColor = Color(level.hexColor)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) lvlColor.copy(alpha = 0.2f) else ForestCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) lvlColor else ForestCardBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { selectedThreat = level }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.title,
                            color = if (isSelected) lvlColor else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Distance Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Distance from perimeter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "$distanceMeters meters",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Slider(
                value = distanceMeters.toFloat(),
                onValueChange = { distanceMeters = it.toInt() },
                valueRange = 50f..1200f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = EmeraldPrimary,
                    inactiveTrackColor = ForestCardBorder
                ),
                modifier = Modifier.testTag("distance_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Direction Selector
            Text(
                text = "Direction",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(directions) { (dirName, angle) ->
                    val isSelected = dirName == selectedDirection
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) ForestCardBorder else ForestCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) TextPrimary else ForestCardBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                selectedDirection = dirName
                                directionAngle = angle
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = dirName,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sector & Notes
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Perimeter sector") },
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextPrimary,
                    unfocusedBorderColor = ForestCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observation notes") },
                shape = RoundedCornerShape(4.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextPrimary,
                    unfocusedBorderColor = ForestCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Broadcast Button
            Button(
                onClick = {
                    val detection = Detection(
                        speciesName = selectedSpecies.commonName,
                        scientificName = selectedSpecies.scientificName,
                        threatLevel = selectedThreat,
                        distanceMeters = distanceMeters,
                        direction = selectedDirection,
                        directionAngleDeg = directionAngle,
                        confidence = 0.96f,
                        timestamp = System.currentTimeMillis(),
                        sensorId = sensorId,
                        locationName = locationName,
                        notes = notes,
                        acknowledged = false,
                        dispatched = false
                    )
                    onAddDetection(detection)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("broadcast_detection_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(selectedThreat.hexColor),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Broadcast alert",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun QuickScenarioChip(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ForestCard)
            .border(1.dp, ForestCardBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
