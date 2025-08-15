package com.gpssimulator.data.model

import java.util.Date

data class Route(
    val id: Long = 0,
    val name: String,
    val startPoint: LocationPoint,
    val segments: List<RouteSegment>,
    val totalDistance: Double,
    val estimatedDuration: Long,
    val movementType: MovementType,
    val createdAt: Date = Date(),
    val isCompleted: Boolean = false,
    val actualDuration: Long = 0
) {
    fun getAllPoints(): List<LocationPoint> {
        return segments.flatMap { segment ->
            generatePointsBetween(segment.startPoint, segment.endPoint, segment.distance)
        }
    }
    
    private fun generatePointsBetween(start: LocationPoint, end: LocationPoint, totalDistance: Double): List<LocationPoint> {
        val points = mutableListOf<LocationPoint>()
        val numIncrements = (totalDistance / 10.0).toInt() // 10 meter increments
        val latStep = (end.latitude - start.latitude) / numIncrements
        val lonStep = (end.longitude - start.longitude) / numIncrements
        
        for (i in 0..numIncrements) {
            val point = LocationPoint(
                latitude = start.latitude + (latStep * i),
                longitude = start.longitude + (lonStep * i),
                speed = calculateSpeedForSegment(start, end, i, numIncrements),
                bearing = calculateBearing(start, end)
            )
            points.add(point)
        }
        
        return points
    }
    
    private fun calculateSpeedForSegment(start: LocationPoint, end: LocationPoint, currentStep: Int, totalSteps: Int): Float {
        val baseSpeed = movementType.baseSpeed
        val variation = movementType.speedVariation
        
        // Add natural variation
        val randomFactor = 0.8 + Math.random() * 0.4 // 0.8 to 1.2
        val progressFactor = when {
            currentStep < totalSteps * 0.3 -> 0.9f // Start slower
            currentStep > totalSteps * 0.7 -> 1.1f // End faster
            else -> 1.0f
        }
        
        return (baseSpeed * randomFactor * progressFactor).toFloat()
    }
    
    private fun calculateBearing(start: LocationPoint, end: LocationPoint): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)
        
        val dLon = lon2 - lon1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        
        return ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toFloat()
    }
}
