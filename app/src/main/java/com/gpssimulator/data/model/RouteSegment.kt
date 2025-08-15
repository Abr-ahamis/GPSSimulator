package com.gpssimulator.data.model

data class RouteSegment(
    val startPoint: LocationPoint,
    val endPoint: LocationPoint,
    val distance: Double, // in meters
    val duration: Long, // in milliseconds
    val movementType: MovementType
)

enum class MovementType(val baseSpeed: Float, val speedVariation: Float) {
    WALKING(1.4f, 0.3f),    // ~5 km/h average walking speed
    RUNNING(3.3f, 0.8f),    // ~12 km/h average running speed
    CYCLING(6.9f, 2.0f)     // ~25 km/h average cycling speed
}
