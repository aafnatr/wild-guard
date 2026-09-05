package com.example.model

enum class UserType(val label: String, val description: String) {
    COMMUNITY_USER(
        label = "Community User",
        description = "Receives early intrusion warnings, threat sirens, and safe evacuation advisories."
    ),
    SYSTEM_OPERATOR(
        label = "System Operator",
        description = "Can broadcast manual alerts, manage sensor stations, and dispatch ranger response teams."
    )
}

data class UserLocation(
    val name: String,
    val zone: String,
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        val PRESETS = listOf(
            UserLocation(
                name = "Mudumalai Buffer Zone 4",
                zone = "Western Ghats Range",
                latitude = 11.5833,
                longitude = 76.5333
            ),
            UserLocation(
                name = "Kaziranga Perimeter East",
                zone = "Brahmaputra Floodplain",
                latitude = 26.5775,
                longitude = 93.1711
            ),
            UserLocation(
                name = "Jim Corbett Sector 2",
                zone = "Shivalik Foothills",
                latitude = 29.5300,
                longitude = 78.7747
            ),
            UserLocation(
                name = "Valparai Tea Estate Settlement",
                zone = "Anamalai Corridor",
                latitude = 10.3242,
                longitude = 76.9558
            ),
            UserLocation(
                name = "Bandipur Frontier Outpost",
                zone = "Southern Biosphere",
                latitude = 11.6664,
                longitude = 76.6291
            )
        )
    }
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val userType: UserType,
    val location: UserLocation
)
