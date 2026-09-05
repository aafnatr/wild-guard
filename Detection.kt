package com.example.model

enum class ThreatLevel(
    val title: String,
    val hexColor: Long,
    val description: String,
    val advisory: String
) {
    LOW(
        title = "LOW",
        hexColor = 0xFF10B981,
        description = "Herbivore / solitary non-aggressive animal at safe perimeter distance.",
        advisory = "Maintain observation. No immediate action required."
    ),
    MEDIUM(
        title = "MEDIUM",
        hexColor = 0xFFF59E0B,
        description = "Wildlife approaching community boundary or agricultural zone.",
        advisory = "Stay indoors. Secure domestic livestock in covered pens."
    ),
    HIGH(
        title = "HIGH",
        hexColor = 0xFFF97316,
        description = "Predator or elephant herd actively moving towards village corridor.",
        advisory = "Activate perimeter warning strobe lights. Avoid nocturnal travel."
    ),
    CRITICAL(
        title = "CRITICAL",
        hexColor = 0xFFEF4444,
        description = "Imminent danger! High threat species breached buffer fence (<300m).",
        advisory = "IMMEDIATE EVACUATION / SHELTER IN PLACE. Sound automated acoustic siren!"
    )
}

enum class AnimalSpecies(
    val commonName: String,
    val scientificName: String,
    val defaultThreat: ThreatLevel,
    val iconEmoji: String,
    val category: String
) {
    ELEPHANT("Asian Elephant", "Elephas maximus", ThreatLevel.HIGH, "🐘", "Megafauna"),
    TIGER("Bengal Tiger", "Panthera tigris", ThreatLevel.CRITICAL, "🐅", "Apex Predator"),
    LEOPARD("Indian Leopard", "Panthera pardus", ThreatLevel.HIGH, "🐆", "Carnivore"),
    WILD_BOAR("Wild Boar", "Sus scrofa", ThreatLevel.MEDIUM, "🐗", "Crop Raider"),
    SLOTH_BEAR("Sloth Bear", "Melursus ursinus", ThreatLevel.HIGH, "🐻", "Aggressive Forager"),
    RHINO("One-Horned Rhino", "Rhinoceros unicornis", ThreatLevel.MEDIUM, "🦏", "Protected Herbivore"),
    GAUR("Indian Bison (Gaur)", "Bos gaurus", ThreatLevel.MEDIUM, "🐂", "Large Bovine")
}

data class Detection(
    val id: String = "",
    val speciesName: String = "Asian Elephant",
    val scientificName: String = "Elephas maximus",
    val threatLevel: ThreatLevel = ThreatLevel.HIGH,
    val distanceMeters: Int = 280,
    val direction: String = "North-East",
    val directionAngleDeg: Float = 45f,
    val confidence: Float = 0.94f,
    val timestamp: Long = System.currentTimeMillis(),
    val sensorId: String = "CAM-TRAP-04",
    val locationName: String = "Northern Fence Line",
    val notes: String = "AI acoustic and optical triangulation confirmed movement.",
    val acknowledged: Boolean = false,
    val dispatched: Boolean = false
)
