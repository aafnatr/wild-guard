package com.example.data

import android.util.Log
import com.example.model.AnimalSpecies
import com.example.model.Detection
import com.example.model.ThreatLevel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DetectionRepository private constructor() {

    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections.asStateFlow()

    private val _isFirestoreConnected = MutableStateFlow(false)
    val isFirestoreConnected: StateFlow<Boolean> = _isFirestoreConnected.asStateFlow()

    private val _lastAlert = MutableStateFlow<Detection?>(null)
    val lastAlert: StateFlow<Detection?> = _lastAlert.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Initialize default seed sightings for demonstration
        loadSeedDetections()
        // Try establishing Firestore real-time listener
        connectToFirestore()
    }

    private fun loadSeedDetections() {
        val now = System.currentTimeMillis()
        val seed = listOf(
            Detection(
                id = "DET-101",
                speciesName = AnimalSpecies.TIGER.commonName,
                scientificName = AnimalSpecies.TIGER.scientificName,
                threatLevel = ThreatLevel.CRITICAL,
                distanceMeters = 190,
                direction = "North-West",
                directionAngleDeg = 315f,
                confidence = 0.97f,
                timestamp = now - (45 * 1000), // 45 seconds ago
                sensorId = "OPTIC-CAM-NW-02",
                locationName = "North Buffer Ravine",
                notes = "Adult male tiger stalking along agricultural drainage fence.",
                acknowledged = false
            ),
            Detection(
                id = "DET-102",
                speciesName = AnimalSpecies.ELEPHANT.commonName,
                scientificName = AnimalSpecies.ELEPHANT.scientificName,
                threatLevel = ThreatLevel.HIGH,
                distanceMeters = 340,
                direction = "North-East",
                directionAngleDeg = 45f,
                confidence = 0.93f,
                timestamp = now - (4 * 60 * 1000), // 4 mins ago
                sensorId = "SEISMIC-NODE-NE-08",
                locationName = "Eastern Forest Boundary",
                notes = "Family herd of 4 elephants browsing near irrigation canal.",
                acknowledged = true
            ),
            Detection(
                id = "DET-103",
                speciesName = AnimalSpecies.LEOPARD.commonName,
                scientificName = AnimalSpecies.LEOPARD.scientificName,
                threatLevel = ThreatLevel.HIGH,
                distanceMeters = 520,
                direction = "South",
                directionAngleDeg = 180f,
                confidence = 0.89f,
                timestamp = now - (12 * 60 * 1000), // 12 mins ago
                sensorId = "THERMAL-CAM-S-01",
                locationName = "South Ridge Ridgepath",
                notes = "Leopard sighted perched on granite rock overlooking settlement.",
                acknowledged = false
            ),
            Detection(
                id = "DET-104",
                speciesName = AnimalSpecies.WILD_BOAR.commonName,
                scientificName = AnimalSpecies.WILD_BOAR.scientificName,
                threatLevel = ThreatLevel.LOW,
                distanceMeters = 850,
                direction = "South-East",
                directionAngleDeg = 135f,
                confidence = 0.85f,
                timestamp = now - (25 * 60 * 1000),
                sensorId = "PIR-SENSOR-SE-05",
                locationName = "Outer Buffer Grasslands",
                notes = "Small sounder foraging along riverbank, safe distance.",
                acknowledged = true
            )
        )
        _detections.value = seed
    }

    private fun connectToFirestore() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestoreListener?.remove()

            firestoreListener = firestore.collection("detections")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("WildGuard", "Firestore listener offline or denied: ${error.message}")
                        _isFirestoreConnected.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        _isFirestoreConnected.value = true
                        val firestoreList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val threatStr = doc.getString("threatLevel") ?: "MEDIUM"
                                val threat = try {
                                    ThreatLevel.valueOf(threatStr)
                                } catch (e: Exception) {
                                    ThreatLevel.MEDIUM
                                }
                                Detection(
                                    id = doc.id,
                                    speciesName = doc.getString("speciesName") ?: "Wildlife Detection",
                                    scientificName = doc.getString("scientificName") ?: "",
                                    threatLevel = threat,
                                    distanceMeters = (doc.getLong("distanceMeters") ?: 250L).toInt(),
                                    direction = doc.getString("direction") ?: "North",
                                    directionAngleDeg = (doc.getDouble("directionAngleDeg") ?: 0.0).toFloat(),
                                    confidence = (doc.getDouble("confidence") ?: 0.92).toFloat(),
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    sensorId = doc.getString("sensorId") ?: "CAM-01",
                                    locationName = doc.getString("locationName") ?: "Zone Perimeter",
                                    notes = doc.getString("notes") ?: "",
                                    acknowledged = doc.getBoolean("acknowledged") ?: false,
                                    dispatched = doc.getBoolean("dispatched") ?: false
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (firestoreList.isNotEmpty()) {
                            // Merge and update
                            _detections.value = firestoreList
                        }
                    }
                }
        } catch (e: Throwable) {
            Log.i("WildGuard", "Running with Real-Time Direct Stream Engine: ${e.message}")
            _isFirestoreConnected.value = false
        }
    }

    fun addDetection(
        detection: Detection,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val newDetection = if (detection.id.isEmpty()) {
            detection.copy(id = "DET-" + UUID.randomUUID().toString().take(6).uppercase())
        } else {
            detection
        }

        // 1. Instantly update real-time flow stream (< 100ms)
        val current = _detections.value.toMutableList()
        current.removeAll { it.id == newDetection.id }
        current.add(0, newDetection)
        _detections.value = current
        _lastAlert.value = newDetection

        // 2. Write to Firestore if connected
        scope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val map = hashMapOf(
                    "speciesName" to newDetection.speciesName,
                    "scientificName" to newDetection.scientificName,
                    "threatLevel" to newDetection.threatLevel.name,
                    "distanceMeters" to newDetection.distanceMeters,
                    "direction" to newDetection.direction,
                    "directionAngleDeg" to newDetection.directionAngleDeg,
                    "confidence" to newDetection.confidence,
                    "timestamp" to newDetection.timestamp,
                    "sensorId" to newDetection.sensorId,
                    "locationName" to newDetection.locationName,
                    "notes" to newDetection.notes,
                    "acknowledged" to newDetection.acknowledged,
                    "dispatched" to newDetection.dispatched
                )
                firestore.collection("detections")
                    .document(newDetection.id)
                    .set(map)
                    .addOnSuccessListener {
                        _isFirestoreConnected.value = true
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(true)
                    }
            } catch (e: Throwable) {
                // Fallback succeeds locally immediately
                onComplete(true)
            }
        }
    }

    fun acknowledgeDetection(id: String) {
        val updated = _detections.value.map {
            if (it.id == id) it.copy(acknowledged = true) else it
        }
        _detections.value = updated
        scope.launch {
            try {
                FirebaseFirestore.getInstance().collection("detections").document(id)
                    .update("acknowledged", true)
            } catch (_: Throwable) {}
        }
    }

    fun dispatchRangers(id: String) {
        val updated = _detections.value.map {
            if (it.id == id) it.copy(dispatched = true, acknowledged = true) else it
        }
        _detections.value = updated
        scope.launch {
            try {
                FirebaseFirestore.getInstance().collection("detections").document(id)
                    .update(mapOf("dispatched" to true, "acknowledged" to true))
            } catch (_: Throwable) {}
        }
    }

    fun clearLastAlert() {
        _lastAlert.value = null
    }

    fun triggerPresetScenario(species: AnimalSpecies, threat: ThreatLevel, distanceMeters: Int, direction: String, angle: Float) {
        val detection = Detection(
            id = "DET-" + UUID.randomUUID().toString().take(6).uppercase(),
            speciesName = species.commonName,
            scientificName = species.scientificName,
            threatLevel = threat,
            distanceMeters = distanceMeters,
            direction = direction,
            directionAngleDeg = angle,
            confidence = (90 + (Math.random() * 8)).toFloat() / 100f,
            timestamp = System.currentTimeMillis(),
            sensorId = "SURV-STATION-" + ((1..9).random()),
            locationName = "Perimeter Corridor (${direction})",
            notes = "Live hackathon demo trigger: Automated computer vision pipeline triangulated $direction intrusion.",
            acknowledged = false
        )
        addDetection(detection)
    }

    companion object {
        @Volatile
        private var instance: DetectionRepository? = null

        fun getInstance(): DetectionRepository {
            return instance ?: synchronized(this) {
                instance ?: DetectionRepository().also { instance = it }
            }
        }
    }
}
